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

import io.reshapr.ctrl.model.Organization;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
/**
 * Repository for managing organization in the Reshapr control plane.
 * @author laurent
 */
@ApplicationScoped
public class OrganizationRepository implements PanacheRepositoryBase<Organization, String> {

   public Organization findByName(String name) {
      return find("name", name).firstResult();
   }

   public List<Organization> findByNames(List<String> names) {
      return list("where name in ?1", names);
   }
}
