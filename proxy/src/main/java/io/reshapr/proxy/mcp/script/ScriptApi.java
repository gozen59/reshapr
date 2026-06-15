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

import io.roastedroot.quickjs4j.annotations.GuestFunction;
import io.roastedroot.quickjs4j.annotations.Invokables;

/**
 * The guest (JavaScript) API invoked by the host to run a custom-tool script. The wrapped script
 * defines a {@code process()} function returning the JSON-stringified result object.
 * @author laurent
 */
@Invokables("js")
public interface ScriptApi {

   /**
    * Run the custom-tool script and return its JSON-serialized result.
    * @return The JSON string produced by {@code JSON.stringify(...)} of the script's return value.
    */
   @GuestFunction
   String process();
}

