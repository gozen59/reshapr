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
import io.reshapr.ctrl.model.SecretType;

/**
 * A record representing a secret in the Reshapr REST API v1.
 * @param id The unique identifier of the secret.
 * @param organizationId The identifier of the organization to which the secret belongs.
 * @param name The name of the secret.
 * @param description A description of the secret.
 * @param username The username associated with the secret.
 * @param password The password associated with the secret.
 * @param token An optional token associated with the secret.
 * @param tokenHeader An optional header for the token.
 * @param certPem An optional PEM-encoded certificate associated with the secret.
 * @param useElicitation A flag indicating whether elicitation is used for this secret.
 */
@RegisterForReflection
public record SecretDTO(
      String id,
      String organizationId,
      String name,
      String description,
      SecretType type,
      String username,
      String password,
      String token,
      String tokenHeader,
      String certPem,
      boolean useElicitation,
      OAuth2ClientConfigurationDTO oauth2ClientConfiguration) {
}
