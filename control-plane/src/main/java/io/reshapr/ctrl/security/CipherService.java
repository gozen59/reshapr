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

import io.quarkus.arc.Unremovable;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.util.Base64;

/**
 * Service for encrypting and decrypting data using AES encryption.
 * This service uses a configurable encryption key, which must be 16, 24, or 32 characters long.
 * @author laurent
 */
@Unremovable
@ApplicationScoped
public class CipherService {

   private static final String ALGORITHM = "AES/ECB/PKCS5Padding";
   private static final int[] AES_KEYSIZES = { 16, 24, 32 };

   @ConfigProperty(name = "reshapr.encryption.key")
   String encryptionKey;

   private byte[] encryptionKeyBytes;

   @PostConstruct
   void initialize() {
      encryptionKeyBytes = encryptionKey.getBytes();
      if (encryptionKeyBytes == null || !isKeySizeValid(encryptionKeyBytes.length)) {
         throw new IllegalArgumentException("Encryption key must 16, 24 or 32 characters long");
      }
   }

   public String encrypt(String data) {
      // Do some encryption.
      Key key = new SecretKeySpec(encryptionKeyBytes, "AES");
      try {
         Cipher c = Cipher.getInstance(ALGORITHM);
         c.init(Cipher.ENCRYPT_MODE, key);
         return Base64.getEncoder().encodeToString(c.doFinal(data.getBytes()));
      } catch (Exception e) {
         throw new RuntimeException(e);
      }
   }

   public String decrypt(String encryptedData) {
      // Do some decryption.
      Key key = new SecretKeySpec(encryptionKeyBytes, "AES");
      try {
         Cipher c = Cipher.getInstance(ALGORITHM);
         c.init(Cipher.DECRYPT_MODE, key);
         return new String(c.doFinal(Base64.getDecoder().decode(encryptedData)));
      } catch (Exception e) {
         throw new RuntimeException(e);
      }
   }

   private static boolean isKeySizeValid(int len) {
      for (int aesKeysize : AES_KEYSIZES) {
         if (len == aesKeysize) {
            return true;
         }
      }
      return false;
   }
}
