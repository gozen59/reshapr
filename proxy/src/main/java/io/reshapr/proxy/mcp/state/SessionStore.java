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

import io.reshapr.proxy.context.SessionInfo;

import io.quarkus.cache.Cache;
import io.quarkus.cache.CacheName;
import io.quarkus.cache.CaffeineCache;
import jakarta.annotation.Nullable;
import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.logging.Logger;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Store for managing session information within the Reshapr MCP.
 * This store allows initializing new sessions and retrieving session information based on session IDs.
 * @author laurent
 */
@ApplicationScoped
public class SessionStore {

   /** Get a JBoss logging logger. */
   private final Logger logger = Logger.getLogger(getClass());

   private final Cache sessionCache;

   /**
    * Create a new SessionStore with a cache for session information.
    * @param sessionCache The cache to use for storing session information
    */
   public SessionStore(@CacheName("session-store") Cache sessionCache) {
      this.sessionCache = sessionCache;
   }

   /**
    * Initialize a new session for a given service id.
    * @param serviceId The service id for which to initialize the session
    * @param protocolVersion The protocol version used in this session
    * @return The newly created session id
    */
   public String initializeSession(String serviceId, String protocolVersion) {
      String sessionId = UUID.randomUUID().toString();
      logger.debugf("Initializing new session with id '%s' for service '%s'", sessionId, serviceId);
      sessionCache.as(CaffeineCache.class).put(sessionId, CompletableFuture.completedFuture(
            new SessionInfo(sessionId, serviceId, protocolVersion)));
      return sessionId;
   }

   /**
    * Retrieve session information for a given session id.
    * @param sessionId The session id to retrieve information for
    * @return the value to which the specified key is mapped, or null if this cache does not contain a mapping for the key
    */
   @Nullable
   public SessionInfo getSessionInfo(String sessionId) {
      logger.tracef("Retrieving session information for session id '%s'", sessionId);
      CompletableFuture<SessionInfo> future = sessionCache.as(CaffeineCache.class).getIfPresent(sessionId);
      return future != null ? future.join() : null;
   }

   /**
    * Retrieve session information for a given session id.
    * @param sessionId The session id to retrieve information for
    * @return the future value to which the specified key is mapped, or null if this cache does not contain a mapping for the key
    */
   @Nullable
   public CompletableFuture<SessionInfo> getSessionInfoFuture(String sessionId) {
      logger.tracef("Retrieving session information future for session id '%s'", sessionId);
      return sessionCache.as(CaffeineCache.class).getIfPresent(sessionId);
   }

   /**
    * Update session information for a given session id.
    * @param sessionId The session id to update information for
    * @param sessionInformation The new session information to store
    */
   public void updateSessionInfo(String sessionId, SessionInfo sessionInformation) {
      logger.debugf("Updating session information for session id '%s'", sessionId);
      sessionCache.as(CaffeineCache.class).put(sessionId, CompletableFuture.completedFuture(sessionInformation));
   }
}
