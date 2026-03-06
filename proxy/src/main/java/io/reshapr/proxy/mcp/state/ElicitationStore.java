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
package io.reshapr.proxy.mcp.state;

import io.reshapr.proxy.registry.SecretEntry;

import io.quarkus.cache.Cache;
import io.quarkus.cache.CacheName;
import io.quarkus.cache.CaffeineCache;
import jakarta.annotation.Nullable;
import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.logging.Logger;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Store for managing elicitation information within the Reshapr MCP.
 * This store allows storing and retrieving elicitation data.
 * @author laurent
 */
@ApplicationScoped
public class ElicitationStore {

   /** Get a JBoss logging logger. */
   private final Logger logger = Logger.getLogger(getClass());

   private final Cache elicitationCache;

   /**
    * Create a new ElicitationStore with a cache for elicitation information.
    * @param elicitationCache The cache to use for storing elicitation information
    */
   public ElicitationStore(@CacheName("elicitation-store") Cache elicitationCache) {
      this.elicitationCache = elicitationCache;
   }

   public String initializeElicitation(String sessionId, String organizationId, String backendEndpoint, SecretEntry secretEntry) {
      String elicitationId = UUID.randomUUID().toString();
      logger.debugf("Initializing new elicitation with id '%s' for session '%s'", elicitationId, sessionId);
      elicitationCache.as(CaffeineCache.class).put(elicitationId, CompletableFuture.completedFuture(
            new ElicitationInfo(elicitationId, sessionId, organizationId, backendEndpoint, secretEntry)));
      return elicitationId;
   }

   /**
    * Retrieve elicitation information for a given session id.
    * @param elicitationId The elicitation id to retrieve information for
    * @return the value to which the specified key is mapped, or null if this cache does not contain a mapping for the key
    */
   @Nullable
   public ElicitationInfo getElicitationInfo(String elicitationId) {
      logger.tracef("Retrieving elicitation information for elicitation id '%s'", elicitationId);
      CompletableFuture<ElicitationInfo> future = elicitationCache.as(CaffeineCache.class).getIfPresent(elicitationId);
      return future != null ? future.join() : null;
   }

   /**
    * Retrieve elicitation information for a given session id.
    * @param elicitationId The elicitation id to retrieve information for
    * @return the future value to which the specified key is mapped, or null if this cache does not contain a mapping for the key
    */
   @Nullable
   public CompletableFuture<ElicitationInfo> getElicitationInfoFuture(String elicitationId) {
      logger.tracef("Retrieving elicitation information future for elicitation id '%s'", elicitationId);
      return elicitationCache.as(CaffeineCache.class).getIfPresent(elicitationId);
   }

   /**
    * Remove elicitation information for a given elicitation id.
    * @param elicitationId The elicitation id to remove information for
    */
   public void removeElicitationInfo(String elicitationId) {
      logger.debugf("Removing elicitation information for elicitation id '%s'", elicitationId);
      elicitationCache.as(CaffeineCache.class).invalidate(elicitationId);
   }
}
