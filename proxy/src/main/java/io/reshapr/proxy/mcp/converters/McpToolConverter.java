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
package io.reshapr.proxy.mcp.converters;

import io.reshapr.proxy.mcp.McpSchema;
import io.reshapr.proxy.proxy.BackendResponse;
import io.reshapr.proxy.proxy.ContentUtil;
import io.reshapr.proxy.registry.*;

import io.smallrye.mutiny.Uni;
import jakarta.annotation.Nullable;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Utility base class for converting a Reshapr Service and Operation into an MCP Tool.
 * @author laurent
 */
public abstract class McpToolConverter {


   /**
    * Get the list of available operations on this converter for the given service.
    * @param service The service to extract operations from.
    * @return The list of available operations.
    */
   public List<OperationEntry> getAvailableOperations(ServiceEntry service) {
      return service.operations();
   }

   /**
    * Extract the name of the tool from the operation.
    * @param operation The operation to extract the name from.
    * @return The tool name
    */
   public String getToolName(OperationEntry operation) {
      return operation.name();
   }

   /**
    * Extract the description of the tool from the operation.
    * @param operation The operation to extract the description from.
    * @return The tool description
    */
   public abstract String getToolDescription(OperationEntry operation);

   /**
    * Extract the metadata of the tool from the operation.
    * @param registry  The gateway registry to use for resource lookup.
    * @param service   The service to which the operation belongs.
    * @param operation The operation to extract the metadata from.
    * @return The tool metadata map
    */
   public @Nullable Map<String, Object> getToolMetadata(GatewayRegistry registry, ServiceEntry service, OperationEntry operation) {
      // Only manage UI resource for now.
      ToolEntry tool = new ToolEntry(service.id(), service.organizationId(), getToolName(operation));
      ResourceEntry resource = registry.getResourceForTool(tool);
      if (resource != null) {
         return Map.of(
               "ui", Map.of("resourceUri", resource.resourceUri())
         );
      }
      return null;
   }

   /**
    * Extract the input schema of the tool from the operation.
    * @param operation The operation to extract the input schema from.
    * @return The tool input schema following the 2024-11-05 MCP spec
    */
   public abstract McpSchema.JsonSchema getInputSchema(OperationEntry operation);

   /**
    * Invoke the tool with the given request and return the response in Microcks domain object.
    * @param operation The operation to invoke the tool on.
    * @param configuration The configuration to apply when invoking the tool
    * @param request   The request to send to the tool.
    * @param headers   Simple representation of headers transmitted at the protocol level.
    * @return The response from the tool.
    */
   public abstract Response getCallResponse(OperationEntry operation, ConfigurationEntry configuration,
                                            McpSchema.SimpleRequest request, Map<String, List<String>> headers);

   /**
    * Invoke the tool with the given request and return the response in Microcks domain object.
    * @param operation The operation to invoke the tool on.
    * @param request   The request to send to the tool.
    * @param headers   Simple representation of headers transmitted at the protocol level.
    * @return The response from the tool.
    */
   public abstract Uni<Response> getCallResponseUni(OperationEntry operation, McpSchema.SimpleRequest request,
                                                 Map<String, List<String>> headers);


   /**
    * Response record holding content and fault indicator.
    * @param content the response content
    * @param isFault indicates whether response is a fault
    */
   public record Response(
         String content,
         boolean isFault
   ) {}

   /** Prepare the Http headers by sanitizing them. */
   protected Map<String, List<String>> sanitizeHttpHeaders(Map<String, List<String>> headers) {
      return headers.entrySet().stream().filter(entry -> !"content-length".equalsIgnoreCase(entry.getKey()))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
   }

   /** Depending on result encoding, extract the response content as a string. */
   protected String extractResponseContent(BackendResponse result) throws IOException {
      return ContentUtil.extractResponseContent(result);
   }
}
