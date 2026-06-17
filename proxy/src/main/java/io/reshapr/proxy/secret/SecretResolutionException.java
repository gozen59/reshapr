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
package io.reshapr.proxy.secret;

/**
 * Exception thrown when a secret reference (e.g. {@code ${env:MY_VAR}}) cannot be
 * resolved locally on the gateway.
 * @author laurent
 */
public class SecretResolutionException extends RuntimeException {

   /**
    * @param message A human-readable message describing the resolution failure. It must
    *                never contain a resolved secret value.
    */
   public SecretResolutionException(String message) {
      super(message);
   }
}

