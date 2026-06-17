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

import io.quarkus.arc.All;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Resolves secret references embedded in a value using the {@code ${scheme:reference}}
 * convention. Resolution happens just-in-time on the gateway, delegating to the
 * {@link SecretResolver} matching the scheme.
 * <p>
 * Several placeholders may be interpolated within a single value (e.g.
 * {@code "Bearer ${env:TOKEN}"} or {@code "${env:USER}:${env:PASS}"}). Values that do not
 * contain any placeholder are returned unchanged, preserving backward compatibility with
 * literal secrets.
 * @author laurent
 */
@ApplicationScoped
public class SecretReferenceResolver {

   /** Matches a {@code ${scheme:reference}} placeholder. */
   private static final Pattern PLACEHOLDER = Pattern.compile("\\$\\{(\\w+):([^}]+)}");

   private final Map<String, SecretResolver> resolversByScheme;

   /**
    * Build the SecretReferenceResolver with all existing and present resolver instances.
    * @param resolvers All {@link SecretResolver} implementations, one per scheme. At runtime they are
    *                  injected by CDI through the {@link All} qualifier; tests may pass a plain list.
    */
   public SecretReferenceResolver(@All List<SecretResolver> resolvers) {
      Map<String, SecretResolver> map = new HashMap<>();
      resolvers.forEach(resolver -> map.put(resolver.scheme(), resolver));
      this.resolversByScheme = Map.copyOf(map);
   }

   /**
    * @param value The value to inspect.
    * @return {@code true} if the value contains at least one {@code ${scheme:reference}} placeholder.
    */
   public boolean hasReference(String value) {
      return value != null && PLACEHOLDER.matcher(value).find();
   }

   /**
    * Resolve every {@code ${scheme:reference}} placeholder found in the given value. A value
    * without placeholder (or {@code null}/empty) is returned as-is.
    * @param value The value possibly containing secret references.
    * @return The value with all references resolved to their actual secret value.
    * @throws SecretResolutionException If a scheme is unknown or a reference cannot be resolved.
    */
   public String resolve(String value) {
      if (value == null || value.isEmpty()) {
         return value;
      }
      Matcher matcher = PLACEHOLDER.matcher(value);
      StringBuilder result = new StringBuilder();
      while (matcher.find()) {
         String scheme = matcher.group(1);
         String reference = matcher.group(2);
         SecretResolver resolver = resolversByScheme.get(scheme);
         if (resolver == null) {
            throw new SecretResolutionException("Unknown secret reference scheme '" + scheme + "'");
         }
         String resolved = resolver.resolve(reference);
         matcher.appendReplacement(result, Matcher.quoteReplacement(resolved));
      }
      matcher.appendTail(result);
      return result.toString();
   }
}

