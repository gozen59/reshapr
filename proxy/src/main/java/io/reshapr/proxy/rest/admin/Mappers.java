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
package io.reshapr.proxy.rest.admin;

import io.reshapr.proxy.registry.ConfigurationEntry;
import io.reshapr.proxy.registry.ServiceEntry;

import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;

/**
 * Mapper interface for converting registry entries to DTOs.
 * This interface uses MapStruct to generate the implementation at compile time.
 * @author laurent
 */
@Mapper(componentModel = "cdi",
      nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface Mappers {

   ServiceEntryDTO toServiceEntryDTO(ServiceEntry serviceEntry);

   ConfigurationEntryDTO toConfigurationEntryDTO(ConfigurationEntry configurationEntry);
}
