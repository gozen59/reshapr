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
package io.reshapr.ctrl.rest.v1;

import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import io.quarkus.security.Authenticated;
import io.reshapr.ctrl.model.Service;
import io.reshapr.ctrl.repository.ServiceRepository;
import io.reshapr.ctrl.service.ServiceManagerService;

import io.smallrye.common.annotation.RunOnVirtualThread;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;

import java.util.List;

/**
 * ServiceResource provides REST endpoints for managing services.
 * @author laurent
 */
@RunOnVirtualThread
@Path("/api/v1/services")
public class ServiceResource {

   /** Get a JBoss logging logger. */
   private final Logger logger = Logger.getLogger(getClass());

   private final ServiceManagerService serviceManagerService;
   private final ServiceRepository serviceRepository;
   private final Mappers v1Mappers;

   public ServiceResource(ServiceManagerService serviceManagerService, ServiceRepository serviceRepository, Mappers v1Mappers) {
      this.serviceManagerService = serviceManagerService;
      this.serviceRepository = serviceRepository;
      this.v1Mappers = v1Mappers;
   }

   @GET
   @Authenticated
   public List<ServiceDTO> getServices(@QueryParam("page") @DefaultValue("0") int page,
         @QueryParam("size") @DefaultValue("20") int size) {
      return serviceRepository.findAll(Sort.ascending("name", "version")).page(Page.of(page, size))
            .project(ServiceDTO.class).list();
   }

   @GET
   @Authenticated
   @Path("/{id}")
   @Produces(MediaType.APPLICATION_JSON)
   public Response getServiceView(@PathParam("id") String id) {
      logger.debugf("Getting service view for service %s", id);
      Service service = serviceRepository.findByIdWithOperations(id);
      if (service == null) {
         return Response.status(Response.Status.NOT_FOUND).build();
      }
      return Response.ok().entity(v1Mappers.toResource(service)).build();
   }

   @DELETE
   @Authenticated
   @Transactional
   @Path("/{id}")
   public Response deleteService(@PathParam("id") String id) {
      logger.debugf("Deleting service with id %s", id);
      boolean found = serviceManagerService.deleteService(id);
      if (!found) {
         return Response.status(Response.Status.NOT_FOUND).build();
      }
      return Response.noContent().build();
   }
}
