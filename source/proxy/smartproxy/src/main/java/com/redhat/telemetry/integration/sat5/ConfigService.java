package com.redhat.telemetry.integration.sat5;

import java.io.File;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

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

  private static final String REPO_LABEL = "Red Hat Insights";
  private static final String CHANNEL_LABEL = "rh-insights-x86_64";
  private static final String REPO_URL = 
    "http://people.redhat.com/dvarga/projects/redhat_access_proactive/6Server/";
  private static final String PROPERTIES_URL = "WEB-INF/insights.properties";
  private static final String ENABLED_PROPERTY = "enabled";
  private static final String USERNAME_PROPERTY = "username";
  private static final String PASSWORD_PROPERTY = "password";
  private static final String PACKAGE_NAME = "redhat-access-proactive";
  private static final String CONFIG_CHANNEL_LABEL = "rh-insights-config";
  private static final String CONFIG_CHANNEL_NAME = 
    "Red Hat Insights client configuration";
  private static final String CONFIG_CHANNEL_DESCRIPTION = 
    "Red Hat Insights client configuration";
  private static final String CONFIG_PATH = 
    "/etc/redhat_access_proactive/redhat_access_proactive.conf";

  @GET
  @Path("/general")
  @Produces(MediaType.APPLICATION_JSON)
  public Config getConfig(
      @CookieParam("pxt-session-cookie") String sessionKey,
      @QueryParam("satellite_user") String satelliteUser) 
          throws ConfigurationException, MalformedURLException {

    if (userIsAdmin(sessionKey, satelliteUser)) {
      PropertiesConfiguration properties = new PropertiesConfiguration();
      properties.load(context.getResourceAsStream(PROPERTIES_URL));
      String username = properties.getString(USERNAME_PROPERTY);
      boolean enabled = properties.getBoolean(ENABLED_PROPERTY);
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

    if (userIsAdmin(sessionKey, satelliteUser)) {
      PropertiesConfiguration properties = new PropertiesConfiguration();
      properties.load(context.getRealPath(PROPERTIES_URL));
      createRepo(sessionKey);
      createChannel(sessionKey);
      createConfigChannel(sessionKey);

      properties.setFile(new File(context.getRealPath(PROPERTIES_URL)));
      properties.setProperty(ENABLED_PROPERTY, config.getEnabled());
      properties.setProperty(USERNAME_PROPERTY, config.getUsername());
      properties.setProperty(PASSWORD_PROPERTY, config.getPassword());
      properties.save();

      return Response.status(200).build();
    } else {
      throw new ForbiddenException("Must be satellite admin.");
    }
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
      if (systemVersion.equals("6Server")) {
        Object[] installedPackages = 
          SatApi.listInstalledPackagesFromChannel(sessionKey, systemId, CHANNEL_LABEL);
      }

      SatSystem satSys = new SatSystem(
          systemId,
          (String) apiSysMap.get("name"),
          systemVersion);
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
      SatApi.listAllPackagesInChannel(sessionKey, CHANNEL_LABEL);
    int packageId = -1;
    for (Object channelPackage : channelPackages) {
      HashMap<Object, Object> channelPackageMap = (HashMap<Object, Object>) channelPackage;
      String packageName = (String) channelPackageMap.get("name");
      if (packageName.equals(PACKAGE_NAME)) {
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
        systemChannelLabels.add(CHANNEL_LABEL);

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
        channelLabels.add(CONFIG_CHANNEL_LABEL);
        SatApi.addConfigChannelsToSystem(sessionKey, systemIds, channelLabels, true);
      }
    }
    SatApi.deployAllSystems(sessionKey, CONFIG_CHANNEL_LABEL);
    return systems;
  };

  @SuppressWarnings("unchecked")
  private Map<String, Integer> channelsExist(String sessionKey) {
    Object[] channels = SatApi.listSoftwareChannels(sessionKey);
    Map<String, Integer> response = new HashMap<String, Integer>();
    response.put("rhel6", -1);
    response.put("rhel7", -1);
    for (Object channel : channels) {
      HashMap<Object, Object> channelMap = (HashMap<Object, Object>) channel;
      String label = (String) channelMap.get("label");
      if (label.equals("rhel6-telemetry-label")) {
        response.put("rhel6", 1);
      } else if (label.equals("rhel7-telemetry-label")) {
        response.put("rhel7", 1);
      }
    }
    return response;
  }
  @SuppressWarnings("unchecked")
  private boolean channelExists(String sessionKey) {
    Object[] channels = SatApi.listSoftwareChannels(sessionKey);
    boolean response = false;
    for (Object channel : channels) {
      HashMap<Object, Object> channelMap = (HashMap<Object, Object>) channel;
      String label = (String) channelMap.get("label");
      if (label.equals(CHANNEL_LABEL)) {
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
      if (label.equals(REPO_LABEL)) {
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
          REPO_LABEL, 
          "YUM", 
          REPO_URL);
    } 
  }

  private boolean createChannel(String sessionKey) {
    boolean response = false;
    if (!channelExists(sessionKey)) {
      int created = SatApi.createChannel(
          sessionKey, 
          CHANNEL_LABEL,
          "x86_64 - Red Hat Insights",
          "Red Hat Insights is the coolest",
          "channel-x86_64",
          "rhel-x86_64-server-6");
      if (created == 0) {
        response = false;
      } else {
        response = true;
        //associate repo with this channel
        SatApi.associateRepo(sessionKey, CHANNEL_LABEL, REPO_LABEL);
        SatApi.syncRepo(sessionKey, CHANNEL_LABEL);
      }
    } else {
      //channel already created
      //TODO: verify the repo is associated?
      response = true;
    }
    return response;
  }

  private void createConfigChannel(String sessionKey) {
    if (SatApi.configChannelExists(sessionKey, CONFIG_CHANNEL_LABEL) != 1) {
      SatApi.createConfigChannel(
          sessionKey, 
          CONFIG_CHANNEL_LABEL, 
          CONFIG_CHANNEL_NAME, 
          CONFIG_CHANNEL_DESCRIPTION);
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
          "upload_url=https://sat57.usersys.redhat.com/insights/rs/telemetry" + 
          System.getProperty("line.separator") +
          "# URL to send API requests to" + 
          System.getProperty("line.separator") +
          "api_url=https://sat57.usersys.redhat.com/insights/rs/telemetry/api" + 
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
          CONFIG_CHANNEL_LABEL,
          CONFIG_PATH,
          false,
          pathInfo);
    }
  }

  /**
   * Create the rhel6 and rhel7 channels and associate the repo to them
   * Returns a map of the new or existing repo IDs
   */
  private Map<String, Integer> createChannels(String sessionKey, int repoId) {
    Map<String, Integer> existingChannelsMap = channelsExist(sessionKey);
    if (existingChannelsMap.get("rhel6") == -1) {
      int rhel6Id = SatApi.createChannel(
          sessionKey, 
          "rhel6-telemetry-label",
          "rhel6 telemetry name",
          "rhel6 telemetry summary",
          "x86_64",
          "");
      existingChannelsMap.put("rhel6", rhel6Id);
    }
    if (existingChannelsMap.get("rhel7") == -1) {
      int rhel7Id = SatApi.createChannel(
          sessionKey, 
          "rhel7-telemetry-label",
          "rhel7 telemetry name",
          "rhel7 telemetry summary",
          "x86_64",
          "");
      existingChannelsMap.put("rhel7", rhel7Id);
    }
    return existingChannelsMap;
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
