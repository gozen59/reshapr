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

/**
 * Data Transfer Object (DTO) for an API token creation request in the Reshapr control plane.
 * @param name the name of the API token to create
 * @param validityDays the number of days the token should be valid
 */
@RegisterForReflection
public record ApiTokenRequestDTO(
      String name,
      ValidityPeriodEnum validityDays) {
}

