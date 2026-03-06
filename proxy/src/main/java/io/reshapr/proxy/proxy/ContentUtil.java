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
package io.reshapr.proxy.proxy;

import com.aayushatharva.brotli4j.decoder.BrotliInputStream;
import com.google.common.net.HttpHeaders;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;

/**
 * Utility class for handling content extraction from backend responses.
 * @author laurent
 */
public class ContentUtil {

   private ContentUtil () {
      // Utility class
   }

   /** Depending on result encoding, extract the response content as a string. */
   public static String extractResponseContent(BackendResponse result) throws IOException {
      String responseContent = null;
      if (result.headers() != null) {
         // Response content can be compressed with gzip if we used the proxy.
         List<String> encodings = result.headers().get(HttpHeaders.CONTENT_ENCODING);
         if (encodings != null) {
            // Extract the encoded content.
            responseContent = extractEncodedResponseContent(encodings, result.content());
         }
      }
      if (responseContent == null) {
         // If no response content here, we can assume it's not compressed and can be read directly.
         responseContent = new String(result.content(), StandardCharsets.UTF_8);
      }
      return responseContent;
   }

   protected static String extractEncodedResponseContent(List<String> encodings, byte[] content) throws IOException {
      String responseContent = null;
      if (encodings.contains("gzip")) {
         // Unzip the response content.
         try (BufferedInputStream bis = new BufferedInputStream(
               new GZIPInputStream(new ByteArrayInputStream(content)))) {
            byte[] uncompressedContent = bis.readAllBytes();
            responseContent = new String(uncompressedContent, StandardCharsets.UTF_8);
         }
      } else if (encodings.contains("deflate")) {
         // Inflate response content.
         try (BufferedInputStream bis = new BufferedInputStream(
               new InflaterInputStream(new ByteArrayInputStream(content)))) {
            byte[] uncompressedContent = bis.readAllBytes();
            responseContent = new String(uncompressedContent, StandardCharsets.UTF_8);
         }
      } else if (encodings.contains("br")) {
         // Brotli encoding is not supported out-of-the-box in Java.
         // We would need to use a third-party library like 'google/brotli' to handle it.
         try (BufferedInputStream bis = new BufferedInputStream(
               new BrotliInputStream(new ByteArrayInputStream(content)))) {
            byte[] uncompressedContent = bis.readAllBytes();
            responseContent = new String(uncompressedContent, StandardCharsets.UTF_8);
         }
      } else {
         // If other encoding is specified, just return the byte translation.
         responseContent = new String(content, StandardCharsets.UTF_8);
      }
      return responseContent;
   }
}
