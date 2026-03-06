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

import java.util.List;
import java.util.function.Predicate;

/**
 * A predicate to filter operations based on included and excluded operation names.
 * @author laurent
 */
public class OperationNameFilterPredicate implements Predicate<Operation> {

   private final List<String> includedOperations;
   private final List<String> excludedOperations;

   /**
    * Constructor for OperationNameFilterPredicate.
    * @param includedOperations the list of operation names to include (has precedence on excludedOperations)
    * @param excludedOperations the list of operation names to exclude (ignore if includedOperations is not empty)
    */
   public OperationNameFilterPredicate(List<String> includedOperations, List<String> excludedOperations) {
      this.includedOperations = includedOperations;
      this.excludedOperations = excludedOperations;
   }

   @Override
   public boolean test(Operation operation) {
      if (includedOperations != null && !includedOperations.isEmpty()) {
         return includedOperations.contains(operation.name);
      }
      if (excludedOperations != null && !excludedOperations.isEmpty()) {
         return !excludedOperations.contains(operation.name);
      }
      return true;
   }
}
