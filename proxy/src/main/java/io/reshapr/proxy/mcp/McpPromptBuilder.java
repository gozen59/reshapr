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

import java.util.List;

/**
 * Interface defining a builder for MCP Prompts.
 * @author laurent
 */
public interface McpPromptBuilder {

   /**
    * List all available prompts.
    * @return A list of MCP Prompt objects
    */
   List<McpSchema.Prompt> listPrompts();

   /**
    * Get a specific prompt by its name.
    * @param request The simple request containing the prompt name
    * @return The MCP Prompt object
    */
   McpSchema.PromptMessage getPrompt(McpSchema.SimpleRequest request);
}
