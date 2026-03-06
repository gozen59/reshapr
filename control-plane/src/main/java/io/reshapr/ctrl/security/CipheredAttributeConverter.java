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
package io.reshapr.ctrl.security;

import io.quarkus.arc.Arc;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * A JPA attribute converter that encrypts and decrypts string attributes using a CipherService.
 * @author laurent
 */
@Converter
public class CipheredAttributeConverter implements AttributeConverter<String, String> {

   @Override
   public String convertToDatabaseColumn(String attribute) {
      if (attribute != null) {
         CipherService cipherService = Arc.container().instance(CipherService.class).get();
         return cipherService.encrypt(attribute);
      }
      return null;
   }

   @Override
   public String convertToEntityAttribute(String dbData) {
      if (dbData != null) {
         CipherService cipherService = Arc.container().instance(CipherService.class).get();
         return cipherService.decrypt(dbData);
      }
      return null;
   }
}
