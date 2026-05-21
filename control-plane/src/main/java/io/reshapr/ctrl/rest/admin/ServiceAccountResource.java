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
package io.reshapr.ctrl.rest.admin;

import io.quarkus.panache.common.Sort;
import io.reshapr.ctrl.model.ServiceAccount;
import io.reshapr.ctrl.repository.ServiceAccountRepository;
import io.reshapr.ctrl.security.AdminAuthenticated;
import io.smallrye.common.annotation.RunOnVirtualThread;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@RunOnVirtualThread
@Path("/api/admin/serviceAccounts")
@AdminAuthenticated
public class ServiceAccountResource {

   /** Get a JBoss logging logger. */
   private final Logger logger = Logger.getLogger(getClass());

   private final ServiceAccountRepository serviceAccountRepository;

   /**
    * Build a ServiceAccountResource with required dependencies.
    * @param serviceAccountRepository The ServiceAccount repository
    */
   public ServiceAccountResource(ServiceAccountRepository serviceAccountRepository) {
      this.serviceAccountRepository = serviceAccountRepository;
   }

   @GET
   @Produces(MediaType.APPLICATION_JSON)
   public List<ServiceAccount> getServiceAccounts() {
      logger.debugf("Getting all Service accounts");
      return serviceAccountRepository.findAll(Sort.ascending("name")).list();
   }

   @POST
   @Transactional
   @Produces(MediaType.APPLICATION_JSON)
   public Response createServiceAccount(@Valid ServiceAccountRequestDTO serviceAccountRequestDTO) {
      logger.debugf("Creating service account with name: %s", serviceAccountRequestDTO.name());

      var existingSA = serviceAccountRepository.findByName(serviceAccountRequestDTO.name());
      if (existingSA != null) {
         logger.warnf("Service account with name '%s' already exists", serviceAccountRequestDTO.name());
         return Response.status(Response.Status.CONFLICT).entity("Service account with same name already exists").build();
      }

      // Proceed to service account creation.
      ServiceAccount serviceAccount = new ServiceAccount();
      serviceAccount.name = serviceAccountRequestDTO.name();
      serviceAccount.description = serviceAccountRequestDTO.description();
      serviceAccount.k8sSubject = serviceAccountRequestDTO.k8sSubject();
      serviceAccount.allowedOrganizations = serviceAccountRequestDTO.allowedOrganizations();
      // Set the validity date.
      serviceAccount.validUntil = LocalDateTime.ofInstant(
            Instant.ofEpochMilli(System.currentTimeMillis() + serviceAccountRequestDTO.validityDays() * 24L * 60L * 60L * 1000L),
            ZoneId.systemDefault());
      serviceAccountRepository.persistAndFlush(serviceAccount);
      // Return 201 status with entity.
      return Response.status(Response.Status.CREATED).entity(serviceAccount).build();
   }

   @PUT
   @Path("/{id}")
   @Transactional
   @Produces(MediaType.APPLICATION_JSON)
   public Response updateServiceAccount(@PathParam("id") String id, @Valid ServiceAccountRequestDTO serviceAccountRequestDTO) {
      logger.debugf("Updating service account with id: %s", id);

      ServiceAccount serviceAccount = serviceAccountRepository.findById(id);
      if (serviceAccount == null) {
         return Response.status(Response.Status.NOT_FOUND).build();
      }
      serviceAccount.description = serviceAccountRequestDTO.description();
      serviceAccount.k8sSubject = serviceAccountRequestDTO.k8sSubject();
      serviceAccount.allowedOrganizations = serviceAccountRequestDTO.allowedOrganizations();
      // Set the validity date.
      serviceAccount.validUntil = LocalDateTime.ofInstant(
            Instant.ofEpochMilli(System.currentTimeMillis() + serviceAccountRequestDTO.validityDays() * 24L * 60L * 60L * 1000L),
            ZoneId.systemDefault());
      serviceAccountRepository.persistAndFlush(serviceAccount);
      // Return 200 status with entity.
      return Response.ok(serviceAccount).build();
   }

   @DELETE
   @Transactional
   @Path("/{id}")
   public Response deleteServiceAccount(@PathParam("id") String id) {
      logger.debugf("Deleting service account with id: %s", id);
      ServiceAccount serviceAccount = serviceAccountRepository.findById(id);
      if (serviceAccount == null) {
         return Response.status(Response.Status.NOT_FOUND).build();
      }
      serviceAccountRepository.delete(serviceAccount);
      return Response.noContent().build();
   }
}
