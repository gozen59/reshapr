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
package io.reshapr.ctrl.model;

/**
 * QuotaMetric represents the different types of metrics that can be used for quota management.
 * Each metric corresponds to a specific resource or action that can be limited.
 * @author laurent
 */
public enum QuotaMetric {
   NONE("none"),
   EXPOSITION_COUNT("exposition.count"),
   GATEWAY_GROUP_COUNT("gateway-group.count"),
   GATEWAY_COUNT("gateway.count");

   private final String metricName;

   QuotaMetric(String metricName) {
      this.metricName = metricName;
   }

   @Override
   public String toString() {
      return metricName;
   }
}
