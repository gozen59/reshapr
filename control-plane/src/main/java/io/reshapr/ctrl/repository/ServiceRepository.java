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

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import io.reshapr.ctrl.model.Service;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * Repository for managing Services in the Reshapr control plane.
 * @author laurent
 */
@ApplicationScoped
public class ServiceRepository implements PanacheRepositoryBase<Service, String> {

   public Service findByNameAndVersion(String name, String version) {
      return find("name = ?1 and version = ?2", name, version).firstResult();
   }

   public Service findByIdWithOperations(String id) {
      return find("select distinct s from Service s left join fetch s.operations where s.id = ?1", id).firstResult();
   }
}
