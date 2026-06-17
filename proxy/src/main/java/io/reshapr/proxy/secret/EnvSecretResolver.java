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

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.ConfigProvider;

/**
 * {@link SecretResolver} resolving {@code ${env:VAR}} references from the gateway's
 * environment. Environment variables take precedence; as a fallback the value is looked
 * up through MicroProfile Config (which also covers system properties, {@code .env} files
 * and {@code application.properties}).
 * <p>
 * Because the reference is resolved at call time, rotating the underlying value is picked
 * up without redeploying the gateway.
 * @author laurent
 */
@ApplicationScoped
public class EnvSecretResolver implements SecretResolver {

   /** The scheme handled by this resolver. */
   public static final String SCHEME = "env";

   @Override
   public String scheme() {
      return SCHEME;
   }

   @Override
   public String resolve(String reference) {
      String value = System.getenv(reference);
      if (value == null) {
         value = ConfigProvider.getConfig().getOptionalValue(reference, String.class).orElse(null);
      }
      if (value == null) {
         throw new SecretResolutionException(
               "Environment variable '" + reference + "' is not defined on this gateway");
      }
      return value;
   }
}

