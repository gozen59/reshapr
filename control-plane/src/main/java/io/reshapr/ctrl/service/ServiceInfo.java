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
package io.reshapr.ctrl.service;

import io.quarkus.runtime.annotations.RegisterForReflection;

import java.util.List;

/**
 * Represents information about a Service for the ServiceManager service.
 * This record is used to encapsulate the name and version of the service.
 * @param name The name of the service.
 * @param version The version of the service.
 */
@RegisterForReflection
public record ServiceInfo(
      String name,
      String version,
      List<String> includedOperations,
      List<String> excludedOperations) {
}
