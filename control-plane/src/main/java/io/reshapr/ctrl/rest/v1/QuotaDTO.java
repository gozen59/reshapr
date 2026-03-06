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

/**
 * Data Transfer Object (DTO) for a quota in the Reshapr control plane.
 * @param id The unique identifier of the quota.
 * @param organizationId The identifier of the organization to which the quota belongs.
 * @param metric The metric that the quota applies to, such as "requests" or "data transfer".
 * @param enabled Indicates whether the quota is currently enabled or not.
 * @param limit The maximum allowed usage for the specified metric within the quota period.
 * @param remaining The remaining usage available for the specified metric within the quota period.
 */
public record QuotaDTO(
         String id,
         String organizationId,
         String metric,
         boolean enabled,
         long limit,
         long remaining) {
}
