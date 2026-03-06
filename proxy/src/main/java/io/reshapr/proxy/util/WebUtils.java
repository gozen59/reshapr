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
package io.reshapr.proxy.util;

/**
 * A collection of web-related utility methods.
 * @author laurent
 */
public class WebUtils {

   private WebUtils() {
      // Utility class.
   }

   /**
    * Get appropriate HTTP scheme for given FQDN.
    * @param fqdn The fully qualified domain name
    * @return http:// for localhost, https:// otherwise
    */
   public static String getHTTPScheme(String fqdn) {
      if (fqdn.startsWith("localhost:")) {
         return "http://";
      }
      return "https://";
   }
}
