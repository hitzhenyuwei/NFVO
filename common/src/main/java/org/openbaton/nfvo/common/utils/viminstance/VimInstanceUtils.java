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

package org.openbaton.nfvo.common.utils.viminstance;

import java.util.*;
import java.util.stream.Collectors;
import org.apache.commons.net.util.SubnetUtils;
import org.openbaton.catalogue.mano.descriptor.InternalVirtualLink;
import org.openbaton.catalogue.mano.descriptor.NetworkServiceDescriptor;
import org.openbaton.catalogue.mano.descriptor.VNFDConnectionPoint;
import org.openbaton.catalogue.mano.descriptor.VirtualLinkDescriptor;
import org.openbaton.catalogue.mano.descriptor.VirtualNetworkFunctionDescriptor;
import org.openbaton.catalogue.mano.record.VirtualLinkRecord;
import org.openbaton.catalogue.nfvo.ImageStatus;
import org.openbaton.catalogue.nfvo.images.AWSImage;
import org.openbaton.catalogue.nfvo.images.BaseNfvImage;
import org.openbaton.catalogue.nfvo.images.DockerImage;
import org.openbaton.catalogue.nfvo.images.NFVImage;
import org.openbaton.catalogue.nfvo.networks.BaseNetwork;
import org.openbaton.catalogue.nfvo.networks.DockerNetwork;
import org.openbaton.catalogue.nfvo.networks.Network;
import org.openbaton.catalogue.nfvo.networks.Subnet;
import org.openbaton.catalogue.nfvo.viminstances.AmazonVimInstance;
import org.openbaton.catalogue.nfvo.viminstances.BaseVimInstance;
import org.openbaton.catalogue.nfvo.viminstances.DockerVimInstance;
import org.openbaton.catalogue.nfvo.viminstances.OpenstackVimInstance;
import org.openbaton.exceptions.BadRequestException;

public class VimInstanceUtils {
  public static void handlePrivateInfo(BaseVimInstance vim) {
    if (vim.getClass().getCanonicalName().equals(OpenstackVimInstance.class.getCanonicalName())) {
      ((OpenstackVimInstance) vim).setPassword("**********");
    } else if (vim.getClass()
        .getCanonicalName()
        .equals(DockerVimInstance.class.getCanonicalName())) {
      ((DockerVimInstance) vim).setCa("**********");
      ((DockerVimInstance) vim).setDockerKey("**********");
      ((DockerVimInstance) vim).setCert("**********");
    } else if (vim.getClass()
        .getCanonicalName()
        .equals(AmazonVimInstance.class.getCanonicalName())) {
      ((AmazonVimInstance) vim).setSecretKey("**********");
    }
  }

  public static String getVimNameWithoutAvailabilityZone(String vimName) {
    if (vimName.contains(":")) return vimName.split(":")[0];
    return vimName;
  }

  public static void updatePrivateInfo(BaseVimInstance vimNew, BaseVimInstance vimOld) {
    if (vimNew
        .getClass()
        .getCanonicalName()
        .equals(OpenstackVimInstance.class.getCanonicalName())) {
      if (((OpenstackVimInstance) vimNew).getPassword().equals("**********")
          || ((OpenstackVimInstance) vimNew).getPassword().isEmpty()
          || ((OpenstackVimInstance) vimNew).getPassword() == null) {
        ((OpenstackVimInstance) vimNew).setPassword(((OpenstackVimInstance) vimOld).getPassword());
      }
    } else if (vimNew
        .getClass()
        .getCanonicalName()
        .equals(DockerVimInstance.class.getCanonicalName())) {
      ((DockerVimInstance) vimNew).setCa(((DockerVimInstance) vimOld).getCa());
      ((DockerVimInstance) vimNew).setDockerKey(((DockerVimInstance) vimOld).getDockerKey());
      ((DockerVimInstance) vimNew).setCert(((DockerVimInstance) vimOld).getCert());
    } else if (vimNew
        .getClass()
        .getCanonicalName()
        .equals(AmazonVimInstance.class.getCanonicalName())) {
      if (((AmazonVimInstance) vimNew).getSecretKey().equals("**********")
          || ((AmazonVimInstance) vimNew).getSecretKey().isEmpty()
          || ((AmazonVimInstance) vimNew).getSecretKey() == null) {
        ((AmazonVimInstance) vimNew).setSecretKey(((AmazonVimInstance) vimOld).getSecretKey());
      }
    }
  }

  public static void updateNfvImage(BaseNfvImage nfvImageOld, BaseNfvImage nfvImageNew) {
    nfvImageOld.setCreated(nfvImageNew.getCreated());
    if (NFVImage.class.isInstance(nfvImageNew)) {
      NFVImage osImageNew = (NFVImage) nfvImageNew;
      NFVImage osImageOld = (NFVImage) nfvImageOld;
      osImageOld.setName(osImageNew.getName());
      osImageOld.setIsPublic(osImageNew.isPublic());
      osImageOld.setMinRam(osImageNew.getMinRam());
      osImageOld.setMinCPU(osImageNew.getMinCPU());
      osImageOld.setMinDiskSpace(osImageNew.getMinDiskSpace());
      osImageOld.setDiskFormat(osImageNew.getDiskFormat());
      osImageOld.setContainerFormat(osImageNew.getContainerFormat());

      osImageOld.setUpdated(osImageNew.getUpdated());
      ImageStatus imageStatus = osImageNew.getStatus();
      if (imageStatus != null) {
        osImageOld.setStatus(imageStatus.toString());
      } else {
        osImageOld.setStatus(ImageStatus.ACTIVE.toString());
      }
    } else if (DockerImage.class.isInstance(nfvImageNew)) {
      DockerImage dockerImageNew = (DockerImage) nfvImageNew;
      DockerImage dockerImageOld = (DockerImage) nfvImageOld;
      dockerImageOld.setTags(dockerImageNew.getTags());
    }
  }

  public static void updateBaseNetworks(BaseNetwork networkOld, BaseNetwork networkNew)
      throws BadRequestException {

    if (Network.class.isInstance(networkOld)) {
      Network osNetworkOld = (Network) networkOld;
      Network osNetworkNew = (Network) networkNew;
      osNetworkOld.setName(osNetworkNew.getName());
      osNetworkOld.setExternal(osNetworkNew.getExternal());
      osNetworkOld.setExtShared(osNetworkNew.getExternal());
      Set<Subnet> subnets_refreshed = new HashSet<>();
      Set<Subnet> subnetsNew = new HashSet<>();
      Set<Subnet> subnetsOld = new HashSet<>();
      if (osNetworkNew.getSubnets() == null) {
        throw new BadRequestException("New network: " + osNetworkNew.getName() + " has no subnets");
      } else if (osNetworkNew.getSubnets() == null) {
        osNetworkNew.setSubnets(new HashSet<Subnet>());
      }
      subnets_refreshed.addAll(osNetworkNew.getSubnets());
      if (osNetworkOld.getSubnets() == null) {
        osNetworkOld.setSubnets(new HashSet<Subnet>());
      }
      for (Subnet subnetNew : subnets_refreshed) {
        boolean found_subnet = false;
        for (Subnet subnetNfvo : osNetworkOld.getSubnets()) {
          if (subnetNfvo.getExtId().equals(subnetNew.getExtId())) {
            subnetNfvo.setName(subnetNew.getName());
            subnetNfvo.setNetworkId(subnetNew.getNetworkId());
            subnetNfvo.setGatewayIp(subnetNew.getGatewayIp());
            subnetNfvo.setCidr(subnetNew.getCidr());
            found_subnet = true;
            break;
          }
        }
        if (!found_subnet) {
          subnetsNew.add(subnetNew);
        }
      }
      for (Subnet subnetNfvo : osNetworkOld.getSubnets()) {
        boolean foundSubnet = false;
        for (Subnet subnet_new : subnets_refreshed) {
          if (subnetNfvo.getExtId().equals(subnet_new.getExtId())) {
            foundSubnet = true;
            break;
          }
        }
        if (!foundSubnet) {
          subnetsOld.add(subnetNfvo);
        }
      }
      osNetworkOld.getSubnets().addAll(subnetsNew);
      osNetworkOld.getSubnets().removeAll(subnetsOld);
    } else if (DockerNetwork.class.isInstance(networkOld)) {
      DockerNetwork dockerNetworkOld = (DockerNetwork) networkOld;
      DockerNetwork dockerNetworkNew = (DockerNetwork) networkNew;

      dockerNetworkOld.setDriver(dockerNetworkNew.getDriver());
      dockerNetworkOld.setDriver(dockerNetworkNew.getGateway());
      dockerNetworkOld.setScope(dockerNetworkNew.getScope());
      dockerNetworkOld.setSubnet(dockerNetworkNew.getSubnet());
    }
  }

  /**
   * Returns a collection of images containing all the images which match the given name and are
   * active.
   *
   * @param vimInstance the VIM containing the images
   * @param imageName the image name to look for
   * @return a collection of matching images
   */
  public static Collection<BaseNfvImage> findActiveImagesByName(
      BaseVimInstance vimInstance, String imageName) {
    if (vimInstance instanceof OpenstackVimInstance) {
      return ((OpenstackVimInstance) vimInstance)
          .getImages()
          .stream()
          .filter(
              i ->
                  ((NFVImage) i).getName() != null
                      && ((NFVImage) i).getName().equals(imageName)
                      && (((NFVImage) i).getStatus() == null
                          || ((NFVImage) i).getStatus().ordinal() == ImageStatus.ACTIVE.ordinal()))
          .collect(Collectors.toList());
    } else if (vimInstance instanceof DockerVimInstance) {
      return ((DockerVimInstance) vimInstance)
          .getImages()
          .stream()
          .filter(
              i ->
                  ((DockerImage) i).getTags() != null
                      && !((DockerImage) i).getTags().isEmpty()
                      && ((DockerImage) i).getTags().contains(imageName))
          .collect(Collectors.toList());
    } else if (vimInstance instanceof AmazonVimInstance) {
      // in case of Amazon Instance the image check is delegated to amazon plugin
      AWSImage skipImage = new AWSImage();
      skipImage.setExtId(imageName);
      return Collections.singletonList(skipImage);
    } else {
      return vimInstance
          .getImages()
          .stream()
          .filter(i -> i.getExtId().equals(imageName))
          .collect(Collectors.toList());
    }
  }

  public static BaseNetwork createBaseNetwork(
      NetworkServiceDescriptor networkServiceDescriptor,
      VirtualNetworkFunctionDescriptor virtualNetworkFunctionDescriptor,
      VNFDConnectionPoint vnfdConnectionPoint,
      BaseVimInstance vimInstance)
      throws BadRequestException {
    if (vimInstance instanceof OpenstackVimInstance) {
      Network network = new Network();
      HashSet<Subnet> subnets = new HashSet<>();
      Subnet subnet = new Subnet();
      subnet.setName(String.format("%s_subnet", vnfdConnectionPoint.getVirtual_link_reference()));
      subnet.setCidr(
          getCidrFromVLName(
              vnfdConnectionPoint.getVirtual_link_reference(),
              networkServiceDescriptor,
              virtualNetworkFunctionDescriptor));
      subnets.add(subnet);
      network.setSubnets(subnets);
      network.setName(vnfdConnectionPoint.getVirtual_link_reference());
      return network;
    } else if (vimInstance instanceof DockerVimInstance) {
      DockerNetwork networkdc = new DockerNetwork();
      networkdc.setName(vnfdConnectionPoint.getVirtual_link_reference());
      networkdc.setSubnet(
          getCidrFromVLName(
              vnfdConnectionPoint.getVirtual_link_reference(),
              networkServiceDescriptor,
              virtualNetworkFunctionDescriptor));
      return networkdc;
    } else {
      BaseNetwork networkb = new BaseNetwork();
      networkb.setName(vnfdConnectionPoint.getVirtual_link_reference());
      return networkb;
    }
  }

  public static BaseNetwork createBaseNetwork(
      NetworkServiceDescriptor networkServiceDescriptor,
      VirtualNetworkFunctionDescriptor virtualNetworkFunctionDescriptor,
      VirtualLinkRecord vlr,
      BaseVimInstance vimInstance)
      throws BadRequestException {
    if (vimInstance instanceof OpenstackVimInstance) {
      Network network = new Network();
      HashSet<Subnet> subnets = new HashSet<>();
      Subnet subnet = new Subnet();
      subnet.setName(String.format("%s_subnet", vlr.getName()));
      subnet.setDns(vlr.getDns());
      subnet.setCidr(
          getCidrFromVLName(
              vlr.getName(), networkServiceDescriptor, virtualNetworkFunctionDescriptor));
      subnets.add(subnet);
      network.setSubnets(subnets);
      network.setName(vlr.getName());
      return network;
    } else if (vimInstance instanceof DockerVimInstance) {
      DockerNetwork networkdc = new DockerNetwork();
      networkdc.setMetadata(getMetadataFromVLName(vlr.getName(), networkServiceDescriptor));
      networkdc.setName(vlr.getName());
      networkdc.setSubnet(
          getCidrFromVLName(
              vlr.getName(), networkServiceDescriptor, virtualNetworkFunctionDescriptor));
      return networkdc;
    } else {
      BaseNetwork networkb = new BaseNetwork();
      networkb.setName(vlr.getName());
      return networkb;
    }
  }

  private static Map<String, String> getMetadataFromVLName(
      String virtual_link_reference, NetworkServiceDescriptor networkServiceDescriptor) {
    for (VirtualLinkDescriptor vld : networkServiceDescriptor.getVld()) {
      if (vld.getName().equals(virtual_link_reference)) {
        return vld.getMetadata();
      }
    }
    return new HashMap<>();
  }

  private static String getCidrFromVLName(
      String virtual_link_reference,
      NetworkServiceDescriptor networkServiceDescriptor,
      VirtualNetworkFunctionDescriptor virtualNetworkFunctionDescriptor)
      throws BadRequestException {
    for (VirtualLinkDescriptor vld : networkServiceDescriptor.getVld()) {
      if (vld.getName().equals(virtual_link_reference)) {
        return vld.getCidr();
      }
    }
    for (InternalVirtualLink ivl : virtualNetworkFunctionDescriptor.getVirtual_link()) {
      if (ivl.getName().equals(virtual_link_reference)) {
        return ivl.getCidr();
      }
    }
    throw new BadRequestException(
        String.format(
            "Connection Point with Virtual link reference %s points to non defined Virtual Link. Please add a VL in the "
                + "VNFD or NSD or change the VL reference",
            virtual_link_reference));
  }

  public static boolean isVNFDConnectionPointExisting(
      VNFDConnectionPoint vnfdConnectionPoint, BaseNetwork network) {
    if (network.getName().equals(vnfdConnectionPoint.getVirtual_link_reference())
        || network.getExtId().equals(vnfdConnectionPoint.getVirtual_link_reference())
        || network.getExtId().equals(vnfdConnectionPoint.getVirtual_link_reference_id())) {
      if (vnfdConnectionPoint.getFixedIp() != null
          && !vnfdConnectionPoint.getFixedIp().equals("")) {
        if (network instanceof Network) {
          Network osNet = (Network) network;
          return osNet.getSubnets() == null
              || osNet.getSubnets().size() <= 0
              || osNet
                  .getSubnets()
                  .stream()
                  .anyMatch(
                      subnet ->
                          new SubnetUtils(subnet.getCidr())
                              .getInfo()
                              .isInRange(vnfdConnectionPoint.getFixedIp()));
        } else if (network instanceof DockerNetwork) {
          DockerNetwork dockerNetwork = (DockerNetwork) network;
          return new SubnetUtils(dockerNetwork.getSubnet())
              .getInfo()
              .isInRange(vnfdConnectionPoint.getFixedIp());
        } else return true;
      } else {
        return true;
      }
    } else return false;
  }

  public static boolean isVLRExisting(
      VirtualLinkRecord virtualLinkRecord, BaseNetwork network, boolean dedicatedNetworks) {
    boolean checkNamesAndIds =
        network.getName().equals(virtualLinkRecord.getName())
            || network.getExtId().equals(virtualLinkRecord.getName())
            || network.getExtId().equals(virtualLinkRecord.getExtId());
    if (dedicatedNetworks)
      return virtualLinkRecord.getExtId() != null
          && !virtualLinkRecord.getExtId().equals("")
          && checkNamesAndIds;
    else return checkNamesAndIds;
  }
}
