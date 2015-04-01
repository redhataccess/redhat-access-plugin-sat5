package com.redhat.telemetry.integration.sat5;

import java.io.File;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.servlet.ServletContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.CookieParam;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.ConfigurationException;

@Path("/config")
public class ConfigService {
  @Context ServletContext context;


  @GET
  @Path("/general")
  @Produces(MediaType.APPLICATION_JSON)
  public Config getConfig(
      @CookieParam("pxt-session-cookie") String sessionKey,
      @QueryParam("satellite_user") String satelliteUser) 
          throws ConfigurationException, MalformedURLException {

    if (userIsAdmin(sessionKey, satelliteUser)) {
      PropertiesConfiguration properties = new PropertiesConfiguration();
      properties.load(context.getResourceAsStream(Constants.PROPERTIES_URL));
      String username = properties.getString(Constants.USERNAME_PROPERTY);
      boolean enabled = properties.getBoolean(Constants.ENABLED_PROPERTY);
      Config config = new Config(enabled, username, "");
      return config;
    } else {
      throw new ForbiddenException("Must be satellite admin.");
    }   
  }

  @POST
  @Path("/general")
  @Consumes(MediaType.APPLICATION_JSON)
  public Response postConfig(
      Config config,
      @CookieParam("pxt-session-cookie") String sessionKey,
      @QueryParam("satellite_user") String satelliteUser) 
          throws ConfigurationException, MalformedURLException {

    if (!userIsAdmin(sessionKey, satelliteUser)) {
      throw new ForbiddenException("Must be satellite admin.");
    }
    if (config.getEnabled()) {
      createRepo(sessionKey);
      createChannel(sessionKey);
      createConfigChannel(sessionKey);
    } else {
      //TODO: push config to disable reporting?
    }
    PropertiesConfiguration properties = new PropertiesConfiguration();
    properties.setFile(new File(context.getRealPath(Constants.PROPERTIES_URL)));
    properties.setProperty(Constants.ENABLED_PROPERTY, config.getEnabled());
    properties.setProperty(Constants.USERNAME_PROPERTY, config.getUsername());
    properties.setProperty(Constants.PASSWORD_PROPERTY, config.getPassword());
    properties.save();
    return Response.status(200).build();
  }

  @SuppressWarnings("unchecked")
  @GET
  @Path("/systems")
  @Produces(MediaType.APPLICATION_JSON)
  public ArrayList<SatSystem> getSystems(
      @CookieParam("pxt-session-cookie") String sessionKey) {

    Object[] apiSystems = SatApi.listSystems(sessionKey);
    ArrayList<SatSystem> satSystems = new ArrayList<SatSystem>();
    for (Object apiSys : apiSystems) {
      HashMap<Object, Object> apiSysMap = (HashMap<Object, Object>) apiSys;
      Object systemDetails = SatApi.getSystemDetails(sessionKey, (int) apiSysMap.get("id"));
      HashMap<Object, Object> systemDetailsMap = (HashMap<Object, Object>) systemDetails;
      String systemVersion = (String) systemDetailsMap.get("release");
      int systemId = (int) apiSysMap.get("id");
      SystemInstallStatus installationStatus = new SystemInstallStatus();
      if (systemVersion.equals("6Server")) {
        if (channelExists(sessionKey)) {
          if (packageInstalled(sessionKey, systemId)) {
            installationStatus.setRpmInstalled(true);
          };
        }
      } 

      SatSystem satSys = new SatSystem(
          systemId,
          (String) apiSysMap.get("name"),
          systemVersion,
          installationStatus,
          true);
      satSystems.add(satSys);
    }

    return satSystems;
  }

  @SuppressWarnings("unchecked")
  @POST
  @Path("/systems")
  @Consumes(MediaType.APPLICATION_JSON)
  public ArrayList<SatSystem> postSystems(
      @CookieParam("pxt-session-cookie") String sessionKey,
      ArrayList<SatSystem> systems) {

    //grab the redhat-access-proactive packageId from the channel
    Object[] channelPackages = 
      SatApi.listAllPackagesInChannel(sessionKey, Constants.CHANNEL_LABEL);
    int packageId = -1;
    for (Object channelPackage : channelPackages) {
      HashMap<Object, Object> channelPackageMap = (HashMap<Object, Object>) channelPackage;
      String packageName = (String) channelPackageMap.get("name");
      if (packageName.equals(Constants.PACKAGE_NAME)) {
        packageId = (int) channelPackageMap.get("id");
      }
    }

    //install the redhat-access-proactive package on the system
    for (SatSystem sys : systems) {
      if (sys.getVersion().equals("6Server")) {
        //list existing channels system is subscribed to
        Object[] systemChannels = SatApi.listSystemChannels(sessionKey, sys.getId());
        ArrayList<String> systemChannelLabels = new ArrayList<String>();
        for (Object systemChannel : systemChannels) {
          String label = 
            (String)((HashMap<Object, Object>) systemChannel).get("label");
          systemChannelLabels.add(label);
        }
        systemChannelLabels.add(Constants.CHANNEL_LABEL);

        //subscribe system to Red Hat Insights child channel
        SatApi.setChildChannels(sessionKey, sys.getId(), systemChannelLabels);

        //install the package
        ArrayList<Integer> packageIds = new ArrayList<Integer>();
        packageIds.add(packageId);
        SatApi.schedulePackageInstall(sessionKey, sys.getId(), packageIds, 60000);

        //subscribe system to Red Hat Insights config channel
        ArrayList<Integer> systemIds = new ArrayList<Integer>();
        systemIds.add(sys.getId());
        ArrayList<String> channelLabels = new ArrayList<String>();
        channelLabels.add(Constants.CONFIG_CHANNEL_LABEL);
        SatApi.addConfigChannelsToSystem(sessionKey, systemIds, channelLabels, true);
      }
    }
    SatApi.deployAllSystems(sessionKey, Constants.CONFIG_CHANNEL_LABEL);
    return systems;
  };

  @SuppressWarnings("unchecked")
  private boolean packageInstalled(String sessionKey, int systemId) {
    Object[] installedPackages = 
      SatApi.listInstalledPackagesFromChannel(sessionKey, systemId, Constants.CHANNEL_LABEL);
    boolean found = false;
    for (Object installedPackage : installedPackages) {
      HashMap<Object, Object> packageMap = (HashMap<Object, Object>) installedPackage;
      found = true;
    }
    return found;
  }
  @SuppressWarnings("unchecked")
  private boolean channelExists(String sessionKey) {
    Object[] channels = SatApi.listSoftwareChannels(sessionKey);
    boolean response = false;
    for (Object channel : channels) {
      HashMap<Object, Object> channelMap = (HashMap<Object, Object>) channel;
      String label = (String) channelMap.get("label");
      if (label.equals(Constants.CHANNEL_LABEL)) {
        response = true;
      }
    }
    return response;
  }

  @SuppressWarnings("unchecked")
  private boolean repoExists(String sessionKey) {
    Object[] repos = SatApi.listUserRepos(sessionKey);
    boolean exists = false;
    for (Object repo : repos) {
      HashMap<Object, Object> repoMap = (HashMap<Object, Object>) repo;
      String label = (String) repoMap.get("label"); 
      if (label.equals(Constants.REPO_LABEL)) {
        exists = true;
      }
    }
    return exists;
  }

  /**
   * Create the repo via sat5 api if it doesn't already exist
   * Returns the new or existing repo id
   */
  private void createRepo(String sessionKey) {
    if (!repoExists(sessionKey)) {
      SatApi.createRepo(
          sessionKey, 
          Constants.REPO_LABEL, 
          "YUM", 
          Constants.REPO_URL);
    } 
  }

  private boolean createChannel(String sessionKey) {
    boolean response = false;
    if (!channelExists(sessionKey)) {
      int created = SatApi.createChannel(
          sessionKey, 
          Constants.CHANNEL_LABEL,
          "x86_64 - Red Hat Insights",
          "Red Hat Insights is the coolest",
          "channel-x86_64",
          "rhel-x86_64-server-6");
      if (created == 0) {
        response = false;
      } else {
        response = true;
        //associate repo with this channel
        SatApi.associateRepo(sessionKey, Constants.CHANNEL_LABEL, Constants.REPO_LABEL);
        SatApi.syncRepo(sessionKey, Constants.CHANNEL_LABEL);
      }
    } else {
      //channel already created
      //TODO: verify the repo is associated?
      response = true;
    }
    return response;
  }

  private void createConfigChannel(String sessionKey) {
    if (SatApi.configChannelExists(sessionKey, Constants.CONFIG_CHANNEL_LABEL) != 1) {
      SatApi.createConfigChannel(
          sessionKey, 
          Constants.CONFIG_CHANNEL_LABEL, 
          Constants.CONFIG_CHANNEL_NAME, 
          Constants.CONFIG_CHANNEL_DESCRIPTION);
      HashMap<String, Object> pathInfo = new  HashMap<String, Object>();
      pathInfo.put("contents", 
          "[redhat_access_proactive]" + 
          System.getProperty("line.separator") +
          "# Change log level, valid options DEBUG, INFO, WARNING, ERROR, CRITICAL. Default DEBUG" +
          System.getProperty("line.separator") +
          "#loglevel=DEBUG" + 
          System.getProperty("line.separator") +
          "# Change authentication method, valid options BASIC, CERT. Default BASIC" +
          System.getProperty("line.separator") +
          "authmethod=BASIC" + 
          System.getProperty("line.separator") +
          "# URL to send uploads to" + 
          System.getProperty("line.separator") +
          "upload_url=https://sat57.usersys.redhat.com/redhataccess/rs/telemetry" + 
          System.getProperty("line.separator") +
          "# URL to send API requests to" + 
          System.getProperty("line.separator") +
          "api_url=https://sat57.usersys.redhat.com/redhataccess/rs/telemetry/api" + 
          System.getProperty("line.separator") +
          "username=" +
          System.getProperty("line.separator") +
          "password=");
      pathInfo.put("contents_enc64", false);
      pathInfo.put("owner", "root");
      pathInfo.put("group", "root");
      pathInfo.put("permissions", "644");
      pathInfo.put("binary", false);
      SatApi.configCreateOrUpdatePath(
          sessionKey,
          Constants.CONFIG_CHANNEL_LABEL,
          Constants.CONFIG_PATH,
          false,
          pathInfo);
    }
  }

  private boolean userIsAdmin(String sessionKey, String username) {
    Object[] userRoles = SatApi.listUserRoles(sessionKey, username);
    boolean response = false;
    if (userRoles != null) {
      for (Object role : userRoles) {
        if (role.equals("satellite_admin")) {
          response = true;
        }
      }
    }
    return response;
  }
}
