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
package io.reshapr.config;

import graphql.parser.ParserOptions;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import java.util.Optional;

/**
 * Configuration class to set up GraphQL parser options based on application properties.
 * This allows customization of the GraphQL parser's behavior, such as maximum characters and tokens.
 * @author laurent
 */
@ApplicationScoped
public class GraphQLParserConfiguration {

   /** Get a JBoss logging logger. */
   private final Logger logger = Logger.getLogger(getClass());

   @ConfigProperty(name = "graphql.parser.max-characters")
   Optional<Integer> maxCharacters;

   @ConfigProperty(name = "graphql.parser.max-tokens")
   Optional<Integer> maxTokens;

   void startup(@Observes StartupEvent event) {
      logger.debugf("Configuring GraphQL ParserOptions with maxCharacters=%s and maxTokens=%s",
            maxCharacters.orElse(null), maxTokens.orElse(null));

      // Override the default ParserOptions with the ones defined in application.properties.
      maxCharacters.ifPresent(integer -> ParserOptions.setDefaultParserOptions(
            ParserOptions.getDefaultParserOptions().transform(opts -> opts.maxCharacters(integer))));

      maxTokens.ifPresent(integer -> ParserOptions.setDefaultParserOptions(
            ParserOptions.getDefaultParserOptions().transform(opts -> opts.maxTokens(integer))));
   }
}
