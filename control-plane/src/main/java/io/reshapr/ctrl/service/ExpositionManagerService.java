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

import io.reshapr.ctrl.control.QuotaRestricted;
import io.reshapr.ctrl.control.ReleaseQuota;
import io.reshapr.ctrl.model.ActiveExposition;
import io.reshapr.ctrl.model.ConfigurationPlan;
import io.reshapr.ctrl.model.Exposition;
import io.reshapr.ctrl.model.GatewayGroup;
import io.reshapr.ctrl.model.QuotaMetric;
import io.reshapr.ctrl.model.Service;
import io.reshapr.ctrl.repository.ActiveExpositionRepository;
import io.reshapr.ctrl.repository.ConfigurationPlanRepository;
import io.reshapr.ctrl.repository.ExpositionRepository;

import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import org.jboss.logging.Logger;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class ExpositionManagerService {

   /** Get a JBoss logging logger. */
   private final Logger logger = Logger.getLogger(getClass());

   private final ExpositionRepository expositionRepository;
   private final ConfigurationPlanRepository configurationPlanRepository;
   private final GatewayGroupManagerService gatewayGroupManagerService;
   private final ActiveExpositionRepository activeExpositionRepository;

   private final ClusterEventBroadcaster clusterEventBroadcaster;

   /**
    * Build a new instance of ExpositionManagerService with required dependencies.
    * @param expositionRepository
    * @param configurationPlanRepository
    * @param gatewayGroupManagerService
    * @param activeExpositionRepository
    * @param clusterEventBroadcaster
    */
   public ExpositionManagerService(ExpositionRepository expositionRepository,
                                   ConfigurationPlanRepository configurationPlanRepository,
                                   GatewayGroupManagerService gatewayGroupManagerService,
                                   ActiveExpositionRepository activeExpositionRepository,
                                   ClusterEventBroadcaster clusterEventBroadcaster) {
      this.expositionRepository = expositionRepository;
      this.configurationPlanRepository = configurationPlanRepository;
      this.gatewayGroupManagerService = gatewayGroupManagerService;
      this.activeExpositionRepository = activeExpositionRepository;
      this.clusterEventBroadcaster = clusterEventBroadcaster;
   }

   /**
    * Creates a new exposition for the given configuration plan and gateway group.
    * @param configurationPlanId the ID of the configuration plan
    * @param gatewayGroupId the ID of the gateway group
    * @return the created exposition
    */
   @Transactional
   @QuotaRestricted(metric = QuotaMetric.EXPOSITION_COUNT)
   public Exposition exposeConfiguration(String configurationPlanId, String gatewayGroupId) throws DependencyNotFoundException {
      logger.infof("Creating a new exposition for config plan '%s' on gateway group '%s'",
            configurationPlanId, gatewayGroupId);

      ConfigurationPlan configurationPlan = configurationPlanRepository.findById(configurationPlanId);
      if (configurationPlan == null) {
         logger.errorf("Configuration plan with id %s not found", configurationPlanId);
         throw new DependencyNotFoundException("Configuration plan with id " + configurationPlanId + " not found");
      }

      // Check gateway group existence and accessibility.
      Optional<GatewayGroup> gatewayGroup = gatewayGroupManagerService.getAvailableGatewayGroups().stream()
            .filter(availableGroup -> availableGroup.id.equals(gatewayGroupId))
            .findFirst();
      if (!gatewayGroup.isPresent()) {
         logger.errorf("Gateway group with id %s not found", gatewayGroupId);
         throw new DependencyNotFoundException("Gateway group with id " + gatewayGroupId + " not found");
      }

      // Create a new Exposition and associate it with the configuration plan and gateway group.
      Exposition exposition = new Exposition();
      exposition.configurationPlan = configurationPlan;
      exposition.gatewayGroup = gatewayGroup.get();
      exposition.service = configurationPlan.service;
      exposition.createdOn = OffsetDateTime.now();
      expositionRepository.persistAndFlush(exposition);

      // Publish the exposition creation event.
      clusterEventBroadcaster.publishExpositionCreationEvent(exposition, gatewayGroup.get());

      logger.debugf("Created exposition with id %s for gateway group %s", exposition.id, gatewayGroupId);
      return exposition;
   }

   /**
    * Lists all expositions for a given service or gateway group.
    * @param serviceId the ID of the service (optional)
    * @param gatewayGroupId the ID of the gateway group (optional)
    * @return a list of expositions
    */
   public List<Exposition> getExpositions(String serviceId, String gatewayGroupId) {
      logger.infof("Listing expositions for service '%s' and gateway group '%s'", serviceId, gatewayGroupId);
      if (serviceId != null) {
         return expositionRepository.findByServiceId(serviceId);
      }
      if (gatewayGroupId != null) {
         return expositionRepository.findByGatewayGroupId(gatewayGroupId);
      }
      return expositionRepository.findAll(Sort.ascending("createdOn")).list();
   }

   /**
    * Retrieves an exposition by its ID.
    * @param expositionId the ID of the exposition
    * @return the exposition, or null if not found
    */
   public Exposition getExposition(String expositionId) {
      logger.infof("Retrieving exposition with id '%s'", expositionId);
      return expositionRepository.loadById(expositionId).firstResult();
   }

   /**
    * Lists all active expositions.
    * @return a list of active expositions
    */
   public List<ActiveExposition> getActiveExpositions() {
      logger.info("Listing all active expositions");
      return activeExpositionRepository.findAll(Sort.ascending("createdOn")).list();
   }

   /**
    * Retrieves an active exposition by its ID.
    * @param expositionId the ID of the active exposition
    * @return the active exposition, or null if not found
    */
   public ActiveExposition getActiveExposition(String expositionId) {
      logger.infof("Retrieving active exposition with id '%s'", expositionId);
      return activeExpositionRepository.findById(expositionId);
   }

   public void propagateServiceChanges(Service service) {
      logger.infof("Propagating changes for service '%s'", service.id);
      List<Exposition> expositions = expositionRepository.findByServiceId(service.id);
      for (Exposition exposition : expositions) {
         logger.debugf("Updating exposition with id '%s' for service '%s'", exposition.id, service.id);
         clusterEventBroadcaster.publishExpositionUpdateEvent(exposition, exposition.gatewayGroup);
      }
   }

   public void propagateConfigurationPlanChanges(ConfigurationPlan configurationPlan) {
      logger.infof("Propagating changes for configuration plan '%s'", configurationPlan.id);
      List<Exposition> expositions = expositionRepository.findByConfigurationPlanId(configurationPlan.id);
      for (Exposition exposition : expositions) {
         logger.debugf("Updating exposition with id '%s' for configuration plan '%s'", exposition.id, configurationPlan.id);
         clusterEventBroadcaster.publishExpositionUpdateEvent(exposition, exposition.gatewayGroup);
      }
   }

   @Transactional
   public void removeConfigurationPlanExpositions(String configurationPlanId) {
      logger.infof("Removing all expositions for configuration plan '%s'", configurationPlanId);
      List<Exposition> expositions = expositionRepository.findByConfigurationPlanId(configurationPlanId);
      for (Exposition exposition : expositions) {
         logger.debugf("Removing exposition with id '%s' for configuration plan '%s'", exposition.id, configurationPlanId);
         doRemoveExposition(exposition);
      }
   }

   @Transactional
   public void removeExposition(String expositionId) throws DependencyNotFoundException {
      logger.infof("Removing exposition with id '%s'", expositionId);

      Exposition exposition = expositionRepository.findById(expositionId);
      if (exposition == null) {
         logger.errorf("Exposition with id %s not found", expositionId);
         throw new DependencyNotFoundException("Exposition with id " + expositionId + " not found");
      }
      doRemoveExposition(exposition);
   }

   @ReleaseQuota(metric = QuotaMetric.EXPOSITION_COUNT)
   protected void doRemoveExposition(Exposition exposition) {
      GatewayGroup gatewayGroup = exposition.gatewayGroup;

      // Remove the exposition and publish the deletion event.
      exposition.delete();
      clusterEventBroadcaster.publishExpositionDeletionEvent(exposition, gatewayGroup);
   }
}
