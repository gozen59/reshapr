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
package io.reshapr.ctrl.rest.admin;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.quarkus.runtime.annotations.RegisterForReflection;
import io.reshapr.json.HtmlEncodedStringDeserializer;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * Data Transfer Object (DTO) for a service account in the Reshapr control plane.
 * @param name The name of the service account.
 * @param description The description of the service account.
 * @param k8sSubject The Kubernetes subject associated with the service account.
 *                   This is typically a Kubernetes service account name.
 * @param allowedOrganizations The list of organizations allowed to use this service account.
 *                             If contains a simple '*' then the service account is available to all organizations.
 * @param validityDays The number of days the service account is valid for.
 */
@RegisterForReflection
public record ServiceAccountRequestDTO(
      @Size(max = 255, message = "Name must not exceed 255 characters")
      @JsonDeserialize(using = HtmlEncodedStringDeserializer.class)
      String name,
      @Size(max = 255, message = "Description must not exceed 255 characters")
      @JsonDeserialize(using = HtmlEncodedStringDeserializer.class)
      String description,
      @Size(max = 255, message = "k8sSubject must not exceed 255 characters")
      @JsonDeserialize(using = HtmlEncodedStringDeserializer.class)
      String k8sSubject,
      List<String> allowedOrganizations,
      int validityDays
) {
}
