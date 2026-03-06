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
package io.reshapr.proxy.mcp;

import org.junit.jupiter.api.Test;

class ReshaprResourcesMcpResourceBuilderTest {

   @Test
   void testResourceTemplates() {
      String uri = "file:///project/src/main.rs?mode=raw";
      String uriTemplate = "file:///project/src/{path}?mode=raw";

      String uriPattern = uriTemplate.replace("/", "\\/")
            .replace("?", "\\?")
            .replaceAll("\\{(.*)\\}", "(.*)");

      System.err.println("uriPattern: " + uriPattern);

      boolean matches = uri.matches(uriPattern);
      System.err.println("matches: " + matches);

      // Extract group 1.
      if (matches) {
         String extractedPath = uri.replaceAll(uriPattern, "$1");
         System.err.println("extractedPath: " + extractedPath);
      }
   }
}
