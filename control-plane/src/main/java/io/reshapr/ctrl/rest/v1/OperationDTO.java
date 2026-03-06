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
 * ServiceDTO is a Data Transfer Object representing a service in the Reshapr control plane.
 * @param name The name of the operation.
 * @param method The HTTP method used for the operation (e.g., GET, POST).
 * @param action The action to be performed by the operation (e.g., "create", "update", "delete").
 * @param inputName The name of the input parameter for the operation.
 * @param outputName The name of the output parameter for the operation.
 */
public record OperationDTO(
      String name,
      String method,
      String action,
      String inputName,
      String outputName) {
}
