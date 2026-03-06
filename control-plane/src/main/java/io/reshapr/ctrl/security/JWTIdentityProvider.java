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

import io.quarkus.security.identity.AuthenticationRequestContext;
import io.quarkus.security.identity.IdentityProvider;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.security.identity.request.TokenAuthenticationRequest;
import io.quarkus.security.runtime.QuarkusSecurityIdentity;
import io.smallrye.jwt.auth.principal.JWTParser;
import io.smallrye.jwt.auth.principal.ParseException;
import io.smallrye.mutiny.Uni;
import io.vertx.core.Vertx;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.jboss.logging.Logger;

/**
 * Identity provider for JWT token authentication.
 * @author laurent
 */
@Priority(2)
@ApplicationScoped
public class JWTIdentityProvider implements IdentityProvider<TokenAuthenticationRequest>  {

   /** Get a JBoss logging logger. */
   private final Logger logger = Logger.getLogger(getClass());

   private final JWTParser parser;

   public JWTIdentityProvider(JWTParser parser) {
      this.parser = parser;
   }

   @Override
   public Class<TokenAuthenticationRequest> getRequestType() {
      return TokenAuthenticationRequest.class;
   }

   @Override
   public Uni<SecurityIdentity> authenticate(TokenAuthenticationRequest request, AuthenticationRequestContext context) {
      logger.tracef("authenticate() called with token: %s", request.getToken().getToken());

      try {
         JsonWebToken jwtPrincipal = parser.parse(request.getToken().getToken());

         QuarkusSecurityIdentity.Builder identityBuilder = QuarkusSecurityIdentity.builder()
               .setPrincipal(jwtPrincipal)
               .addCredential(request.getToken());

         if (jwtPrincipal.claim("org").isPresent()) {
            String organizationId = jwtPrincipal.claim("org").get().toString();
            logger.debugf("authenticate(): Extracted organisationId from claim: %s", organizationId);

            // Set the current tenant context for the organization.
            ReshaprTenantContext.setCurrentTenant(organizationId);
            Vertx.currentContext().putLocal(ReshaprTenantResolver.TENANT_ID_CONTEXT_KEY, organizationId);

            identityBuilder.addAttribute(ReshaprTenantResolver.TENANT_ID_CONTEXT_KEY, organizationId);
         }

         return Uni.createFrom().item(identityBuilder.build());
      } catch (ParseException pe) {
         logger.debug("authenticate(): Invalid failed with JWT parsing exception", pe);
         return Uni.createFrom().nullItem();
      }
   }
}
