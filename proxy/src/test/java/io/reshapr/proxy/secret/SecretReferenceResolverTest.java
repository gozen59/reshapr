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

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * This is a test case for SecretReferenceResolver.
 * @author laurent
 */
class SecretReferenceResolverTest {

   /** A simple in-memory resolver backed by a map, used for testing. */
   private static SecretResolver mapResolver(String scheme, Map<String, String> values) {
      return new SecretResolver() {
         @Override
         public String scheme() {
            return scheme;
         }

         @Override
         public String resolve(String reference) {
            String value = values.get(reference);
            if (value == null) {
               throw new SecretResolutionException("Unknown reference '" + reference + "'");
            }
            return value;
         }
      };
   }

   private SecretReferenceResolver resolverWith(SecretResolver... resolvers) {
      return new SecretReferenceResolver(List.of(resolvers));
   }

   @Test
   void shouldReturnLiteralValueUnchanged() {
      SecretReferenceResolver resolver = resolverWith(mapResolver("env", Map.of("TOKEN", "abc")));

      assertEquals("plain-token", resolver.resolve("plain-token"));
   }

   @Test
   void shouldReturnNullAndEmptyUnchanged() {
      SecretReferenceResolver resolver = resolverWith(mapResolver("env", Map.of()));

      assertNull(resolver.resolve(null));
      assertEquals("", resolver.resolve(""));
   }

   @Test
   void shouldResolveSinglePlaceholder() {
      SecretReferenceResolver resolver = resolverWith(mapResolver("env", Map.of("TOKEN", "s3cr3t")));

      assertEquals("s3cr3t", resolver.resolve("${env:TOKEN}"));
   }

   @Test
   void shouldInterpolatePlaceholderWithinValue() {
      SecretReferenceResolver resolver = resolverWith(mapResolver("env", Map.of("TOKEN", "s3cr3t")));

      assertEquals("Bearer s3cr3t", resolver.resolve("Bearer ${env:TOKEN}"));
   }

   @Test
   void shouldResolveMultiplePlaceholders() {
      SecretReferenceResolver resolver = resolverWith(
            mapResolver("env", Map.of("USER", "alice", "PASS", "p@ss")));

      assertEquals("alice:p@ss", resolver.resolve("${env:USER}:${env:PASS}"));
   }

   @Test
   void shouldSafelyHandleSpecialCharactersInResolvedValue() {
      // Dollar and backslash must not break the regex replacement.
      SecretReferenceResolver resolver = resolverWith(mapResolver("env", Map.of("TOKEN", "a$b\\c")));

      assertEquals("a$b\\c", resolver.resolve("${env:TOKEN}"));
   }

   @Test
   void shouldThrowOnUnknownScheme() {
      SecretReferenceResolver resolver = resolverWith(mapResolver("env", Map.of("TOKEN", "abc")));

      SecretResolutionException ex = assertThrows(SecretResolutionException.class,
            () -> resolver.resolve("${vault:secret/path}"));
      assertTrue(ex.getMessage().contains("vault"));
   }

   @Test
   void shouldPropagateResolutionFailure() {
      SecretReferenceResolver resolver = resolverWith(mapResolver("env", Map.of()));

      assertThrows(SecretResolutionException.class, () -> resolver.resolve("${env:MISSING}"));
   }

   @Test
   void shouldDetectReferencePresence() {
      SecretReferenceResolver resolver = resolverWith(mapResolver("env", Map.of()));

      assertTrue(resolver.hasReference("${env:TOKEN}"));
      assertTrue(resolver.hasReference("Bearer ${env:TOKEN}"));
      assertFalse(resolver.hasReference("literal"));
      assertFalse(resolver.hasReference(null));
   }
}

