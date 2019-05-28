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
import java.util.Map;
import java.util.concurrent.ExecutionException;
import org.openbaton.catalogue.mano.descriptor.VirtualNetworkFunctionDescriptor;
import org.openbaton.catalogue.nfvo.Script;
import org.openbaton.catalogue.nfvo.VNFPackage;
import org.openbaton.catalogue.nfvo.VNFPackageMetadata;
import org.openbaton.catalogue.nfvo.images.BaseNfvImage;
import org.openbaton.exceptions.AlreadyExistingException;
import org.openbaton.exceptions.BadFormatException;
import org.openbaton.exceptions.BadRequestException;
import org.openbaton.exceptions.ExistingVNFPackage;
import org.openbaton.exceptions.IncompatibleVNFPackage;
import org.openbaton.exceptions.NetworkServiceIntegrityException;
import org.openbaton.exceptions.NotFoundException;
import org.openbaton.exceptions.PluginException;
import org.openbaton.exceptions.VNFPackageFormatException;
import org.openbaton.exceptions.VimException;
import org.openbaton.exceptions.WrongAction;

/** Created by mpa on 05/05/15. */
public interface VNFPackageManagement {

  /**
   * This operation handles reading the Metadata of the VNF Package
   *
   * @param metadata the metadata
   * @param vnfPackage the corresponding VNF package
   * @param imageFiles a Map of image files extracted from the package (maps image name to byte[])
   */
  VNFPackage handleMetadata(
      Map<String, Object> metadata, VNFPackage vnfPackage, Map<String, byte[]> imageFiles)
      throws IncompatibleVNFPackage, BadFormatException, IOException;

  VirtualNetworkFunctionDescriptor add(
      byte[] pack, boolean isImageIncluded, String projectId, boolean fromMarketPlace)
      throws IOException, VimException, NotFoundException, PluginException, ExistingVNFPackage,
          VNFPackageFormatException, IncompatibleVNFPackage, BadRequestException,
          AlreadyExistingException, NetworkServiceIntegrityException, InterruptedException,
          BadFormatException, ExecutionException;

  /**
   * This operation handles the data about the image of the vnf package
   *
   * @param vnfPackage
   * @param virtualNetworkFunctionDescriptor
   * @param metadata
   * @param projectId
   */
  BaseNfvImage handleImage(
      VNFPackage vnfPackage,
      VirtualNetworkFunctionDescriptor virtualNetworkFunctionDescriptor,
      Map<String, Object> metadata,
      String projectId)
      throws NotFoundException, PluginException, VimException, BadRequestException, IOException,
          AlreadyExistingException, InterruptedException, ExecutionException, BadFormatException;

  /**
   * This operation allows submitting and validating the VNF Package from the marketplace.
   *
   * @param link
   * @param projectId
   */
  VirtualNetworkFunctionDescriptor onboardFromMarket(String link, String projectId)
      throws IOException, AlreadyExistingException, IncompatibleVNFPackage, VimException,
          NotFoundException, PluginException, NetworkServiceIntegrityException, BadRequestException,
          InterruptedException, BadFormatException;

  /**
   * This operation allows submitting and validating the VNF Package from the Package Repository.
   *
   * @param link
   * @param projectId
   */
  VirtualNetworkFunctionDescriptor onboardFromPackageRepository(String link, String projectId)
      throws IOException, AlreadyExistingException, IncompatibleVNFPackage, VimException,
          NotFoundException, PluginException, NetworkServiceIntegrityException, BadRequestException,
          InterruptedException;

  /**
   * This operation allows disabling the VNF Package, so that it is not possible to instantiate any
   * further.
   */
  void disable();

  /** This operation allows enabling the VNF Package. */
  void enable();

  /** This operation allows updating the VNF Package. */
  VNFPackage update(String id, VNFPackage pack_new, String projectId) throws NotFoundException;

  VNFPackage query(String id, String projectId);

  /** This operation is used to query information on VNF Packages. */
  Iterable<VNFPackage> query();

  /** This operation is used to query information on VNF Packages. */
  Iterable<VNFPackageMetadata> query(
      String name,
      String vendor,
      String version,
      String nfvoVersion,
      String vnfmType,
      String osId,
      String osVersion,
      String osArchitecture,
      String tag,
      String projectId);

  /** This operation is used to remove a disabled VNF Package. */
  void delete(String id, String projectId) throws WrongAction;

  Script updateScript(Script script, String vnfPackageId)
      throws NotFoundException, BadFormatException, ExecutionException, InterruptedException;

  Iterable<VNFPackage> queryByProjectId(String projectId);
}
