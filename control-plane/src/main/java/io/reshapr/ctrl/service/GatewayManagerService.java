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

import io.reshapr.ctrl.model.Gateway;
import io.reshapr.ctrl.model.GatewayGroup;
import io.reshapr.ctrl.repository.GatewayRepository;

import io.quarkus.grpc.GrpcService;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import org.jboss.logging.Logger;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class GatewayManagerService {

   /** Get a JBoss logging logger. */
   private final Logger logger = Logger.getLogger(getClass());

   private final GatewayRepository gatewayRepository;
   private final ExpositionDiscoveryServiceHandler expositionDiscoveryServiceHandler;

   /**
    * Constructor for GatewayManagerService.
    * @param gatewayRepository the repository to manage gateways
    */
   public GatewayManagerService(GatewayRepository gatewayRepository,
                                @GrpcService ExpositionDiscoveryServiceHandler expositionDiscoveryServiceHandler) {
      this.gatewayRepository = gatewayRepository;
      this.expositionDiscoveryServiceHandler = expositionDiscoveryServiceHandler;
   }

   @Transactional
   public void registerGateway(String gatewayName, List<GatewayGroup> matchingGroups, List<String> fqdns) {
      logger.infof("Registering gateway with name: '%s'", gatewayName);

      Optional<Gateway> gatewayOpt = gatewayRepository.findByName(gatewayName);
      Gateway gateway = gatewayOpt.orElseGet(() -> {
         logger.infof("Gateway with ID %s not found, creating a new one", gatewayName);
         Gateway newGateway = new Gateway();
         newGateway.name = gatewayName;
         newGateway.startedAt = LocalDateTime.now();
         return newGateway;
      });

      gateway.lastHeartbeat = LocalDateTime.now();
      gateway.fqdns = fqdns;
      gateway.gatewayGroups = matchingGroups;
      gatewayRepository.persist(gateway);
   }

   @Transactional
   public boolean updateGatewayHeartbeat(String gatewayName) {
      logger.infof("Updating heartbeat for gateway with name: '%s'", gatewayName);

      Optional<Gateway> gatewayOpt = gatewayRepository.findByName(gatewayName);
      if (gatewayOpt.isEmpty()) {
         logger.warnf("Gateway with ID %s not found", gatewayName);
         return false;
      }

      Gateway gateway = gatewayOpt.get();
      gateway.lastHeartbeat = LocalDateTime.now();
      gatewayRepository.persist(gateway);
      return true;
   }

   @Transactional
   public void unregisterGateway(String gatewayName) {
      logger.infof("Unregistering gateway with name: '%s'", gatewayName);

      Optional<Gateway> gatewayOpt = gatewayRepository.findByName(gatewayName);
      if (gatewayOpt.isEmpty()) {
         logger.warnf("Gateway with ID %s not found", gatewayName);
         return;
      }
      Gateway gateway = gatewayOpt.get();
      // Clear exposition observers for this gateway to avoid trying to send updates to a non-existing gateway.
      expositionDiscoveryServiceHandler.clearObserver(gateway.organizationId, gatewayName);
      gateway.delete();
   }

   @Transactional
   public void cleanExpiredRegistrations(LocalDateTime beforeDate) {
      List<Gateway> gateways = gatewayRepository.findAllWithHeartbeatBefore(beforeDate);
      if (!gateways.isEmpty()) {
         logger.infof("Cleaning %d expired gateway registrations", gateways.size());
      }
      gateways.forEach(PanacheEntityBase::delete);
   }
}
