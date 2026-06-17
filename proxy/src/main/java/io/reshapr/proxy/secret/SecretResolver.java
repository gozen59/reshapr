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

/**
 * Strategy interface for resolving a secret reference locally on the gateway.
 * <p>
 * Implementations are selected by their {@link #scheme()} and discovered via CDI.
 * A secret value declared in the control plane as {@code ${scheme:reference}} is
 * resolved at call time by the matching resolver, so that real secret values never
 * need to be persisted in or transferred from the control plane.
 * @author laurent
 */
public interface SecretResolver {

   /**
    * The scheme handled by this resolver, e.g. {@code "env"} for {@code ${env:MY_VAR}}.
    * @return The scheme identifier, without the {@code ':'} separator.
    */
   String scheme();

   /**
    * Resolve the given reference into its actual secret value.
    * @param reference The reference part following the scheme, e.g. {@code MY_VAR} for {@code ${env:MY_VAR}}.
    * @return The resolved secret value.
    * @throws SecretResolutionException If the reference cannot be resolved locally.
    */
   String resolve(String reference) throws SecretResolutionException;
}

