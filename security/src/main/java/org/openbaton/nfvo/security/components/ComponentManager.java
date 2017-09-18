package org.openbaton.nfvo.security.components;

import static org.openbaton.nfvo.common.utils.rabbit.RabbitManager.createRabbitMqUser;
import static org.openbaton.nfvo.common.utils.rabbit.RabbitManager.removeRabbitMqUser;
import static org.openbaton.nfvo.common.utils.rabbit.RabbitManager.setRabbitMqUserPermissions;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import org.openbaton.catalogue.nfvo.ManagerCredentials;
import org.openbaton.catalogue.nfvo.VnfmManagerEndpoint;
import org.openbaton.catalogue.security.Project;
import org.openbaton.catalogue.security.Role;
import org.openbaton.catalogue.security.ServiceMetadata;
import org.openbaton.exceptions.MissingParameterException;
import org.openbaton.exceptions.NotFoundException;
import org.openbaton.nfvo.common.utils.key.KeyHelper;
import org.openbaton.nfvo.repositories.ManagerCredentialsRepository;
import org.openbaton.nfvo.repositories.ProjectRepository;
import org.openbaton.nfvo.repositories.ServiceRepository;
import org.openbaton.nfvo.repositories.VnfmEndpointRepository;
import org.openbaton.nfvo.security.authentication.OAuth2AuthorizationServerConfig;
import org.openbaton.vnfm.interfaces.register.VnfmRegister;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.stereotype.Service;

//import java.util.Base64;

/** Created by lto on 04/04/2017. */
@Service
@ConfigurationProperties
public class ComponentManager implements org.openbaton.nfvo.security.interfaces.ComponentManager {

  private Logger log = LoggerFactory.getLogger(this.getClass());

  @Autowired private OAuth2AuthorizationServerConfig serverConfig;
  //  @Autowired private TokenStore tokenStore;
  //  @Autowired private DefaultTokenServices tokenServices;
  @Autowired private Gson gson;
  @Autowired private ServiceRepository serviceRepository;
  @Autowired private ManagerCredentialsRepository managerCredentialsRepository;
  @Autowired private VnfmRegister vnfmRegister;

  @Value("${nfvo.security.service.token.validity:31556952}")
  private int serviceTokenValidityDuration;

  @Value("${nfvo.rabbit.brokerIp:localhost}")
  private String brokerIp;

  @Value("${nfvo.rabbit.managementPort:15672}")
  private String managementPort;

  @Value("${spring.rabbitmq.password:openbaton}")
  private String rabbitPassword;

  @Value("${spring.rabbitmq.username:admin}")
  private String rabbitUsername;

  @Value("${spring.rabbitmq.virtual-host:/}")
  private String vhost;

  @Autowired private VnfmEndpointRepository vnfmManagerEndpointRepository;
  @Autowired private ProjectRepository projectRepository;

  /*
   * Service related operations
   */

  /**
   * @param body
   * @return an encrypted token
   * @throws NotFoundException
   * @throws IllegalBlockSizeException
   * @throws InvalidKeyException
   * @throws BadPaddingException
   * @throws NoSuchAlgorithmException
   * @throws NoSuchPaddingException
   */
  @Override
  public String registerService(String body)
      throws IllegalBlockSizeException, InvalidKeyException, BadPaddingException,
          NoSuchAlgorithmException, NoSuchPaddingException, NotFoundException {

    ServiceMetadata service = null;
    String unencryptedBody = null;
    for (ServiceMetadata serviceMetadata : serviceRepository.findAll()) {
      try {
        unencryptedBody = KeyHelper.decryptNew(body, serviceMetadata.getKeyValue());
      } catch (NoSuchPaddingException
          | NoSuchAlgorithmException
          | InvalidKeyException
          | BadPaddingException
          | IOException
          | IllegalBlockSizeException e) {
        e.printStackTrace();
        continue;
      }

      service = serviceMetadata;
      break;
    }
    if (unencryptedBody == null)
      throw new NotFoundException("Could not decrypt the body, did you enabled the service?");

    JsonObject bodyJson = gson.fromJson(unencryptedBody, JsonObject.class);

    String serviceName = bodyJson.getAsJsonPrimitive("name").getAsString();
    String action = bodyJson.getAsJsonPrimitive("action").getAsString();

    if (!service.getName().equals(serviceName)) {
      log.error(
          "The name of the found Service does not match to the requested name " + serviceName);
      throw new NotFoundException(
          "The name of the found Service does not match to the requested name " + serviceName);
    }

    if (action.toLowerCase().equals("register")) {
      if (service.getToken() != null && !service.getToken().equals("")) {
        if (service.getTokenExpirationDate() > (new Date()).getTime()) return service.getToken();
      }
      OAuth2AccessToken token = serverConfig.getNewServiceToken(serviceName);
      service.setToken(KeyHelper.encryptNew(token.getValue(), service.getKeyValue()));
      service.setTokenExpirationDate(token.getExpiration().getTime());
      serviceRepository.save(service);
      return service.getToken();

    } else if (action.toLowerCase().equals("remove") || action.toLowerCase().equals("delete")) {
      serviceRepository.delete(service);
      log.info("Removed service " + serviceName);
      return null;
    } else {
      log.error("Action " + action + " unknown!");
      throw new RuntimeException("Action " + action + " unknown!");
    }
  }

  @Override
  public String createService(String serviceName, String projectId, List<String> projects)
      throws NoSuchAlgorithmException, IOException, NotFoundException, MissingParameterException {
    for (ServiceMetadata serviceMetadata : serviceRepository.findAll()) {
      if (serviceMetadata.getName().equals(serviceName)) {
        log.debug("Service " + serviceName + " already exists.");
        return serviceMetadata.getKeyValue();
      }
    }
    ServiceMetadata serviceMetadata = new ServiceMetadata();
    serviceMetadata.setRoles(new HashSet<>());

    if (projects.isEmpty()) {
      throw new MissingParameterException("Project list must not be empty");
    }

    if (projects.size() == 1 && projects.get(0).equals("*")) {
      Role r = new Role();
      r.setRole(Role.RoleEnum.ADMIN);
      r.setProject("*");
      serviceMetadata.getRoles().add(r);
    } else {

      for (String project : projects) {
        Role r = new Role();
        r.setRole(Role.RoleEnum.USER);
        Project pr = projectRepository.findFirstById(project);
        pr = pr == null ? projectRepository.findFirstByName(project) : pr;
        if (pr == null) {
          log.error("Project with id or name " + project + " not found");
          throw new NotFoundException("Project with id or name " + project + " not found");
        }
        r.setProject(pr.getName());
        serviceMetadata.getRoles().add(r);
      }
    }

    serviceMetadata.setName(serviceName);
    serviceMetadata.setKeyValue(KeyHelper.genKey());
    log.debug("Saving ServiceMetadata: " + serviceMetadata);
    serviceRepository.save(serviceMetadata);
    return serviceMetadata.getKeyValue();
  }

  @Override
  public boolean isService(String tokenToCheck)
      throws InvalidKeyException, BadPaddingException, NoSuchAlgorithmException,
          IllegalBlockSizeException, NoSuchPaddingException {
    for (ServiceMetadata serviceMetadata : serviceRepository.findAll()) {
      if (serviceMetadata.getToken() != null && !serviceMetadata.getToken().equals("")) {
        String encryptedServiceToken = serviceMetadata.getToken();
        String keyData = serviceMetadata.getKeyValue();
        try {
          if (KeyHelper.decryptNew(encryptedServiceToken, keyData).equals(tokenToCheck))
            return true;
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    }
    return false;
  }

  @Override
  public Iterable<ServiceMetadata> listServices() {
    return serviceRepository.findAll();
  }

  @Override
  public void removeService(String id) {
    //TODO remove also associated toker
    ServiceMetadata serviceMetadataToRemove = serviceRepository.findById(id);
    log.debug("Found service: " + serviceMetadataToRemove);
    //  if (serviceMetadataToRemove.getToken() != null)
    //      serverConfig.tokenServices().revokeToken(serviceMetadataToRemove.getToken());
    serviceRepository.delete(id);
  }

  /*
   * Manager related operations
   */

  public String enableManager(byte[] message) {
    return enableManager(new String(message));
  }

  /**
   * Handles the registration requests of VNFMs and returns a ManagerCredential object from which
   * the VNFMs can get the rabbitmq username and password.
   *
   * @param message
   * @return
   * @throws IOException
   */
  @Override
  public String enableManager(String message) {
    try {
      // deserialize message
      JsonObject body = gson.fromJson(message, JsonObject.class);
      if (!body.has("action")) {
        log.error("Could not process Json message. The 'action' property is missing.");
        return null;
      }
      JsonElement vnfmManagerEndpoint = body.get("vnfmManagerEndpoint");
      if (body.get("action").getAsString().toLowerCase().equals("register")) {

        // register plugin or vnfm
        if (!body.has("type")) {
          log.error("Could not process Json message. The 'type' property is missing.");
          return null;
        }
        String username = body.get("type").getAsString();
        String password = org.apache.commons.lang.RandomStringUtils.randomAlphanumeric(16);

        ManagerCredentials managerCredentials =
            managerCredentialsRepository.findFirstByRabbitUsername(username);
        VnfmManagerEndpoint endpoint = null;
        if (managerCredentials != null) {
          log.warn("Manager already registered.");
          return gson.toJson(managerCredentials);
        } else {
          managerCredentials = new ManagerCredentials();
          if (vnfmManagerEndpoint != null)
            if (vnfmManagerEndpoint.isJsonPrimitive())
              endpoint =
                  gson.fromJson(vnfmManagerEndpoint.getAsString(), VnfmManagerEndpoint.class);
            else endpoint = gson.fromJson(vnfmManagerEndpoint, VnfmManagerEndpoint.class);
        }

        //          String regexOpenbaton = "(^nfvo)";
        //          String regexManager = "(^" + username + ")|(openbaton-exchange)";
        //          String regexBoth = regexOpenbaton + "|" + regexManager;

        String configurePermissions =
            "^amq\\.gen.*|amq\\.default$|(" + username + ")|(nfvo." + username + ".actions)";
        String writePermissions =
            "^amq\\.gen.*|amq\\.default$|("
                + username
                + ")|(vnfm.nfvo.actions)|(vnfm.nfvo.actions.reply)|(nfvo."
                + username
                + ".actions)|(openbaton-exchange)";
        String readPermissions =
            "^amq\\.gen.*|amq\\.default$|(nfvo."
                + username
                + ".actions)|("
                + username
                + ")|(openbaton-exchange)";

        createRabbitMqUser(
            rabbitUsername, rabbitPassword, brokerIp, managementPort, username, password, vhost);
        try {
          setRabbitMqUserPermissions(
              rabbitUsername,
              rabbitPassword,
              brokerIp,
              managementPort,
              username,
              vhost,
              configurePermissions,
              writePermissions,
              readPermissions);
        } catch (Exception e) {
          try {
            removeRabbitMqUser(rabbitUsername, rabbitPassword, brokerIp, managementPort, username);
          } catch (Exception e2) {
            log.error("Clean up failed. Could not remove RabbitMQ user " + username);
            e2.printStackTrace();
          }
          throw e;
        }

        managerCredentials.setRabbitUsername(username);
        managerCredentials.setRabbitPassword(password);
        managerCredentials = managerCredentialsRepository.save(managerCredentials);
        if (endpoint != null) vnfmManagerEndpointRepository.save(endpoint);
        log.info("Registered a new manager.");
        return gson.toJson(managerCredentials);
      } else if (body.get("action").getAsString().toLowerCase().equals("unregister")
          || body.get("action").getAsString().toLowerCase().equals("deregister")) {

        if (!body.has("username")) {
          log.error("Could not process Json message. The 'username' property is missing.");
          return null;
        }
        if (!body.has("password")) {
          log.error("Could not process Json message. The 'password' property is missing.");
          return null;
        }
        String username = body.get("username").getAsString();
        ManagerCredentials managerCredentials =
            managerCredentialsRepository.findFirstByRabbitUsername(username);
        if (managerCredentials == null) {
          log.error("Did not find manager with name " + body.get("username"));
          return null;
        }
        if (body.get("password").getAsString().equals(managerCredentials.getRabbitPassword())) {
          managerCredentialsRepository.delete(managerCredentials);
          // if message comes from a vnfm, remove the endpoint
          if (body.has("vnfmManagerEndpoint"))
            vnfmRegister.unregister(gson.fromJson(vnfmManagerEndpoint, VnfmManagerEndpoint.class));

          removeRabbitMqUser(rabbitUsername, rabbitPassword, brokerIp, managementPort, username);
        } else {
          log.warn(
              "Some manager tried to unregister with a wrong password! or maybe i have an inconsistent DB...most probably... ;( ");
        }
        return null;
      } else return null;
    } catch (Exception e) {
      log.error("Exception while enabling manager or plugin.");
      e.printStackTrace();
      return null;
    }
  }

  @Override
  public void removeTokens() {
    for (ServiceMetadata serviceMetadata : serviceRepository.findAll()) {
      serviceMetadata.setToken(null);
      serviceRepository.save(serviceMetadata);
    }
  }
}
