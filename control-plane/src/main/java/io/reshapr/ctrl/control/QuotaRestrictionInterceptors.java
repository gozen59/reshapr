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
package io.reshapr.ctrl.control;

import io.reshapr.ctrl.model.QuotaMetric;

import jakarta.annotation.Priority;
import jakarta.interceptor.Interceptor;

/**
 * Concrete interceptors for each quota metric.
 * This allows us to use the @QuotaRestricted annotation with specific metrics.
 * @author laurent
 */
public class QuotaRestrictionInterceptors {

   @QuotaRestricted(metric = QuotaMetric.EXPOSITION_COUNT)
   @Priority(2000)
   @Interceptor
   public static class ExpositionCountQuotaRestriction extends QuotaRestrictionInterceptor {
      public ExpositionCountQuotaRestriction() {
         super(QuotaMetric.EXPOSITION_COUNT);
      }
   }

   @QuotaRestricted(metric = QuotaMetric.GATEWAY_GROUP_COUNT)
   @Priority(2000)
   @Interceptor
   public static class GatewayGroupCountQuotaRestriction extends QuotaRestrictionInterceptor {
      public GatewayGroupCountQuotaRestriction() {
         super(QuotaMetric.GATEWAY_GROUP_COUNT);
      }
   }
}
