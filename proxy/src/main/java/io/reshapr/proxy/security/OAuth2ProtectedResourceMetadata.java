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
package io.reshapr.proxy.security;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.quarkus.runtime.annotations.RegisterForReflection;

import java.util.List;

/**
 * OAuth2ProtectedResourceMetadata is a record that holds metadata about an OAuth2 protected resource.
 * @author laurent
 */
@RegisterForReflection
public record OAuth2ProtectedResourceMetadata(
      @JsonProperty("resource") String resource,
      @JsonProperty("authorization_servers") List<String> authorizationServers,
      @JsonProperty("jwks_uri") String jwksUri,
      @JsonProperty("scopes_supported") List<String> supportedScopes
) {
}
