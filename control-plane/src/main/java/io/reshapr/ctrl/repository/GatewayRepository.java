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
package io.reshapr.ctrl.repository;

import io.reshapr.ctrl.model.Gateway;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for managing gateways in the Reshapr control plane.
 * @author laurent
 */
@ApplicationScoped
public class GatewayRepository implements PanacheRepositoryBase<Gateway, String> {

   public Optional<Gateway> findByName(String name) {
      return find("name", name).singleResultOptional();
   }

   /**
    * List all gateways for the current tenant, eagerly fetching their gateway groups so the
    * collection can be safely mapped to DTOs outside of the persistence context.
    * @return the list of gateways with their gateway groups initialized
    */
   public List<Gateway> listAllWithGroups() {
      return find("select distinct g FROM Gateway g left join fetch g.gatewayGroups").list();
   }

   public List<Gateway> findAllWithHeartbeatBefore(LocalDateTime lastHeartbeat) {
      return find("lastHeartbeat <= ?1", lastHeartbeat).list();
   }
}
