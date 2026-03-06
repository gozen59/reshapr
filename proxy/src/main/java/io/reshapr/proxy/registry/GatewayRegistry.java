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
package io.reshapr.proxy.registry;

import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry for model (like services, artifacts and configuration plans) entries in the Reshapr Gateway.
 * This registry allows adding, retrieving, and removing different entries
 * based on their ID or coordinates (organization ID, service name, and version).
 * @author laurent
 */
@ApplicationScoped
public class GatewayRegistry {

   private long lastUpdateTimestamp = System.currentTimeMillis();

   private final ConcurrentHashMap<String, ServiceEntry> servicesById = new ConcurrentHashMap<>();
   private final ConcurrentHashMap<ServiceCoordinates, ServiceEntry> servicesByCoordinates = new ConcurrentHashMap<>();

   private final ConcurrentHashMap<ServiceEntry, ConfigurationEntry> configurations = new ConcurrentHashMap<>();

   private final ConcurrentHashMap<ServiceEntry, ArtifactEntry> mainArtifacts = new ConcurrentHashMap<>();
   private final ConcurrentHashMap<ServiceEntry, List<ArtifactEntry>> attachedArtifacts = new ConcurrentHashMap<>();

   private final ConcurrentHashMap<ToolEntry, ResourceEntry> resourcesByTool = new ConcurrentHashMap<>();

   /**
    * Retrieves the timestamp of the last update to the registry.
    * @return The last update timestamp in milliseconds.
    */
   public long getLastUpdateTimestamp() {
      return lastUpdateTimestamp;
   }

   /**
    * Retrieves all registered service entries.
    * @return A list of all service entries.
    */
   public List<ServiceEntry> getAllServices() {
      return servicesById.values().stream().toList();
   }

   /**
    * Adds a service entry to the registry.
    * @param serviceEntry The service entry to add.
    */
   public void addService(ServiceEntry serviceEntry) {
      servicesById.put(serviceEntry.id(), serviceEntry);
      servicesByCoordinates.put(new ServiceCoordinates(serviceEntry.organizationId(), serviceEntry.name(), serviceEntry.version()), serviceEntry);
      lastUpdateTimestamp = System.currentTimeMillis();
   }

   /**
    * Retrieves a service entry by its ID.
    * @param serviceId The ID of the service to retrieve.
    * @return The service entry, or null if not found.
    */
   public ServiceEntry getService(String serviceId) {
      return servicesById.get(serviceId);
   }

   /**
    * Retrieves a service entry by its organization ID, service name, and version.
    * @param organizationId The ID of the organization.
    * @param serviceName The name of the service.
    * @param version The version of the service.
    * @return The service entry, or null if not found.
    */
   public ServiceEntry getService(String organizationId, String serviceName, String version) {
      return servicesByCoordinates.get(new ServiceCoordinates(organizationId, serviceName, version));
   }

   /**
    * Removes a service entry by its ID.
    * @param serviceId The ID of the service to remove.
    */
   public void removeService(String serviceId) {
      ServiceEntry entry = servicesById.remove(serviceId);
      if (entry != null) {
         servicesByCoordinates.remove(new ServiceCoordinates(entry.organizationId(), entry.name(), entry.version()));
         configurations.remove(entry);
         mainArtifacts.remove(entry);
         attachedArtifacts.remove(entry);
      }
      lastUpdateTimestamp = System.currentTimeMillis();
   }

   /** Coordinates for a service entry, used for efficient retrieval. */
   private record ServiceCoordinates(String organizationId, String serviceName, String version) {}

   /**
    * Adds a configuration entry for a given service.
    * @param service The service entry this configuration is associated with.
    * @param configuration The configuration entry to add.
    */
   public void addConfiguration(ServiceEntry service, ConfigurationEntry configuration) {
      configurations.put(service, configuration);
   }

   /**
    * Retrieves the configuration entry for a given service.
    * @param service The service entry to retrieve the configuration for.
    * @return The configuration entry, or null if not found.
    */
   public ConfigurationEntry getConfiguration(ServiceEntry service) {
      return configurations.get(service);
   }

   /**
    * Adds the main artifact for a given service.
    * @param service The service entry this artifact is associated with.
    * @param artifact The artifact entry to add.
    */
   public void addMainArtifact(ServiceEntry service, ArtifactEntry artifact) {
      mainArtifacts.put(service, artifact);
   }

   /**
    * Adds attached artifacts for a given service.
    * @param service The service entry these artifacts are associated with.
    * @param artifacts The list of artifact entries to add.
    */
   public void addAttachedArtifacts(ServiceEntry service, List<ArtifactEntry> artifacts) {
      attachedArtifacts.put(service, artifacts);
   }

   /**
    * Retrieves the main artifact for a given service.
    * @param service The service entry to retrieve the main artifact for.
    * @return The main artifact entry, or null if not found.
    */
   public ArtifactEntry getMainArtifact(ServiceEntry service) {
      return mainArtifacts.get(service);
   }

   /**
    * Checks if there are attached artifacts for a given service.
    * @param service The service entry to check for attached artifacts.
    * @return True if there are attached artifacts, false otherwise.
    */
   public boolean hasAttachedArtifacts(ServiceEntry service) {
      return attachedArtifacts.containsKey(service);
   }

   /**
    * Retrieves the attached artifacts for a given service.
    * @param service The service entry to retrieve the attached artifacts for.
    * @return The list of attached artifact entries, or null if not found.
    */
   public List<ArtifactEntry> getAttachedArtifacts(ServiceEntry service) {
      return attachedArtifacts.get(service);
   }

   /**
    * Adds a resource entry for a given tool.
    * @param tool The tool entry this resource is associated with.
    * @param resource The resource entry to add.
    */
   public void addResourceForTool(ToolEntry tool, ResourceEntry resource) {
      resourcesByTool.put(tool, resource);
   }

   /**
    * Checks if there is a resource entry for a given tool.
    * @param tool The tool entry to check for a resource.
    * @return True if there is a resource entry, false otherwise.
    */
   public boolean hasResourceForTool(ToolEntry tool) {
      return resourcesByTool.containsKey(tool);
   }

   /**
    * Retrieves the resource entry for a given tool.
    * @param tool The tool entry to retrieve the resource for.
    * @return The resource entry, or null if not found.
    */
   public ResourceEntry getResourceForTool(ToolEntry tool) {
      return resourcesByTool.get(tool);
   }
}
