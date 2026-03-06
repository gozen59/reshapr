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

import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import io.quarkus.panache.common.Sort;
import io.reshapr.ctrl.model.Artifact;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

/**
 * Repository for managing artifacts in the Reshapr control plane.
 * @author laurent
 */
@ApplicationScoped
public class ArtifactRepository implements PanacheRepositoryBase<Artifact, String> {

   public List<Artifact> findByServiceIdAndSourceArtifact(String serviceId, String sourceArtifact) {
      return find("service.id = ?1 and sourcePath = ?2", serviceId, sourceArtifact).list();
   }

   public PanacheQuery<Artifact> findByServiceId(String serviceId) {
      return find("service.id", Sort.ascending("name"), serviceId);
   }
}
