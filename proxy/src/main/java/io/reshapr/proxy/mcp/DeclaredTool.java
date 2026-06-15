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

import jakarta.annotation.Nullable;

import java.util.Objects;

/**
 * A reference to a tool an operation declares it may call. It acts as a security allow-list and as
 * the input to the elicitation pre-flight. Although currently produced by custom-tool scripts, the
 * concept is intentionally generic and not tied to scripting.
 * @param serviceCoordinate The target service as the readable couple {@code <service_name:service_version>},
 *                          or {@code null}/blank for a tool of the same service as the declaring operation.
 * @param tool The name of the tool that may be called.
 * @author laurent
 */
public record DeclaredTool(@Nullable String serviceCoordinate, String tool) {

   /** Whether this declared tool targets the declaring operation's own service. */
   public boolean isSameService() {
      return serviceCoordinate == null || serviceCoordinate.isBlank();
   }

   /** Whether the declared tool matches the given service coordinate and tool name. */
   public static boolean matches(DeclaredTool declared, @Nullable String serviceCoordinate, String tool) {
      boolean sameTool = declared.tool().equals(tool);
      if (!sameTool) {
         return false;
      }
      boolean callIsSameService = serviceCoordinate == null || serviceCoordinate.isBlank();
      if (declared.isSameService() || callIsSameService) {
         return declared.isSameService() && callIsSameService;
      }
      return Objects.equals(declared.serviceCoordinate(), serviceCoordinate);
   }
}

