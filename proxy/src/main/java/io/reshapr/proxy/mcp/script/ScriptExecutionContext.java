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
package io.reshapr.proxy.mcp.script;

/**
 * Scoped storage for the current custom-tool script nesting depth. Used to bound cross-script
 * recursion (a script calling a tool that is itself a script, and so on). The depth is carried as
 * a {@link ScopedValue} so it propagates across the virtual threads used for asynchronous tool
 * calls (provided it is re-established on those threads).
 * @author laurent
 */
public final class ScriptExecutionContext {

   /** The current script nesting depth (unbound means depth 0, i.e. no script running yet). */
   public static final ScopedValue<Integer> DEPTH = ScopedValue.newInstance();

   private ScriptExecutionContext() {
      // Utility class
   }

   /** The current script nesting depth, or 0 if no script is currently running. */
   public static int currentDepth() {
      return DEPTH.isBound() ? DEPTH.get() : 0;
   }
}

