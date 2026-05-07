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
package io.reshapr.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.runtime.annotations.RegisterForReflection;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

/**
 * Utils for dealing with OpenIO Connect protocol.
 * @author laurent
 */
public class OidcUtils {

   private OidcUtils() {
      // Hide default no argument constructor as it's a utility class.'
   }

   /**
    * Exchange an authorization code for an access token.
    * @param oidcEndpointConfig the OIDC endpoint configuration to use for the exchange.
    * @param objectMapper the ObjectMapper to use for JSON parsing.
    * @param authorizationCode the authorization code to exchange.
    * @param redirectUri the redirect uri used for the exchange.
    * @return
    */
   public static String exchangeAuthorizationCode(OidcEndpointConfig oidcEndpointConfig, ObjectMapper objectMapper,
                                                  String authorizationCode, String redirectUri) throws AuthenticationException {
      // Build the request to the token endpoint.
      HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
            .uri(URI.create(oidcEndpointConfig.endpointUrl()))
            .method("POST",
                  HttpRequest.BodyPublishers.ofString(getAuthorizationCodeQueryString(oidcEndpointConfig, authorizationCode, redirectUri)))
            .header("Content-Type", "application/x-www-form-urlencoded");

      try (HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(3))
            .version(HttpClient.Version.HTTP_1_1).build()) {

         // Send the request to token endpoint.
         HttpResponse<String> response = httpClient.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString());

         if (response.statusCode() != 200) {
            throw new AuthenticationException("OAuth2 token endpoint returned error " + response.statusCode());
         }

         // Now parse the response to extract the access token.
         JsonNode jsonResponse = objectMapper.readTree(response.body());
         return jsonResponse.get("access_token").asText();
      } catch (Exception e) {
         if (!(e instanceof AuthenticationException)) {
            throw new AuthenticationException("Failed to exchange authorization code: " + e.getMessage());
         }
         throw (AuthenticationException)e;
      }
   }

   private static String getAuthorizationCodeQueryString(OidcEndpointConfig oidcEndpointConfig, String authorizationCode, String redirectUri) {
      StringBuilder queryString = new StringBuilder("grant_type=authorization_code");
      queryString.append("&code=").append(authorizationCode);
      queryString.append("&redirect_uri=").append(URLEncoder.encode(redirectUri, StandardCharsets.UTF_8));
      queryString.append("&client_id=").append(oidcEndpointConfig.clientId());
      if (oidcEndpointConfig.clientSecret() != null) {
         queryString.append("&client_secret=").append(oidcEndpointConfig.clientSecret());
      }
      return queryString.toString();
   }

   @RegisterForReflection
   public record OidcEndpointConfig(String endpointUrl, String clientId, String clientSecret) {
   }
}
