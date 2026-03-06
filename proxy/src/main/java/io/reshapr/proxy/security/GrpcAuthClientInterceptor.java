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
package io.reshapr.proxy.security;

import io.grpc.CallCredentials;
import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.ClientInterceptor;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;
import io.grpc.Status;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import java.util.concurrent.Executor;

/**
 * Interceptor to handle authentication for gRPC client calls.
 * @author laurent
 */
@ApplicationScoped
public class GrpcAuthClientInterceptor implements ClientInterceptor {

   /** Get a JBoss logging logger. */
   private final Logger logger = Logger.getLogger(getClass());

   public static final Metadata.Key<String> AUTHORIZATION_KEY = Metadata.Key.of("Authorization", Metadata.ASCII_STRING_MARSHALLER);

   @ConfigProperty(name = "reshapr.ctrl.token")
   String controlPlaneToken;


   @Override
   public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(MethodDescriptor<ReqT, RespT> methodDescriptor,
         CallOptions callOptions, Channel channel) {

      if (controlPlaneToken != null && !controlPlaneToken.isEmpty()) {
         logger.tracef("Adding authentication token to gRPC call: %s", controlPlaneToken);

         CallOptions newCallOptions = callOptions.withCallCredentials(new CallCredentials() {
            @Override
            public void applyRequestMetadata(RequestInfo requestInfo, Executor executor, MetadataApplier metadataApplier) {
               executor.execute(() -> {
                  try {
                     Metadata headers = new Metadata();
                     headers.put(AUTHORIZATION_KEY, "ApiToken " + controlPlaneToken);
                     metadataApplier.apply(headers);
                  } catch (Throwable e) {
                     metadataApplier.fail(Status.UNAUTHENTICATED.withCause(e));
                  }
               });
            }
         });
         return channel.newCall(methodDescriptor, newCallOptions);
      }
      return channel.newCall(methodDescriptor, callOptions);
   }
}
