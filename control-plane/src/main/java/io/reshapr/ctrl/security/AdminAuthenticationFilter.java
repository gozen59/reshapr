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
package io.reshapr.ctrl.security;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import java.io.IOException;
import java.lang.reflect.Method;

/**
 * AdminAuthenticationFilter is a JAX-RS filter that checks for a valid admin API key
 * on endpoints annotated with @AdminAuthenticated.
 * @author laurent
 */
@Provider
@AdminAuthenticated
public class AdminAuthenticationFilter implements ContainerRequestFilter {

   /** Get a JBoss logging logger. */
   private final Logger logger = Logger.getLogger(getClass());

   @ConfigProperty(name = "reshapr.ctrl.api.key")
   String controlPlaneApiKey;

   @Context
   ResourceInfo resourceInfo;

   @Override
   public void filter(ContainerRequestContext requestContext) throws IOException {
      logger.debugf("Processing request for %s", requestContext.getUriInfo().getPath());

      Class<?> clazz = resourceInfo.getResourceClass();
      Method method = resourceInfo.getResourceMethod();
      if (clazz.isAnnotationPresent(AdminAuthenticated.class) || (method != null && method.isAnnotationPresent(AdminAuthenticated.class))) {
         logger.debug("AdminAuthenticated annotation found, performing admin authentication check.");

         String apiKey = requestContext.getHeaderString("x-reshapr-api-key");
         // If authentication fails, abort the request
         if (apiKey == null || !apiKey.equals(controlPlaneApiKey)) {
            logger.warn("Invalid or missing API key for admin endpoint.");
            requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).entity("Unauthorized").build());
         }

         // Proceed.
         logger.debug("Admin authentication successful.");
      }
   }
}
