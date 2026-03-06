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
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import org.hibernate.annotations.Type;

import java.time.LocalDateTime;
import java.util.List;

/**
 * A gateway that is currently running and reporting health to the Reshapr control plane.
 * @author laurent
 */
@Entity
@Table(name = "gateways", uniqueConstraints =
   @jakarta.persistence.UniqueConstraint(columnNames = {"name", "organization_id"}))
public class Gateway extends TenantAwareEntity {

   @Column(nullable = false)
   public String name;

   @Temporal(TemporalType.TIMESTAMP)
   @Column(name = "started_at", nullable = false, columnDefinition = "TIMESTAMP")
   public LocalDateTime startedAt;

   @Temporal(TemporalType.TIMESTAMP)
   @Column(name = "last_heartbeat", nullable = false, columnDefinition = "TIMESTAMP")
   public LocalDateTime lastHeartbeat;

   @Type(JsonType.class)
   @Column(columnDefinition = "JSONB")
   public List<String> fqdns;

   @ManyToMany
   @JoinTable(
         name = "gateways_gateway_groups",
         joinColumns = @JoinColumn(name = "gateway_id"),
         inverseJoinColumns = @JoinColumn(name = "gateway_group_id"))
   public List<GatewayGroup> gatewayGroups;
}
