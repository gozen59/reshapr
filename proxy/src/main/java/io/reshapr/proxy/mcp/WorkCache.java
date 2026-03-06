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
package io.reshapr.proxy.mcp;

import io.reshapr.proxy.util.LRUCache;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

/**
 * A configurable basic working cache for MCP operations.
 * @author laurent
 */
@ApplicationScoped
public class WorkCache {

   /** Get a JBoss logging logger. */
   private final Logger logger = Logger.getLogger(getClass());

   private final LRUCache<String, Object> cache;

   /**
    * Build a working cache from a LRU cache with maximum capacity.
    * @param cacheSize The maximum capacity/size of the working cache.
    */
   public WorkCache(@ConfigProperty(name = "reshapr.gateway.mcp.cache.size") int cacheSize){
      this.cache = new LRUCache<>(cacheSize);
   }

   public void set(String key, Object value) {
      cache.put(key, value);
   }

   public Object get(String key) {
      logger.tracef("Looking for key '%s'", key);
      return cache.get(key);
   }

   public int size() {
      return cache.size();
   }

   public boolean isEmpty() {
      return cache.isEmpty();
   }

   public void clear() {
      cache.clear();
   }
}
