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
package io.reshapr.ctrl.model;

import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.Cacheable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.QueryHint;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import org.hibernate.annotations.Type;

import java.util.List;

@Entity
@Cacheable
@Table(name = "shared_resources", uniqueConstraints = {
      @UniqueConstraint(columnNames = {"type", "organization_id"})
})
@NamedQueries({
      @NamedQuery(name = "SharedResource.findByTypeAndOrganization",
            query = "from SharedResource where type = ?1 and (organizationId = ?2 or organizationId is null)",
            hints = @QueryHint(name = "org.hibernate.cacheable", value = "true") )
})
public class SharedResource extends BaseEntity {

   @Column(name = "organization_id")
   public String organizationId;
   public String type;

   @Type(JsonType.class)
   @Column(columnDefinition = "JSONB", name = "resource_ids")
   public List<String> resourceIds;

   public List<String> getResourceIds() {
      return resourceIds;
   }

   public static List<SharedResource> findByTypeAndOrganizationId(String type, String organizationId) {
      return find("#SharedResource.findByTypeAndOrganization", type, organizationId).list();
   }
}
