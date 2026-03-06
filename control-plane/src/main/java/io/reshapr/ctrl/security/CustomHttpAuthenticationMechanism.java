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

import io.netty.handler.codec.http.HttpResponseStatus;
import io.quarkus.security.AuthenticationFailedException;
import io.quarkus.security.credential.TokenCredential;
import io.quarkus.security.identity.IdentityProviderManager;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.security.identity.request.AuthenticationRequest;
import io.quarkus.security.identity.request.TokenAuthenticationRequest;
import io.quarkus.vertx.http.runtime.security.ChallengeData;
import io.quarkus.vertx.http.runtime.security.HttpAuthenticationMechanism;
import io.quarkus.vertx.http.runtime.security.HttpCredentialTransport;
import io.smallrye.mutiny.Uni;
import io.vertx.ext.web.RoutingContext;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Alternative;
import org.jboss.logging.Logger;

import java.util.Set;

/**
 * Custom HTTP authentication mechanism for Reshapr.
 * This mechanism extracts the token from the Authorization header and authenticates it.
 * @author laurent
 */
@Alternative
@Priority(1)
@ApplicationScoped
public class CustomHttpAuthenticationMechanism implements HttpAuthenticationMechanism {

   /** Get a JBoss logging logger. */
   private final Logger logger = Logger.getLogger(getClass());

   public static final String API_TOKEN_TYPE = "api-token";
   public static final String BEARER_TYPE = "bearer";

   private static final String API_TOKEN_PREFIX = "ApiToken ";
   private static final String BEARER_PREFIX = "Bearer ";


   @Override
   public Set<Class<? extends AuthenticationRequest>> getCredentialTypes() {
      return Set.of(TokenAuthenticationRequest.class);
   }

   @Override
   public Uni<HttpCredentialTransport> getCredentialTransport(RoutingContext context) {
      return HttpAuthenticationMechanism.super.getCredentialTransport(context);
   }

   @Override
   public Uni<ChallengeData> getChallenge(RoutingContext context) {
      return Uni.createFrom().item(new ChallengeData(HttpResponseStatus.UNAUTHORIZED.code(), "Authorization", "ApiToken <token> || Bearer <jwt>"));
   }

   @Override
   public Uni<SecurityIdentity> authenticate(RoutingContext context, IdentityProviderManager identityProviderManager) {
      logger.tracef("authenticate() called with context: %s", context);

      if (context.request().getHeader("Authorization") != null) {
         String header = context.request().getHeader("Authorization");

         AuthenticationRequest request = null;
         if (header.startsWith(API_TOKEN_PREFIX)){
            String token = context.request().getHeader("Authorization").substring(API_TOKEN_PREFIX.length());
            logger.tracef("authenticate() called with ApiToken: %s", token);
            request = new ApiTokenAuthenticationRequest(new TokenCredential(token, API_TOKEN_TYPE));
         } else if (header.startsWith(BEARER_PREFIX)) {
            String token = context.request().getHeader("Authorization").substring(BEARER_PREFIX.length());
            logger.tracef("authenticate() called with Bearer: %s", token);
            request = new TokenAuthenticationRequest(new TokenCredential(token, BEARER_TYPE));
         }

         if (request != null) {
            return identityProviderManager.authenticate(request);
         }
      }
      logger.error("authenticate() called without a valid token");
      return Uni.createFrom().failure(new AuthenticationFailedException());
   }
}
