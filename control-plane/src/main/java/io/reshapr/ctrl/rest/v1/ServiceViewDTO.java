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
package io.reshapr.ctrl.rest.v1;

import io.reshapr.ctrl.model.ServiceType;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * ServiceViewDTO is a Data Transfer Object representing a service in the Reshapr control plane.
 * @param id The unique identifier of the service.
 * @param organizationId The identifier of the organization that owns the service.
 * @param name The name of the service.
 * @param version The version of the service.
 * @param createdOn The creation data of the service.
 * @param type The type of the service, such as REST, GraphQL, or gRPC.
 * @param operations A list of operations associated with the service, each represented by an OperationDTO.
 */
public record ServiceViewDTO(
      String id,
      String organizationId,
      String name,
      String version,
      OffsetDateTime createdOn,
      ServiceType type,
      List<OperationDTO> operations) {
}
