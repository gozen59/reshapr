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
import io.reshapr.proxy.mcp.WorkCache;
import io.reshapr.proxy.proxy.ProxyService;
import io.reshapr.proxy.registry.ArtifactEntry;
import io.reshapr.proxy.registry.ArtifactEntryType;
import io.reshapr.proxy.registry.OperationEntry;
import io.reshapr.proxy.registry.ServiceEntry;
import io.reshapr.proxy.secret.SecretReferenceResolver;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * This is a test case for OpenAPIMcpToolConverter.
 * @author laurent
 */
class OpenAPIMcpToolConverterTest {

   @Test
   void testOpenAPIWIthRecurseRefConversion() throws Exception {
      String specification = FileUtils.readFileToString(
            new File("target/test-classes/io/reshapr/proxy/mcp/trade-api-3.0.1-openapi.yaml"),
            StandardCharsets.UTF_8);

      ArtifactEntry artifactEntry = new ArtifactEntry("1", "trade-api-3.0.1-openapi.yaml",
            "GRPC", ArtifactEntryType.OPEN_API_SPEC, true, specification);

      List<OperationEntry> operations = List.of(
//            new OperationEntry("GET /customers", "GET", null, null, null),
//            new OperationEntry("GET /customers/{customerId}", "GET", null, null, null),
//            new OperationEntry("GET /accounts", "GET", null, null, null),
//            new OperationEntry("GET /accounts/{accountId}", "GET", null, null, null),
//            new OperationEntry("GET /orders", "GET", null, null, null),
            new OperationEntry("POST /orders", "POST", null, null, null)
      );

      ServiceEntry serviceEntry = new ServiceEntry("1", "reshapr", "Trade API",
      "3.0.1", "REST", operations);

      ObjectMapper objectMapper = new ObjectMapper();

      OpenAPIMcpToolConverter converter = new OpenAPIMcpToolConverter(serviceEntry, artifactEntry, null,
            new WorkCache(1000), objectMapper, new ProxyService(new SecretReferenceResolver(java.util.List.of())));

      for (OperationEntry operation : operations) {
         McpSchema.JsonSchema schema = converter.getInputSchema(operation);
         if ("POST /orders".equals(operation.name())) {
            // The underlyingFinancialInstrument on FinancialInstrument is a recursive $ref
            // and should be removed by the converter.
            String schemaStr = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(schema);
            assertFalse(schemaStr.contains("underlyingFinancialInstrument"));
         }
      }
   }
}
