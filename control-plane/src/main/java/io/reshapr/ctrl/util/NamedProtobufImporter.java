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
package io.reshapr.ctrl.util;

import io.github.microcks.domain.Exchange;
import io.github.microcks.domain.Operation;
import io.github.microcks.domain.Resource;
import io.github.microcks.domain.Service;
import io.github.microcks.util.MockRepositoryImportException;
import io.github.microcks.util.MockRepositoryImporter;
import io.github.microcks.util.grpc.ProtobufImporter;

import java.util.List;

/**
 * This is a simple wrapper around ProtobufImporter that ignores default service Name and version
 * parameters and allows overriding them with fixed values.
 * @author laurent
 */
public class NamedProtobufImporter implements MockRepositoryImporter {

   private final ProtobufImporter delegate;
   private final String serviceName;
   private final String serviceVersion;

   /**
    * Create a new NamedProtobufImporter with its delegate and fixed name and version.
    * @param delegate the ProtobufImporter to delegate to
    * @param serviceName the overridden service name
    * @param serviceVersion the overridden service version
    */
   public NamedProtobufImporter(ProtobufImporter delegate, String serviceName, String serviceVersion) {
      this.delegate = delegate;
      this.serviceName = serviceName;
      this.serviceVersion = serviceVersion;
   }

   @Override
   public List<Service> getServiceDefinitions() throws MockRepositoryImportException {
      List<Service> services = delegate.getServiceDefinitions();
      return services.stream().peek(service -> {
         service.setName(serviceName);
         service.setVersion(serviceVersion);
      }).toList();
   }

   @Override
   public List<Resource> getResourceDefinitions(Service service) throws MockRepositoryImportException {
      return delegate.getResourceDefinitions(service);
   }

   @Override
   public List<Exchange> getMessageDefinitions(Service service, Operation operation) throws MockRepositoryImportException {
      return delegate.getMessageDefinitions(service, operation);
   }
}
