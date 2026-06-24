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

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * A service that can be exposed via Reshapr gateways.
 * @author laurent
 */
@Entity
@Table(name = "services", uniqueConstraints = {
      @UniqueConstraint(columnNames = {"name", "version", "organization_id"})
})
public class Service extends TenantAwareEntity {

   @Column(nullable = false)
   public String name;

   @Column(nullable = false)
   public String version;

   @Column(name = "created_on", nullable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
   public OffsetDateTime createdOn;

   @Enumerated(EnumType.STRING)
   public ServiceType type;

   @ElementCollection()
   @CollectionTable(name = "services_operations", joinColumns = @JoinColumn(name = "service_id"))
   @Fetch(FetchMode.JOIN)
   public List<Operation> operations;
}
