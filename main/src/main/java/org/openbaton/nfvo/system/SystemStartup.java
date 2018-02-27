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

package org.openbaton.nfvo.system;

import static org.openbaton.nfvo.common.utils.rabbit.RabbitManager.createRabbitMqUser;
import static org.openbaton.nfvo.common.utils.rabbit.RabbitManager.setRabbitMqUserPermissions;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Properties;
import org.openbaton.catalogue.nfvo.Configuration;
import org.openbaton.catalogue.nfvo.ConfigurationParameter;
import org.openbaton.nfvo.repositories.ConfigurationRepository;
import org.openbaton.plugin.mgmt.PluginStartup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

/** Created by lto on 12/05/15. */
@Service
@Order(value = Ordered.HIGHEST_PRECEDENCE)
@ConfigurationProperties
class SystemStartup implements CommandLineRunner {

  private Logger log = LoggerFactory.getLogger(this.getClass());

  @Autowired private ConfigurationRepository configurationRepository;

  @Value("${nfvo.plugin.active.consumers:10}")
  private String numConsumers;

  @Value("${spring.rabbitmq.username:admin}")
  private String username;

  @Value("${spring.rabbitmq.password:openbaton}")
  private String password;

  @Value("${spring.rabbitmq.host:localhost}")
  private String brokerIp;

  @Value("${nfvo.rabbit.management.port:15672}")
  private String managementPort;

  @Value("${spring.rabbit.virtual-host:/}")
  private String virtualHost;

  @Value("${nfvo.plugin.installation-dir:./plugins}")
  private String pluginDir;

  @Value("${nfvo.plugin.wait:true}")
  private boolean waitForPlugin;

  @Value("${nfvo.plugin.install:true}")
  private boolean installPlugin;

  @Value("${spring.config.location:/etc/openbaton/openbaton-nfvo.properties}")
  private String propFileLocation;

  @Value("${nfvo.plugin.log.path:./plugin-logs}")
  private String pluginLogPath;

  @Value("${nfvo.rabbit.manager-registration-user.name:openbaton-manager-user}")
  private String managerRegistrationUserName;

  @Value("${nfvo.rabbit.manager-registration-user.password:openbaton}")
  private String getManagerRegistrationUserPassword;

  @Override
  public void run(String... args) throws Exception {
    log.info("Initializing OpenBaton");

    log.debug(Arrays.asList(args).toString());

    propFileLocation = propFileLocation.replace("file:", "");
    log.debug("Property file: " + propFileLocation);

    Properties properties = new Properties();
    try (InputStream is = new FileInputStream(propFileLocation)) {
      properties.load(is);
    }
    log.trace("Config Values are: " + properties.values());

    Configuration c = new Configuration();

    c.setName("system");
    c.setConfigurationParameters(new HashSet<>());

    /* Adding properties from file */
    for (Entry<Object, Object> entry : properties.entrySet()) {
      ConfigurationParameter cp = new ConfigurationParameter();
      cp.setConfKey((String) entry.getKey());
      cp.setValue((String) entry.getValue());
      c.getConfigurationParameters().add(cp);
    }

    /* Adding system properties */
    Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();
    for (NetworkInterface netint : Collections.list(nets)) {
      ConfigurationParameter cp = new ConfigurationParameter();
      log.trace("Display name: " + netint.getDisplayName());
      log.trace("Name: " + netint.getName());
      cp.setConfKey("ip-" + netint.getName().replaceAll("\\s", ""));
      Enumeration<InetAddress> inetAddresses = netint.getInetAddresses();
      for (InetAddress inetAddress : Collections.list(inetAddresses)) {
        if (inetAddress.getHostAddress().contains(".")) {
          log.trace("InetAddress: " + inetAddress.getHostAddress());
          cp.setValue(inetAddress.getHostAddress());
        }
      }
      log.trace("");
      c.getConfigurationParameters().add(cp);
    }

    configurationRepository.save(c);

    createRabbitMqUser(
        username,
        password,
        brokerIp,
        managementPort,
        managerRegistrationUserName,
        getManagerRegistrationUserPassword,
        virtualHost);

    setRabbitMqUserPermissions(
        username,
        password,
        brokerIp,
        managementPort,
        managerRegistrationUserName,
        virtualHost,
        "^amq\\.gen.*|amq\\.default$",
        "^amq\\.gen.*|amq\\.default$|nfvo.manager.handling|openbaton-exchange",
        "^amq\\.gen.*|amq\\.default$|nfvo.manager.handling|openbaton-exchange");

    if (installPlugin) {
      startPlugins(pluginDir);
    }
  }

  private void startPlugins(String folderPath) throws IOException {
    PluginStartup.startPluginRecursive(
        folderPath,
        waitForPlugin,
        brokerIp,
        "5672",
        Integer.parseInt(numConsumers),
        username,
        password,
        virtualHost,
        managementPort,
        pluginLogPath);
  }
}
