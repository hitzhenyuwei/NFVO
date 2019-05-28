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

package org.openbaton.nfvo.core.interfaces;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import org.openbaton.catalogue.mano.descriptor.NetworkServiceDescriptor;
import org.openbaton.catalogue.mano.descriptor.VNFComponent;
import org.openbaton.catalogue.mano.record.NetworkServiceRecord;
import org.openbaton.catalogue.mano.record.VNFCInstance;
import org.openbaton.catalogue.mano.record.VirtualNetworkFunctionRecord;
import org.openbaton.catalogue.nfvo.Configuration;
import org.openbaton.catalogue.nfvo.Script;
import org.openbaton.catalogue.nfvo.messages.Interfaces.NFVMessage;
import org.openbaton.exceptions.*;

/** Created by mpa on 30/04/15. */
public interface NetworkServiceRecordManagement {

  /**
   * This operation allows submitting and validating a Network Service Descriptor (NSD), including
   * any related VNFFGD and VLD.
   */
  NetworkServiceRecord onboard(
      String nsd_id,
      String projectId,
      List keys,
      Map<String, Set<String>> vduVimInstances,
      Map<String, Configuration> configurations,
      String monitoringIp)
      throws VimException, NotFoundException, PluginException, BadRequestException, IOException,
          AlreadyExistingException, BadFormatException, ExecutionException, InterruptedException;

  /**
   * This operation allows submitting and validating a Network Service Descriptor (NSD), including
   * any related VNFFGD and VLD.
   *
   * @param networkServiceDescriptor NSD
   * @param projectId project ID
   * @param keys Keys to use
   * @param vduVimInstances mapping VDUs to VIMs
   * @param configurations configuration map
   * @param monitoringIp the monitoring IP
   * @return the created NSR
   * @throws VimException exception
   * @throws PluginException exception
   * @throws NotFoundException exception
   * @throws BadRequestException exception
   * @throws IOException exception
   * @throws AlreadyExistingException exception
   * @throws BadFormatException exception
   * @throws ExecutionException exception
   * @throws InterruptedException exception
   */
  NetworkServiceRecord onboard(
      NetworkServiceDescriptor networkServiceDescriptor,
      String projectId,
      List keys,
      Map vduVimInstances,
      Map configurations,
      String monitoringIp)
      throws VimException, PluginException, NotFoundException, BadRequestException, IOException,
          AlreadyExistingException, BadFormatException, ExecutionException, InterruptedException;

  /**
   * This operation allows updating a Network Service Record (NSR). This update might include
   * creating/deleting new VNFFGDs and/or new VLDs.
   *
   * @param new_nsd the new NSD
   * @param old_id ID of the NSD to update
   * @param projectId project ID
   * @return the updated NSD
   * @throws NotFoundException if NSD with given ID does not exist
   */
  NetworkServiceRecord update(NetworkServiceRecord new_nsd, String old_id, String projectId)
      throws NotFoundException;

  /**
   * This operation allows updating a Virtual Network Function Record (VNFR) (The UPDATE is intended
   * as the execution of the scripts associated to the lifecycle UPDATE by the VNF provider)
   *
   * @param nsrId NSR ID
   * @param vnfrId VNFR ID
   * @param projectId project ID
   * @return the updated VNFR
   * @throws NotFoundException exception
   * @throws BadFormatException exception
   * @throws ExecutionException exception
   * @throws InterruptedException exception
   */
  VirtualNetworkFunctionRecord updateVnfr(String nsrId, String vnfrId, String projectId)
      throws NotFoundException, BadFormatException, ExecutionException, InterruptedException;

  /**
   * This operation allows upgrading a Virtual Network Function Record (VNFR) (The UPGRADE is
   * intended as one of the following the rebuild of the VNFR (all the VNFC Instances, if many)
   * using a new OS image and/or new VNF scripts
   *
   * @throws NotFoundException exception
   * @throws BadFormatException exception
   * @throws ExecutionException exception
   * @throws InterruptedException exception
   * @throws IOException exception
   * @throws BadRequestException exception
   * @throws VimException exception
   * @throws PluginException exception
   * @throws VimDriverException exception
   */
  VirtualNetworkFunctionRecord upgradeVnfr(
      String nsrId, String vnfrId, String projectId, String vnfdId)
      throws NotFoundException, BadFormatException, ExecutionException, InterruptedException,
          IOException, BadRequestException, VimException, PluginException, VimDriverException;

  /**
   * This operation is used to query the information of the Network Service Descriptor (NSD),
   * including any related VNFFGD and VLD.
   */
  Iterable<NetworkServiceRecord> query();

  void executeAction(
      NFVMessage nfvMessage,
      String nsrId,
      String idVnf,
      String idVdu,
      String idVNFCI,
      String projectId)
      throws NotFoundException, BadFormatException, ExecutionException, InterruptedException;

  NetworkServiceRecord query(String id, String projectId) throws NotFoundException;

  /**
   * This operation is used to remove a Network Service Record.
   *
   * @param id NSR ID
   * @param projectId project ID
   * @throws NotFoundException exception
   * @throws WrongStatusException exception
   * @throws BadFormatException exception
   * @throws ExecutionException exception
   * @throws InterruptedException exception
   */
  void delete(String id, String projectId)
      throws NotFoundException, WrongStatusException, BadFormatException, ExecutionException,
          InterruptedException;

  /**
   * This operation is used to resume a failed Network Service Record.
   *
   * @param id NSR ID
   * @param projectId project ID
   * @throws NotFoundException exception
   * @throws BadFormatException exception
   * @throws ExecutionException exception
   * @throws InterruptedException exception
   */
  void resume(String id, String projectId)
      throws NotFoundException, BadFormatException, ExecutionException, InterruptedException;

  /**
   * This operation is used to execute a script on a specific Virtual Network Function Record during
   * runtime.
   *
   * @param idNsr
   * @param idVnf
   * @param projectId
   * @param script
   * @throws NotFoundException exception
   * @throws InterruptedException exception
   * @throws BadFormatException exception
   * @throws ExecutionException exception
   */
  void executeScript(String idNsr, String idVnf, String projectId, Script script)
      throws NotFoundException, InterruptedException, BadFormatException, ExecutionException;

  void deleteVNFRecord(String idNsr, String idVnf, String projectId) throws NotFoundException;

  /**
   * Returns the VirtualNetworkFunctionRecord with idVnf into NSR with idNsr
   *
   * @param idNsr of Nsr
   * @param idVnf of VirtualNetworkFunctionRecord
   * @return the VirtualNetworkFunctionRecord selected
   */
  VirtualNetworkFunctionRecord getVirtualNetworkFunctionRecord(
      String idNsr, String idVnf, String projectId) throws NotFoundException;

  /**
   * Deletes the VNFDependency with idVnfr into NSR with idNsr
   *
   * @param idNsr of NSR
   * @param idVnfd of VNFDependency
   */
  void deleteVNFDependency(String idNsr, String idVnfd, String projectId) throws NotFoundException;

  /**
   * This method will add a {@link VNFCInstance} into a NetworkServiceRecord to a specific
   * VirtualDeploymentUnit of a specific VirtualNetworkFunctionRecord
   *
   * @param id of the NetworkServiceRecord
   * @param idVnf of the VirtualNetworkFunctionRecord
   * @param idVdu of the VirtualDeploymentUnit chosen
   * @param vimInstanceNames list of vim names
   */
  void addVNFCInstance(
      String id,
      String idVnf,
      String idVdu,
      VNFComponent component,
      String mode,
      String projectId,
      List<String> vimInstanceNames)
      throws NotFoundException, BadFormatException, WrongStatusException;

  /**
   * This method will add a {@link VNFCInstance} into a NetworkServiceRecord to a specific
   * VirtualNetworkFunctionRecord. The VirtualDeploymentUnit is randomly chosen
   */
  void addVNFCInstance(
      String id,
      String idVnf,
      VNFComponent component,
      String projectId,
      List<String> vimInstanceNames)
      throws NotFoundException, BadFormatException, WrongStatusException;

  /**
   * This method will remove a {@link VNFCInstance} of a NetworkServiceRecord from a specific
   * VirtualNetworkFunctionRecord. VirtualDeploymentUnit will be randomly chosen.
   */
  void deleteVNFCInstance(String id, String idVnf, String projectId)
      throws NotFoundException, WrongStatusException, InterruptedException, ExecutionException,
          VimException, PluginException, BadFormatException;

  /**
   * This method will remove a {@link VNFCInstance} of a NetworkServiceRecord from a specific
   * VirtualDeploymentUnit of a specific VirtualNetworkFunctionRecord.
   */
  void deleteVNFCInstance(String id, String idVnf, String idVdu, String idVNFCI, String projectId)
      throws NotFoundException, WrongStatusException, InterruptedException, ExecutionException,
          VimException, PluginException, BadFormatException;

  /**
   * This method will start a {@link VNFCInstance} of a NetworkServiceRecord from a specific
   * VirtualDeploymentUnit of a specific VirtualNetworkFunctionRecord.
   */
  void startVNFCInstance(String id, String idVnf, String idVdu, String idVNFCI, String projectId)
      throws NotFoundException, BadFormatException, ExecutionException, InterruptedException;

  /**
   * This method will stop a {@link VNFCInstance} of a NetworkServiceRecord from a specific
   * VirtualDeploymentUnit of a specific VirtualNetworkFunctionRecord.
   */
  void stopVNFCInstance(String id, String idVnf, String idVdu, String idVNFCI, String projectId)
      throws NotFoundException, BadFormatException, ExecutionException, InterruptedException;

  void switchToRedundantVNFCInstance(
      String id,
      String idVnf,
      String idVdu,
      String idVNFC,
      String standby,
      VNFCInstance failedVnfcInstance,
      String projectId)
      throws NotFoundException, WrongStatusException, BadFormatException, ExecutionException,
          InterruptedException;

  void deleteVNFCInstance(String id, String idVnf, String idVdu, String projectId)
      throws NotFoundException, WrongStatusException, InterruptedException, ExecutionException,
          VimException, PluginException, BadFormatException;

  List<NetworkServiceRecord> queryByProjectId(String projectId);

  NetworkServiceRecord scaleOutNsr(
      String nsrId,
      String vnfdId,
      String projectId,
      List keys,
      Map vduVimInstances,
      Map configurations,
      String monitoringIp)
      throws NotFoundException, BadRequestException, MissingParameterException,
          InterruptedException, BadFormatException, ExecutionException, CyclicDependenciesException,
          NetworkServiceIntegrityException;

  VirtualNetworkFunctionRecord restartVnfr(
      NetworkServiceRecord nsr, String vnfrId, String imageName, String projectId)
      throws NotFoundException, IOException, BadRequestException, VimException, PluginException,
          ExecutionException, InterruptedException, BadFormatException, VimDriverException;
}
