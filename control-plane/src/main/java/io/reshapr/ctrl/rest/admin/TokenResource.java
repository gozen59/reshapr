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

import io.reshapr.ctrl.rest.v1.Mappers;
import io.reshapr.ctrl.security.AdminAuthenticated;
import io.reshapr.ctrl.service.TokenManagerService;

import io.smallrye.common.annotation.RunOnVirtualThread;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;

@RunOnVirtualThread
@Path("/api/admin/tokens")
@AdminAuthenticated
public class TokenResource {

   /** Get a JBoss logging logger. */
   private final Logger logger = Logger.getLogger(getClass());

   private final TokenManagerService tokenManagerService;
   private final Mappers v1Mappers;

   /**
    * Build a TokenResource with required dependencies.
    * @param tokenManagerService The TokenManager service
    * @param v1Mappers The v1 mappers
    */
   public TokenResource(TokenManagerService tokenManagerService, Mappers v1Mappers) {
      this.tokenManagerService = tokenManagerService;
      this.v1Mappers = v1Mappers;
   }

   @GET
   @Path("/apiTokens/{organizationName}")
   public Response getApiTokens(@PathParam("organizationName") String organizationName) {
      logger.debugf("Getting API tokens for organization: %s", organizationName);
      return Response.ok(v1Mappers.toATResources(tokenManagerService.getApiTokens(organizationName))).build();
   }
}
