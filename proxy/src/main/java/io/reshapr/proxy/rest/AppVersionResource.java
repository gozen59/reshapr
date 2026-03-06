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
package io.reshapr.proxy.rest;

import io.quarkus.runtime.annotations.RegisterForReflection;
import io.smallrye.common.annotation.RunOnVirtualThread;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 * REST resource to expose the application version and build timestamp.
 * @author laurent
 */
@RunOnVirtualThread
@Path("/api/version")
public class AppVersionResource {

   @ConfigProperty(name = "reshapr.version")
   String version;
   @ConfigProperty(name = "reshapr.buildTimestamp")
   String buildTimestamp;

   @GET
   @Path("/")
   public AppVersion getAppVersion() {
      return new AppVersion(version, buildTimestamp);
   }

   @RegisterForReflection
   public record AppVersion(String version, String buildTimestamp) {
   }
}
