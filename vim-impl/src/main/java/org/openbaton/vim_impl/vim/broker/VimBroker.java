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

package org.openbaton.vim_impl.vim.broker;

import java.util.HashMap;
import java.util.List;
import javax.annotation.PostConstruct;
import org.openbaton.catalogue.mano.common.DeploymentFlavour;
import org.openbaton.catalogue.nfvo.Quota;
import org.openbaton.catalogue.nfvo.Server;
import org.openbaton.catalogue.nfvo.viminstances.BaseVimInstance;
import org.openbaton.exceptions.PluginException;
import org.openbaton.exceptions.VimException;
import org.openbaton.nfvo.vim_interfaces.vim.Vim;
import org.openbaton.vim.drivers.interfaces.ClientInterfaces;
import org.openbaton.vim_impl.vim.GenericVIM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

/** Created by lto on 20/05/15. */
@Service
@Scope
@ConfigurationProperties
public class VimBroker implements org.openbaton.nfvo.vim_interfaces.vim.VimBroker {

  @Value("${nfvo.rabbit.management.port:15672}")
  private String managementPort;

  @Value("${spring.rabbitmq.host:localhost}")
  private String brokerIp;

  public String getBrokerIp() {
    return brokerIp;
  }

  public void setBrokerIp(String brokerIp) {
    this.brokerIp = brokerIp;
  }

  public String getPluginTimeout() {
    return pluginTimeout;
  }

  public void setPluginTimeout(String pluginTimeout) {
    this.pluginTimeout = pluginTimeout;
  }

  public String getRabbitUsername() {
    return rabbitUsername;
  }

  public void setRabbitUsername(String rabbitUsername) {
    this.rabbitUsername = rabbitUsername;
  }

  public String getRabbitPassword() {
    return rabbitPassword;
  }

  public void setRabbitPassword(String rabbitPassword) {
    this.rabbitPassword = rabbitPassword;
  }

  public String getPort() {
    return port;
  }

  public void setPort(String port) {
    this.port = port;
  }

  public String getVirtualHost() {
    return virtualHost;
  }

  public void setVirtualHost(String virtualHost) {
    this.virtualHost = virtualHost;
  }

  @Value("${nfvo.plugin.timeout:120000}")
  private String pluginTimeout;

  @Value("${spring.rabbitmq.username:admin}")
  private String rabbitUsername;

  @Value("${spring.rabbitmq.password:openbaton}")
  private String rabbitPassword;

  @Value("${nfvo.vim.drivers.allowInfiniteQuota:false}")
  private String allowInfiniteQuota;

  @Value("${spring.rabbitmq.port:5672}")
  private String port;

  @Value("${spring.rabbitmq.virtual-host:/}")
  private String virtualHost;

  private Logger log = LoggerFactory.getLogger(this.getClass());

  @Autowired private ConfigurableApplicationContext context;
  private HashMap<String, ClientInterfaces> clientInterfaces;

  public String getAllowInfiniteQuota() {
    return allowInfiniteQuota;
  }

  public void setAllowInfiniteQuota(String allowInfiniteQuota) {
    this.allowInfiniteQuota = allowInfiniteQuota;
  }

  public String getManagementPort() {
    return managementPort;
  }

  public void setManagementPort(String managementPort) {
    this.managementPort = managementPort;
  }

  @PostConstruct
  private void init() {
    this.clientInterfaces = new HashMap<>();
  }

  @Override
  public void addClient(ClientInterfaces client, String type) {
    log.info("Registered client of type: " + type);
    this.clientInterfaces.put(type, client);
  }

  @Override
  public ClientInterfaces getClient(String type) {
    return this.clientInterfaces.get(type);
  }

  @Override
  public Vim getVim(String type) throws PluginException {
    /* Needed only for test */
    try {
      port = String.valueOf(Integer.parseInt(port));
    } catch (NumberFormatException e) {
      port = "5672";
    }
    try {
      pluginTimeout = String.valueOf(Integer.parseInt(pluginTimeout));
    } catch (NumberFormatException e) {
      pluginTimeout = "120000";
    }
    String pluginName = null;
    if (type.contains(".")) {
      String[] split = type.split("\\.");
      type = split[0];
      pluginName = split[1];
    }
    return new GenericVIM(
        type,
        rabbitUsername,
        rabbitPassword,
        brokerIp,
        Integer.parseInt(port),
        virtualHost,
        this.managementPort,
        context,
        pluginName,
        Integer.parseInt(pluginTimeout));
  }

  @Override
  public Quota getLeftQuota(BaseVimInstance vimInstance) throws VimException, PluginException {
    Vim vim = getVim(vimInstance.getType());

    Quota maximalQuota = vim.getQuota(vimInstance);

    if (allowInfiniteQuota != null && allowInfiniteQuota.equalsIgnoreCase("true")) {
      if (maximalQuota.getInstances() == -1) {
        maximalQuota.setInstances(Integer.MAX_VALUE);
      }
      if (maximalQuota.getRam() == -1) {
        maximalQuota.setRam(Integer.MAX_VALUE);
      }
      if (maximalQuota.getCores() == -1) {
        maximalQuota.setCores(Integer.MAX_VALUE);
      }
      if (maximalQuota.getKeyPairs() == -1) {
        maximalQuota.setKeyPairs(Integer.MAX_VALUE);
      }
      if (maximalQuota.getFloatingIps() == -1) {
        maximalQuota.setFloatingIps(Integer.MAX_VALUE);
      }
    } else {
      if (maximalQuota.getInstances() < 0
          || maximalQuota.getRam() < 0
          || maximalQuota.getCores() < 0
          || maximalQuota.getKeyPairs() < 0
          || maximalQuota.getFloatingIps() < 0) {
        log.error(
            "Infinite quota are not allowed. Please set nfvo.vim.drivers.allowInfiniteQuota to true or change the "
                + "quota in your VIM installation");
        throw new VimException(
            "Infinite quota are not allowed. Please set nfvo.vim.drivers.allowInfiniteQuota to true or change the "
                + "quota in your VIM installation");
      }
    }

    List<Server> servers = vim.queryResources(vimInstance);
    log.debug("MaximalQuota is: " + maximalQuota);
    // Calculate used resource by servers (cpus, ram)
    for (Server server : servers) {
      // Subtract instances
      maximalQuota.setInstances(maximalQuota.getInstances() - 1);
      // Subtract used ram and cpus
      DeploymentFlavour flavor = server.getFlavor();
      maximalQuota.setRam(
          maximalQuota.getRam()
              - (flavor != null && flavor.getRam() != null ? flavor.getRam() : 0));
      maximalQuota.setCores(
          maximalQuota.getCores()
              - (flavor != null && flavor.getVcpus() != null ? flavor.getVcpus() : 0));
      // TODO add floating ips when quota command will work...
    }
    return maximalQuota;
  }
}
