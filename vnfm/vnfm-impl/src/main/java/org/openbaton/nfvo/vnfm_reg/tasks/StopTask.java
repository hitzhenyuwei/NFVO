/*
 * Copyright (c) 2016 Open Baton (http://www.openbaton.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.openbaton.nfvo.vnfm_reg.tasks;

import java.util.Date;
import org.openbaton.catalogue.mano.common.Event;
import org.openbaton.catalogue.mano.descriptor.VirtualDeploymentUnit;
import org.openbaton.catalogue.mano.record.VNFCInstance;
import org.openbaton.catalogue.mano.record.VirtualNetworkFunctionRecord;
import org.openbaton.catalogue.nfvo.messages.Interfaces.NFVMessage;
import org.openbaton.nfvo.repositories.VNFCInstanceRepository;
import org.openbaton.nfvo.vnfm_reg.tasks.abstracts.AbstractTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

/** Created by fmu on 19/08/16. */
@Service
@Scope("prototype")
@ConfigurationProperties(prefix = "nfvo.stop")
public class StopTask extends AbstractTask {

  @Autowired private VNFCInstanceRepository vnfcInstanceRepository;

  private String ordered;
  private VNFCInstance vnfcInstance;

  @Override
  public boolean isAsync() {
    return true;
  }

  public void setOrdered(String ordered) {
    this.ordered = ordered;
  }

  public VNFCInstance getVnfcInstance() {
    return vnfcInstance;
  }

  public void setVnfcInstance(VNFCInstance vnfcInstance) {
    this.vnfcInstance = vnfcInstance;
  }

  @Override
  public NFVMessage doWork() {
    log.info("Stopped VNFR: " + virtualNetworkFunctionRecord.getName());
    VirtualNetworkFunctionRecord existing =
        vnfrRepository.findFirstById(virtualNetworkFunctionRecord.getId());
    log.trace("VNFR existing hibernate version = " + existing.getHbVersion());
    log.trace("VNFR reiceived hibernate version = " + virtualNetworkFunctionRecord.getHbVersion());

    for (VirtualDeploymentUnit virtualDeploymentUnit : virtualNetworkFunctionRecord.getVdu()) {
      for (VNFCInstance vnfcInstance : virtualDeploymentUnit.getVnfc_instance()) {
        log.trace("VNFCI received hibernate version = " + vnfcInstance.getHbVersion());
      }
    }

    for (VirtualDeploymentUnit virtualDeploymentUnit : existing.getVdu()) {
      for (VNFCInstance vnfcInstance : virtualDeploymentUnit.getVnfc_instance()) {
        log.trace("VNFCI existing hibernate version = " + vnfcInstance.getHbVersion());
      }
    }

    setHistoryLifecycleEvent(new Date());
    saveVirtualNetworkFunctionRecord();

    return null;
  }

  @Override
  protected void setEvent() {
    event = Event.STOP.name();
  }

  @Override
  protected void setDescription() {
    description = "The Stop scripts were executed correctly on this VNFR";
  }
}
