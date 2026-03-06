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

import java.io.File;

/**
 * Represents information about a Specification artifact in the Reshapr control plane.
 * This record is used to encapsulate the name and whether it is the main artifact.
 * @param name The name of the artifact.
 * @param specificationFile The file holding the content of the specification.
 * @param mainArtifact Indicates if this artifact is the main one.
 */
public record SpecificationArtifactInfo(
      String name,
      File specificationFile,
      boolean mainArtifact) {
}
