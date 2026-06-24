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

import java.time.OffsetDateTime;

/**
 * Data Transfer Object (DTO) for Initial Access Token in the Reshapr control plane.
 * @param id The unique identifier of the initial access token in database
 * @param organizationId The organization ID associated with the token
 * @param iatId The ID of the initial access token as provided by the identity provider
 * @param token The actual initial access token string
 * @param serviceId The service ID for which the token was created
 * @param issuedAt The timestamp when the token was issued
 */
@RegisterForReflection
public record InitialAccessTokenDTO(
      String id,
      String organizationId,
      String iatId,
      String token,
      String serviceId,
      OffsetDateTime issuedAt
) {
}
