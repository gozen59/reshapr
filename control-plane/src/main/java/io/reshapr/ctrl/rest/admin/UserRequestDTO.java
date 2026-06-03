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

import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Data Transfer Object for User information.
 * @param username The username of the user
 * @param email The email of the user
 */
@RegisterForReflection
public record UserRequestDTO(
      @NotBlank(message = "User name must not be blank")
      @Size(min = 1, max = 100, message = "User name must be between 1 and 100 characters")
      String username,
      @Email(message = "Email should be valid")
      @Size(max = 255, message = "Email must not exceed 255 characters")
      String email,
      @Size(max = 255, message = "Password must not exceed 255 characters")
      String password,
      String firstname,
      String lastname
) {
}
