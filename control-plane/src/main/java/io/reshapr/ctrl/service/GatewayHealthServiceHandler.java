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
package io.reshapr.ctrl.service;

import io.reshapr.health.gateway.v1.GatewayHealthResponse;
import io.reshapr.health.gateway.v1.GatewayHealthServiceGrpc;
import io.reshapr.health.gateway.v1.GatewayRequest;

import io.grpc.stub.StreamObserver;
import io.quarkus.grpc.GrpcService;
import io.quarkus.security.Authenticated;
import io.smallrye.common.annotation.RunOnVirtualThread;
import jakarta.enterprise.context.control.ActivateRequestContext;
import org.jboss.logging.Logger;

/**
 * gRPC service handler for gateway health requests.
 * @author laurent
 */
@GrpcService
public class GatewayHealthServiceHandler extends GatewayHealthServiceGrpc.GatewayHealthServiceImplBase {

   /** Get a JBoss logging logger. */
   private final Logger logger = Logger.getLogger(getClass());

   private final GatewayManagerService gatewayManagerService;

   /**
    *
    * @param gatewayManagerService
    */
   public GatewayHealthServiceHandler(GatewayManagerService gatewayManagerService) {
      this.gatewayManagerService = gatewayManagerService;
   }

   @Override
   @Authenticated
   @RunOnVirtualThread
   @ActivateRequestContext
   public void advertHealthy(GatewayRequest request, StreamObserver<GatewayHealthResponse> responseObserver) {
      logger.infof("Received GatewayRequest for gatewayId: %s", request.getGatewayId());
      logger.tracef("Executing on thread: %s", Thread.currentThread().getName());

      boolean result = gatewayManagerService.updateGatewayHeartbeat(request.getGatewayId());

      // Create a response indicating that request was processed.
      GatewayHealthResponse response = GatewayHealthResponse.newBuilder()
            .setAcknowledged(result)
            .build();
      responseObserver.onNext(response);
      responseObserver.onCompleted();
   }

   @Override
   @Authenticated
   @RunOnVirtualThread
   @ActivateRequestContext
   public void advertShutdown(GatewayRequest request, StreamObserver<GatewayHealthResponse> responseObserver) {
      logger.infof("Received GatewayRequest for gatewayId: %s", request.getGatewayId());
      logger.tracef("Executing on thread: %s", Thread.currentThread().getName());

      gatewayManagerService.unregisterGateway(request.getGatewayId());

      // Create a response indicating that request was processed successfully.
      GatewayHealthResponse response = GatewayHealthResponse.newBuilder()
            .setAcknowledged(true)
            .build();
      responseObserver.onNext(response);
      responseObserver.onCompleted();
   }
}
