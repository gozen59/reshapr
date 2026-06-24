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
package io.reshapr.ctrl.control;

import io.reshapr.ctrl.security.ReshaprTenantContext;
import io.reshapr.ctrl.service.GatewayManagerService;

import io.quarkus.arc.Unremovable;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.logging.Logger;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;

@Unremovable
@ApplicationScoped
public class GatewayRegistrationCleaner {

   /** Get a JBoss logging logger. */
   private final Logger logger = Logger.getLogger(getClass());

   private final GatewayManagerService gatewayManagerService;

   /**
    *
    * @param gatewayManagerService
    */
   public GatewayRegistrationCleaner(GatewayManagerService gatewayManagerService) {
      this.gatewayManagerService = gatewayManagerService;
   }

   @Scheduled(every = "5m")
   public void cleanExpiredGatewayRegistrations() {
      logger.debugf("Triggering expired gateway registrations check...");
      // Set the tenant context to "reshapr" to include all gateways from all organizations.
      ReshaprTenantContext.setCurrentTenant("reshapr");
      OffsetDateTime lastHeartbeatThreshold = OffsetDateTime.ofInstant(Instant.ofEpochMilli(System.currentTimeMillis() - 5 * 60 * 1000),
            ZoneId.systemDefault()); // 5 minutes ago
      gatewayManagerService.cleanExpiredRegistrations(lastHeartbeatThreshold);
   }
}
