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

import io.quarkus.runtime.annotations.RegisterForReflection;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Data Transfer Object (DTO) for a gateway view in the Reshapr control plane.
 * @param id The unique identifier of the gateway.
 * @param organizationId The identifier of the organization that owns the gateway.
 * @param name The name of the gateway.
 * @param version The version of the gateway runtime advertised during registration.
 * @param startedAt The timestamp when the gateway was started.
 * @param lastHeartbeat The timestamp of the last heartbeat received from the gateway.
 * @param fqdns The list of fully qualified domain names associated with the gateway.
 * @param labels The labels advertised by the gateway during registration.
 * @param gatewayGroups The gateway groups the gateway is mapped to.
 */
@RegisterForReflection
public record GatewayViewDTO(
      String id,
      String organizationId,
      String name,
      String version,
      LocalDateTime startedAt,
      LocalDateTime lastHeartbeat,
      List<String> fqdns,
      Map<String, String> labels,
      List<GatewayGroupDTO> gatewayGroups) {
}
