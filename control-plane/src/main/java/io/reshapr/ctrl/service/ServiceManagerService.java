/*
 * Copyright The Reshapr Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.reshapr.ctrl.service;

import io.reshapr.ctrl.model.Artifact;
import io.reshapr.ctrl.model.ArtifactType;
import io.reshapr.ctrl.model.ConfigurationPlan;
import io.reshapr.ctrl.model.Operation;
import io.reshapr.ctrl.model.OperationNameFilterPredicate;
import io.reshapr.ctrl.model.Secret;
import io.reshapr.ctrl.model.Service;
import io.reshapr.ctrl.model.ServiceType;
import io.reshapr.ctrl.repository.ArtifactRepository;
import io.reshapr.ctrl.repository.SecretRepository;
import io.reshapr.ctrl.repository.ServiceRepository;
import io.reshapr.ctrl.util.ReshaprArtifactBuilder;
import io.reshapr.ctrl.util.ReshaprArtifactException;
import io.reshapr.ctrl.util.NamedOpenAPIImporter;
import io.reshapr.ctrl.util.NamedProtobufImporter;

import io.github.microcks.domain.Resource;
import io.github.microcks.util.HTTPDownloader;
import io.github.microcks.util.MockRepositoryImportException;
import io.github.microcks.util.MockRepositoryImporter;
import io.github.microcks.util.MockRepositoryImporterFactory;
import io.github.microcks.util.ReferenceResolver;
import io.github.microcks.util.RelativeReferenceURLBuilderFactory;
import io.github.microcks.util.graphql.GraphQLImporter;
import io.github.microcks.util.grpc.ProtobufImporter;
import io.github.microcks.util.openapi.OpenAPIImporter;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import org.jboss.logging.Logger;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Predicate;

/**
 * Service for managing service definitions in the Reshapr control plane.
 * @author laurent
 */
@ApplicationScoped
public class ServiceManagerService {

   /** Get a JBoss logging logger. */
   private final Logger logger = Logger.getLogger(getClass());

   private final SecretRepository secretRepository;
   private final ServiceRepository serviceRepository;
   private final ArtifactRepository artifactRepository;
   private final ConfigurationPlanManagerService configurationPlanManagerService;
   private final ExpositionManagerService expositionManagerService;

   public ServiceManagerService(SecretRepository secretRepository, ServiceRepository serviceRepository,
                                ArtifactRepository artifactRepository, ConfigurationPlanManagerService configurationPlanManagerService,
                                ExpositionManagerService expositionManagerService) {
      this.secretRepository = secretRepository;
      this.serviceRepository = serviceRepository;
      this.artifactRepository = artifactRepository;
      this.configurationPlanManagerService = configurationPlanManagerService;
      this.expositionManagerService = expositionManagerService;
   }

   /**
    * Imports a service definition from a remote URL.
    * @param url The URL of the remote service specification.
    * @param secretName The name of the secret to use for authentication (optional).
    * @param mainArtifact Whether this is the main artifact for the service.
    * @param serviceInfo Optional overriding information on the service to discover (maybe null).
    * @return The imported service.
    * @throws MockRepositoryImportException If an error occurs during import.
    */
   public Service importRemoteSpecification(String url, String secretName, boolean mainArtifact, ServiceInfo serviceInfo)
         throws MockRepositoryImportException {
      logger.debugf("Importing Service definition from remote URL %s", url);

      Secret secret = null;
      if (secretName != null) {
         secret = secretRepository.findByName(secretName);
         logger.debugf("Secret %s was requested. Have we found it? %s", secretName, (secret != null));
      }

      File specificationFile = null;
      try {
         io.github.microcks.domain.Secret microcksSecret = toMicrocksSecret(secret);

         // Download remote to local file before import.
         HTTPDownloader.FileAndHeaders fileAndHeaders = HTTPDownloader.handleHTTPDownloadToFileAndHeaders(url,
               microcksSecret, true);
         specificationFile = fileAndHeaders.getLocalFile();

         // Import the specification file.
         return importSpecificationFile(
               new SpecificationArtifactInfo(url, specificationFile, mainArtifact),
               new ReferenceResolver(url, microcksSecret, true,
                     RelativeReferenceURLBuilderFactory
                           .getRelativeReferenceURLBuilder(fileAndHeaders.getResponseHeaders())),
               serviceInfo
         );

      } catch (IOException ioe) {
         logger.errorf("Unable to download specification file from URL %s: %s", url, ioe.getMessage());
         throw new MockRepositoryImportException(ioe.getMessage(), ioe);
      } finally {
         // Cleanup and remove local file.
         if (specificationFile != null) {
            specificationFile.delete();
         }
      }
   }

   /**
    * Imports a service definition from the given repository file.
    * @param artifactInfo Information about the specification artifact being imported.
    * @param referenceResolver A reference resolver to use for relative references (maybe null).
    * @param serviceInfo Optional overriding information on the service to discover (maybe null).
    * @return The imported service.
    * @throws MockRepositoryImportException If an error occurs during import.
    */
   @Transactional
   public Service importSpecificationFile(SpecificationArtifactInfo artifactInfo, ReferenceResolver referenceResolver,
                                          ServiceInfo serviceInfo) throws MockRepositoryImportException {
      logger.debugf("Importing Service definition from artifact %s", artifactInfo.specificationFile().getName());

      // Retrieve the correct importer based on file path.
      MockRepositoryImporter importer = null;
      try {
         importer = MockRepositoryImporterFactory.getMockRepositoryImporter(artifactInfo.specificationFile(), referenceResolver);
      } catch (IOException ioe) {
         logger.errorf("Unable to create importer for file %s: %s", artifactInfo.specificationFile().getName(), ioe.getMessage());
         throw new MockRepositoryImportException(ioe.getMessage(), ioe);
      }

      // Prepare operation filtering predicate.
      Predicate<Operation> filterPredicate = new OperationNameFilterPredicate(null, null);

      // Check serviceInfo for name, version and operation overrides.
      if (serviceInfo != null) {
         if (serviceInfo.name() != null && serviceInfo.version() != null) {
            // In case of GraphQL, we may need to set service name and version if they are not present in the spec itself.
            if (importer instanceof GraphQLImporter graphQLImporter) {
               graphQLImporter.setServiceName(serviceInfo.name());
               graphQLImporter.setServiceVersion(serviceInfo.version());
            }
            // In case of OpenAPI, we may need to wrap the importer to force service name and version.
            if (importer instanceof OpenAPIImporter openAPIImporter) {
               importer = new NamedOpenAPIImporter(openAPIImporter,
                     serviceInfo.name(), serviceInfo.version());
            }
            // In case of Protobuf, we may need to wrap the importer to force service name and version.
            if (importer instanceof ProtobufImporter protobufImporter) {
               importer = new NamedProtobufImporter(protobufImporter,
                     serviceInfo.name(), serviceInfo.version());
            }
         }
         filterPredicate = new OperationNameFilterPredicate(serviceInfo.includedOperations(), serviceInfo.excludedOperations());
      }

      List<io.github.microcks.domain.Service> services = importer.getServiceDefinitions();
      if (services.size() != 1) {
         logger.warnf("Expected exactly one service definition in the artifact, found %d", services.size());
         throw new MockRepositoryImportException("Expected exactly one service definition in the artifact " + artifactInfo.name());
      }

      io.github.microcks.domain.Service microcksService = services.getFirst();
      Service service = serviceRepository.findByNameAndVersion(microcksService.getName(), microcksService.getVersion());

      if (service == null) {
         logger.debugf("Creating a new Service %s", microcksService.getName());
         service = new Service();
         service.name = microcksService.getName();
         service.version = microcksService.getVersion();
         service.createdOn = LocalDateTime.now();
         service.type = fromMicrocksServiceType(microcksService.getType());
      }

      // Set or update operation that may have changed since previous import.
      service.operations = microcksService.getOperations().stream()
            .map(this::fromMicrocksOperation)
            .filter(filterPredicate)
            .toList();
      serviceRepository.persist(service);

      // Remove previous artifacts attached to service if any.
      if (artifactInfo.mainArtifact()) {
         artifactRepository.delete("where service.id = ?1 and mainArtifact=true", service.id);
      } else {
         artifactRepository.delete("where service.id = ?1 and sourceArtifact = ?2",
               service.id, artifactInfo.name());
      }

      // Discover and persist new artifacts from the Microcks service definition.
      final Service finalService = service;
      List<Artifact> artifacts = importer.getResourceDefinitions(microcksService).stream()
            .map(this::fromMicrocksResource)
            .peek(artifact -> {
               artifact.service = finalService;
               artifact.sourceArtifact = artifactInfo.name();
               artifact.mainArtifact = artifactInfo.mainArtifact();
            })
            .toList();
      artifactRepository.persist(artifacts);

      return service;
   }

   @Transactional
   public boolean deleteService(String serviceId) {
      Service service = serviceRepository.findById(serviceId);
      if (service != null) {
         logger.infof("Deleting Service definition with id %s", serviceId);
         artifactRepository.delete("service.id = ?1", service.id);
         List<ConfigurationPlan> configurationPlans = configurationPlanManagerService.getConfigurationPlans(serviceId);
         configurationPlans.forEach(configurationPlanManagerService::deleteConfigurationPlan);
         serviceRepository.delete(service);
         return true;
      }
      return false;
   }

   /**
    * Attach an artifact to a service from a remote URL.
    * @param url The URL of the remote artifact.
    * @param secretName The name of the secret to use for authentication (optional).
    * @return The attached artifact.
    * @throws ReshaprArtifactException If an error occurs during attachment.
    */
   public Artifact attachRemoteArtifact(String url, String secretName) throws ReshaprArtifactException {
      logger.debugf("Attaching artifact to Service from remote URL %s", url);

      Secret secret = null;
      if (secretName != null) {
         secret = secretRepository.findByName(secretName);
         logger.debugf("Secret %s was requested. Have we found it? %s", secretName, (secret != null));
      }

      File artifactFile = null;
      try {
         io.github.microcks.domain.Secret microcksSecret = toMicrocksSecret(secret);

         // Download remote to local file before import.
         HTTPDownloader.FileAndHeaders fileAndHeaders = HTTPDownloader.handleHTTPDownloadToFileAndHeaders(url,
               microcksSecret, true);
         artifactFile = fileAndHeaders.getLocalFile();

         // Attach the artifact file.
         return attachArtifactFile(new AttachmentArtifactInfo(url, artifactFile));
      } catch (IOException ioe) {
         logger.errorf("Unable to download artifact file from URL %s: %s", url, ioe.getMessage());
         throw new ReshaprArtifactException(ioe.getMessage(), ioe);
      } finally {
         // Cleanup and remove local file.
         if (artifactFile != null) {
            artifactFile.delete();
         }
      }
   }

   /**
    * Attach an artifact to a service from a local file.
    * @param artifactInfo Information about the artifact being attached.
    * @return The attached artifact.
    * @throws ReshaprArtifactException If an error occurs during attachment.
    */
   @Transactional
   public Artifact attachArtifactFile(AttachmentArtifactInfo artifactInfo) throws ReshaprArtifactException {
      logger.debugf("Attaching information from artifact %s", artifactInfo.name());

      // Retrieve the correct importer based on file path.
      ReshaprArtifactBuilder.ArtifactWithServiceRef artifactWithServiceRef = ReshaprArtifactBuilder.parseArtifact(artifactInfo.name(), artifactInfo.attachmentFile());

      // Find the service to attach artifact to.
      Service service = serviceRepository.findByNameAndVersion(
            artifactWithServiceRef.serviceName(), artifactWithServiceRef.serviceVersion());
      if (service == null) {
         logger.errorf("No Service found with name %s and version %s to attach artifact %s",
               artifactWithServiceRef.serviceName(), artifactWithServiceRef.serviceVersion(), artifactWithServiceRef.artifact().name);
         throw new ReshaprArtifactException("No Service found with name " + artifactWithServiceRef.serviceName() +
               " and version " + artifactWithServiceRef.serviceVersion());
      }

      // Access to artifact information.
      Artifact artifact = artifactWithServiceRef.artifact();

      // Remove previous artifact of same type attached to service if any.
      artifactRepository.delete("where service.id = ?1 and type = ?2",
            service.id, artifact.type);

      // Configure and persist new artifact.
      artifact.service = service;
      artifact.mainArtifact = false;
      artifact.sourceArtifact = artifactInfo.name();
      artifactRepository.persist(artifact);

      // Propagate changes to exposition before returning.
      propagateArtifactsChanges(service);
      return artifact;
   }

   /** Triggers the potential updates of expositions when a Service artifact has changed. */
   protected void propagateArtifactsChanges(Service service) {
      logger.infof("Propagating artifact changes for service '%s'", service.id);
      expositionManagerService.propagateServiceChanges(service);
   }

   /** Converts a Microcks service type to a Reshapr service type. */
   private ServiceType fromMicrocksServiceType(io.github.microcks.domain.ServiceType microcksServiceType) {
      return switch (microcksServiceType) {
         case REST -> ServiceType.REST;
         case GRAPHQL -> ServiceType.GRAPHQL;
         case GRPC -> ServiceType.GRPC;
         default -> ServiceType.REST; // Default to REST if unknown
      };
   }

   /** Converts a Microcks operation to a Reshapr operation. */
   private Operation fromMicrocksOperation(io.github.microcks.domain.Operation microcksOperation) {
      Operation operation = new Operation();
      operation.name = microcksOperation.getName();
      operation.method = microcksOperation.getMethod();
      operation.action = microcksOperation.getAction();
      operation.inputName = microcksOperation.getInputName();
      operation.outputName = microcksOperation.getOutputName();
      return operation;
   }

   /** Converts a Microcks resource to a Reshapr artifact. */
   private Artifact fromMicrocksResource(Resource microcksResource) {
      Artifact artifact = new Artifact();
      artifact.name = microcksResource.getName();
      artifact.path = microcksResource.getPath();
      artifact.content = microcksResource.getContent();
      artifact.type = fromMicrocksResourceType(microcksResource.getType());
      return artifact;
   }

   /** Converts a Microcks resource type to a Reshapr artifact type. */
   private ArtifactType fromMicrocksResourceType(io.github.microcks.domain.ResourceType microcksResourceType) {
      return switch (microcksResourceType) {
         case OPEN_API_SPEC -> ArtifactType.OPEN_API_SPEC;
         case JSON_SCHEMA -> ArtifactType.JSON_SCHEMA;
         case GRAPHQL_SCHEMA -> ArtifactType.GRAPHQL_SCHEMA;
         case PROTOBUF_SCHEMA -> ArtifactType.PROTOBUF_SCHEMA;
         case PROTOBUF_DESCRIPTOR -> ArtifactType.PROTOBUF_DESCRIPTOR;
         default -> ArtifactType.JSON_FRAGMENT; // Default to JSON_FRAGMENT if unknown.
      };
   }

   /** Converts a Reshapr secret to a Microcks secret. */
   private io.github.microcks.domain.Secret toMicrocksSecret(Secret secret) {
      if (secret != null) {
         io.github.microcks.domain.Secret microcksSecret = new io.github.microcks.domain.Secret();
         microcksSecret.setId(secret.id);
         microcksSecret.setName(secret.name);
         microcksSecret.setDescription(secret.description);
         microcksSecret.setUsername(secret.username);
         microcksSecret.setPassword(secret.getPassword());
         microcksSecret.setTokenHeader(secret.tokenHeader);
         microcksSecret.setToken(secret.getToken());
         microcksSecret.setCaCertPem(secret.certPem);
         return microcksSecret;
      }
      return null;
   }
}
