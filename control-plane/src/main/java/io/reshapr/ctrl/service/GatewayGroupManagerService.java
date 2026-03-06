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
package io.reshapr.ctrl.service;

import io.reshapr.ctrl.model.GatewayGroup;
import io.reshapr.ctrl.model.SharedResource;
import io.reshapr.ctrl.model.SharedResourceTypes;
import io.reshapr.ctrl.repository.GatewayGroupRepository;
import io.reshapr.ctrl.security.ReshaprTenantResolver;

import io.vertx.mutiny.core.Vertx;
import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.logging.Logger;

import java.util.List;

@ApplicationScoped
public class GatewayGroupManagerService {

   /** Get a JBoss logging logger. */
   private final Logger logger = Logger.getLogger(getClass());

   private final GatewayGroupRepository gatewayGroupRepository;

   /**
    *
    * @param gatewayGroupRepository
    */
   public GatewayGroupManagerService(GatewayGroupRepository gatewayGroupRepository) {
      this.gatewayGroupRepository = gatewayGroupRepository;
   }

   public List<GatewayGroup> getOwnedGatewayGroups() {
      logger.debug("Retrieving gateway groups owned by the current tenant");
      String organizationId = Vertx.currentContext().getLocal(ReshaprTenantResolver.TENANT_ID_CONTEXT_KEY);
      return gatewayGroupRepository.find("organizationId", organizationId).list();
   }

   public List<GatewayGroup> getAvailableGatewayGroups() {
      logger.debug("Retrieving gateway groups available to the current tenant");
      String organizationId = Vertx.currentContext().getLocal(ReshaprTenantResolver.TENANT_ID_CONTEXT_KEY);
      List<SharedResource> sharedResources = SharedResource.findByTypeAndOrganizationId(SharedResourceTypes.GATEWAY_GROUP, organizationId);

      // Flatten the list of available gateway group IDs from shared resources.
      List<String> gatewayGroupIds = sharedResources.stream()
            .map(SharedResource::getResourceIds)
            .flatMap(List::stream)
            .toList();

      return gatewayGroupRepository.findOwnedAndWithIds(organizationId, gatewayGroupIds);
   }
}
