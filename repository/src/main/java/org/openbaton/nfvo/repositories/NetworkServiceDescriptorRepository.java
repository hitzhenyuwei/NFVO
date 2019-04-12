/*
 * Copyright (c) 2015-2018 Open Baton (http://openbaton.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openbaton.nfvo.repositories;

import java.util.List;
import org.openbaton.catalogue.mano.descriptor.NetworkServiceDescriptor;
import org.springframework.data.repository.CrudRepository;

public interface NetworkServiceDescriptorRepository
    extends CrudRepository<NetworkServiceDescriptor, String>,
        NetworkServiceDescriptorRepositoryCustom {
  NetworkServiceDescriptor findFirstById(String id);

  // Finds the NSDs that contains the VNFD with the given id
  List<NetworkServiceDescriptor> findByVnfd_idAndProjectId(String id, String projectId);

  NetworkServiceDescriptor findFirstByIdAndProjectId(String id, String projectId);

  List<NetworkServiceDescriptor> findByProjectId(String projectId);
}
