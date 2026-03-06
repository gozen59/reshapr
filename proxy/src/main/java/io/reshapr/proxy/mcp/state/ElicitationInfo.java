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

/**
 * Holds information about an elicitation process.
 * @author laurent
 */
public class ElicitationInfo {

   private final String id;
   private final String sessionId;
   private final String organizationId;
   private final String backendEndpoint;
   private final SecretEntry secretEntry;

   public ElicitationInfo(String id, String sessionId, String organizationId, String backendEndpoint, SecretEntry secretEntry) {
      this.id = id;
      this.sessionId = sessionId;
      this.organizationId = organizationId;
      this.backendEndpoint = backendEndpoint;
      this.secretEntry = secretEntry;
   }

   public String getId() {
      return id;
   }
   public String getSessionId() {
      return sessionId;
   }
   public String getOrganizationId() {
      return organizationId;
   }
   public String getBackendEndpoint() {
      return backendEndpoint;
   }
   public SecretEntry getSecretEntry() {
      return secretEntry;
   }
}
