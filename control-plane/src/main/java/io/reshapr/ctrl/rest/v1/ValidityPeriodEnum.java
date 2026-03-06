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
package io.reshapr.ctrl.rest.v1;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Enumeration representing validity periods for API tokens.
 * Each enum constant corresponds to a specific number of days.
 */
public enum ValidityPeriodEnum {
   ONE_DAY(1),
   SEVEN_DAYS(7),
   THIRTY_DAYS(30),
   NINETY_DAYS(90);

   private final int days;

   ValidityPeriodEnum(int days) {
      this.days = days;
   }

   @JsonValue
   public int getDays() {
      return days;
   }
}
