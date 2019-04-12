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
import org.openbaton.catalogue.mano.record.VirtualNetworkFunctionRecord;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

public interface VNFRRepository extends CrudRepository<VirtualNetworkFunctionRecord, String> {
  VirtualNetworkFunctionRecord findFirstById(String id);

  @Query(
      "select v from VirtualNetworkFunctionRecord v where v.id=?1 and v.parent_ns_id=?2 and v.projectId=?3")
  VirtualNetworkFunctionRecord findByIdAndParent_ns_idAndProjectId(
      String id, String parent_ns_id, String projectId);

  List<VirtualNetworkFunctionRecord> findByProjectId(String id);

  @Query("select v from VirtualNetworkFunctionRecord v where v.parent_ns_id=?1")
  List<VirtualNetworkFunctionRecord> findByParentNsId(String networkServiceRecordId);
}
