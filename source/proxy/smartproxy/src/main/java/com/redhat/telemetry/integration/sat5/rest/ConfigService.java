package com.redhat.telemetry.integration.sat5.rest;

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

import com.redhat.telemetry.integration.sat5.json.Config;
import com.redhat.telemetry.integration.sat5.json.SatSystem;
import com.redhat.telemetry.integration.sat5.json.SystemInstallStatus;
import com.redhat.telemetry.integration.sat5.satellite.SatApi;
import com.redhat.telemetry.integration.sat5.util.Constants;

@Path("/config")
public class ConfigService {
  @Context ServletContext context;


  /**
   * Retrieve general config values
   */
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

  /**
   * Update general config values
   */
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
      createRepo(sessionKey, 6);
      createChannel(sessionKey, 6);
      createRepo(sessionKey, 7);
      createChannel(sessionKey, 7);
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

  /**
   * Retrieve a list of systems visible to the user
   * Includes the installation status for each system
   */
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
      boolean validSystem = false;
      boolean enabled = false;
      int version = -1;
      if (systemVersion.equals(Constants.VERSION_RHEL6_SERVER)) {
        version = 6;
      } else if (systemVersion.equals(Constants.VERSION_RHEL7_SERVER)) {
        version = 7;
      }
      if (version != -1) {
        validSystem = true;
        if (channelExists(sessionKey, version)) {
          if (rpmInstalled(sessionKey, systemId, version)) {
            installationStatus.setRpmInstalled(true);
            enabled = true;
          }
          if (softwareChannelAssociated(sessionKey, systemId, version)) {
            installationStatus.setSoftwareChannelAssociated(true);
            enabled = true;
          }
          if (configChannelAssociated(sessionKey, systemId)) {
            installationStatus.setConfigChannelAssociated(true);
            enabled = true;
          }
          if (configDeployed(sessionKey, systemId)) {
            installationStatus.setConfigDeployed(true);
            enabled = true;
          }
        }
      }

      SatSystem satSys = new SatSystem(
          systemId,
          (String) apiSysMap.get("name"),
          systemVersion,
          installationStatus,
          enabled,
          validSystem);
      satSystems.add(satSys);
    }

    return satSystems;
  }

  /**
   * (Un)Install insights on multiple systems
   */
  @POST
  @Path("/systems")
  @Consumes(MediaType.APPLICATION_JSON)
  public ArrayList<SatSystem> postSystems(
      @CookieParam("pxt-session-cookie") String sessionKey,
      ArrayList<SatSystem> systems) {


    for (SatSystem sys : systems) {
      if (sys.getValidType()) {
        int version = -1;
        if (sys.getVersion().equals(Constants.VERSION_RHEL6_SERVER)) {
          version = 6;
        } else if (sys.getVersion().equals(Constants.VERSION_RHEL7_SERVER)) {
          version = 7;
        }
        int packageId = getPackageId(sessionKey, version);

        if (sys.getEnabled()) { //install missing pieces
          setSystemSoftwareChannels(sessionKey, sys.getId(), true, version);

          //install the package
          if (!rpmInstalled(sessionKey, sys.getId(), version)) {;
            ArrayList<Integer> packageIds = new ArrayList<Integer>();
            packageIds.add(packageId);
            SatApi.schedulePackageInstall(sessionKey, sys.getId(), packageIds, 60000);
          }

          //subscribe system to Red Hat Insights config channel
          if (!configChannelAssociated(sessionKey, sys.getId())) {
            ArrayList<Integer> systemIds = new ArrayList<Integer>();
            systemIds.add(sys.getId());
            ArrayList<String> channelLabels = new ArrayList<String>();
            channelLabels.add(Constants.CONFIG_CHANNEL_LABEL);
            SatApi.addConfigChannelsToSystem(sessionKey, systemIds, channelLabels, true);
          }
        } else { //remove installed pieces
          if (softwareChannelAssociated(sessionKey, sys.getId(), version)) {
            setSystemSoftwareChannels(sessionKey, sys.getId(), false, version);
          }
          if (rpmInstalled(sessionKey, sys.getId(), version)) {
            ArrayList<Integer> packageIds = new ArrayList<Integer>();
            packageIds.add(packageId);
            SatApi.schedulePackageRemove(sessionKey, sys.getId(), packageIds);
          }
          if (configChannelAssociated(sessionKey, sys.getId())) {
            ArrayList<Integer> systemIds = new ArrayList<Integer>();
            systemIds.add(sys.getId());
            ArrayList<String> channelLabels = new ArrayList<String>();
            channelLabels.add(Constants.CONFIG_CHANNEL_LABEL);
            SatApi.removeConfigChannelsFromSystem(sessionKey, systemIds, channelLabels);
          }
        }
      }
    }
    SatApi.deployAllSystems(sessionKey, Constants.CONFIG_CHANNEL_LABEL);
    return systems;
  }

  @SuppressWarnings("unchecked")
  private int getPackageId(String sessionKey, int version) {
    String channelLabel = getVersionSpecificChannelLabel(version);
    //grab the redhat-access-proactive packageId from the channel
    Object[] channelPackages = 
      SatApi.listAllPackagesInChannel(sessionKey, channelLabel);
    int packageId = -1;
    if (channelPackages != null) {
      for (Object channelPackage : channelPackages) {
        HashMap<Object, Object> channelPackageMap = (HashMap<Object, Object>) channelPackage;
        String packageName = (String) channelPackageMap.get("name");
        if (packageName.equals(Constants.PACKAGE_NAME)) {
          packageId = (int) channelPackageMap.get("id");
        }
      }
    }
    return packageId;
  }

  /**
   * Check if a system has the insights channel associated
   */
  @SuppressWarnings("unchecked")
  private boolean softwareChannelAssociated(String sessionKey, int systemId, int version) {
    Object[] channels = SatApi.listSystemChannels(sessionKey, systemId);
    boolean found = false;
    if (channels != null) {
      for (Object channel : channels) {
        HashMap<Object, Object> channelMap = (HashMap<Object, Object>) channel;
        if (channelMap.get("label").equals(getVersionSpecificChannelLabel(version))) {
          found = true;
        }
      }
    }
    return found;
  }

  /**
   * Check if a system has the config channel associated
   */
  @SuppressWarnings("unchecked")
  private boolean configChannelAssociated(String sessionKey, int systemId) {
    Object[] channels = SatApi.listConfigChannels(sessionKey, systemId);
    boolean found = false;
    if (channels != null) {
      for (Object channel : channels) {
        HashMap<Object, Object> channelMap = (HashMap<Object, Object>) channel;
        if (channelMap.get("label").equals(Constants.CONFIG_CHANNEL_LABEL)) {
          found = true;
        }
      }
    }
    return found;
  }

  /**
   * Get the latest insights config file revision
   */
  @SuppressWarnings("unchecked")
  private int getLatestFileRevision(String sessionKey) {
    Object[] revisions =
      (Object[]) SatApi.getFileRevisions(
          sessionKey, Constants.CONFIG_CHANNEL_LABEL, Constants.CONFIG_PATH);
    int version = -1;
    if (revisions != null) {
      for (Object revision : revisions) {
        HashMap<Object, Object> revisionMap = (HashMap<Object, Object>) revision;
        int currentVersion = (int) revisionMap.get("revision");
        if (currentVersion > version) {
          version = currentVersion;
        }
      }
    }
    return version;
  }

  /**
   * Check if a system has the latest config file rev deployed
   */
  @SuppressWarnings("unchecked")
  private boolean configDeployed(String sessionKey, int systemId) {
    boolean response = false;
    int latestRevision = getLatestFileRevision(sessionKey);
    if (latestRevision > 0) {
      ArrayList<String> paths = new ArrayList<String>();
      paths.add(Constants.CONFIG_PATH);
      Object[] fileInfos = SatApi.lookupFileInfo(sessionKey, systemId, paths, 1);
      if (fileInfos != null) {
        for (Object fileInfo : fileInfos) {
          HashMap<Object, Object> fileInfoMap = (HashMap<Object, Object>) fileInfo;
          String channel = (String) fileInfoMap.get("channel");
          if (channel.equals(Constants.CONFIG_CHANNEL_NAME)) {
            int revision = (int) fileInfoMap.get("revision");
            if (revision == latestRevision) {
              response = true;
            }
          }
        }
      }
    }

    return response;
  }

  private String getVersionSpecificChannelLabel(int version) {
    String channelLabel = "";
    if (version == 6) {
      channelLabel = Constants.CHANNEL_LABEL_RHEL6;
    } else if (version == 7) {
      channelLabel = Constants.CHANNEL_LABEL_RHEL7;
    }
    return channelLabel;
  };


  private String getVersionSpecificRepoUrl(int version) {
    String repoUrl = "";

    if (version == 6) {
      repoUrl = Constants.REPO_URL_RHEL6;
    } else if (version == 7) {
      repoUrl = Constants.REPO_URL_RHEL7;
    }
    return repoUrl;
  }

  private String getVersionSpecificRepoLabel(int version) {
    String repoLabel = "";
    if (version == 6) {
      repoLabel = Constants.REPO_LABEL_RHEL6;
    } else if (version == 7) {
      repoLabel = Constants.REPO_LABEL_RHEL7;
    }
    return repoLabel;
  }

  /**
   * Check if a system has the RPM installed
   */
  @SuppressWarnings("unchecked")
  private boolean rpmInstalled(String sessionKey, int systemId, int version) {
    Object[] installedPackages = 
      SatApi.listInstalledPackagesFromChannel(
          sessionKey, 
          systemId, 
          getVersionSpecificChannelLabel(version));
    boolean found = false;
    if (installedPackages != null) {
      for (Object installedPackage : installedPackages) {
        HashMap<Object, Object> packageMap = (HashMap<Object, Object>) installedPackage;
        found = true;
      }
    }
    return found;
  }

  /**
   * Check if the insights software channel exists
   */
  @SuppressWarnings("unchecked")
  private boolean channelExists(String sessionKey, int version) {
    Object[] channels = SatApi.listSoftwareChannels(sessionKey);
    boolean response = false;
    String channelLabel = getVersionSpecificChannelLabel(version);
    if (channels != null) {
      for (Object channel : channels) {
        HashMap<Object, Object> channelMap = (HashMap<Object, Object>) channel;
        String label = (String) channelMap.get("label");
        if (label.equals(channelLabel)) {
          response = true;
        }
      }
    }
    return response;
  }

  /**
   * Check if the insights repo exists
   */
  @SuppressWarnings("unchecked")
  private boolean repoExists(String sessionKey, int version) {
    Object[] repos = SatApi.listUserRepos(sessionKey);
    boolean exists = false;
    if (repos != null) {
      for (Object repo : repos) {
        HashMap<Object, Object> repoMap = (HashMap<Object, Object>) repo;
        String label = (String) repoMap.get("label"); 
        if (label.equals(getVersionSpecificChannelLabel(version))) {
          exists = true;
        }
      }
    }
    return exists;
  }

  /**
   * Subscribe a system to the insights software channel
   */
  @SuppressWarnings("unchecked")
  private boolean setSystemSoftwareChannels(
      String sessionKey,
      int systemId,
      boolean addInsightsChannel,
      int version) {

    //list existing channels system is subscribed to
    Object[] systemChannels = SatApi.listSystemChannels(sessionKey, systemId);
    ArrayList<String> systemChannelLabels = new ArrayList<String>();
    String channelLabel = getVersionSpecificChannelLabel(version);
    if (systemChannels != null) {
      for (Object systemChannel : systemChannels) {
        String label = 
          (String)((HashMap<Object, Object>) systemChannel).get("label");
        //insights channel already associated
        if (label.equals(channelLabel)) {
          if (addInsightsChannel) {
            return true;
          }
        } else {
          systemChannelLabels.add(label);
        }
      }
    }
    if (addInsightsChannel) {
      systemChannelLabels.add(channelLabel);
    }

    //subscribe system to Red Hat Insights child channel
    int response = 
      SatApi.setChildChannels(sessionKey, systemId, systemChannelLabels);
    if (response != 1) {
      return false;
    } else {
      return true;
    }
  }

  /**
   * Create the repo via sat5 api if it doesn't already exist
   * Returns the new or existing repo id
   */
  private void createRepo(String sessionKey, int version) {
    if (!repoExists(sessionKey, version)) {
      String repoLabel = getVersionSpecificRepoLabel(version);
      String repoUrl = getVersionSpecificRepoUrl(version);
      SatApi.createRepo(
          sessionKey, 
          repoLabel, 
          "YUM", 
          repoUrl);
    } 
  }

  /**
   * Create the insights custom software channel,
   * assocate to the insights repo,
   * syncronize the repo.
   */
  private boolean createChannel(String sessionKey, int version) {
    boolean response = false;
    String channelLabel = "";
    String name = "";
    String summary = "";
    String archLabel = "";
    String parent = "";
    String repoLabel = "";
    if (version == 6) {
      channelLabel = Constants.CHANNEL_LABEL_RHEL6;
      name = Constants.CHANNEL_NAME_RHEL6;
      summary = Constants.CHANNEL_SUMMARY_RHEL6;
      archLabel = Constants.CHANNEL_ARCH_RHEL6;
      parent = Constants.CHANNEL_PARENT_RHEL6;
      repoLabel = Constants.REPO_LABEL_RHEL6;
    } else if (version == 7) {
      channelLabel = Constants.CHANNEL_LABEL_RHEL7;
      name = Constants.CHANNEL_NAME_RHEL7;
      summary = Constants.CHANNEL_SUMMARY_RHEL7;
      archLabel = Constants.CHANNEL_ARCH_RHEL7;
      parent = Constants.CHANNEL_PARENT_RHEL7;
      repoLabel = Constants.REPO_LABEL_RHEL7;
    }

    if (!channelExists(sessionKey, version)) {
      int created = SatApi.createChannel(
          sessionKey, 
          channelLabel,
          name,
          summary,
          archLabel,
          parent);
      if (created == 0) {
        response = false;
      } else {
        response = true;
        //associate repo with this channel
        SatApi.associateRepo(sessionKey, channelLabel, repoLabel);
        SatApi.syncRepo(sessionKey, channelLabel);
      }
    } else {
      //channel already created
      //TODO: verify the repo is associated?
      response = true;
    }
    return response;
  }

  /**
   * Create the insights config channel and add a default file
   */
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
          "auto_config=true");
          //"# Change log level, valid options DEBUG, INFO, WARNING, ERROR, CRITICAL. Default DEBUG" +
          //System.getProperty("line.separator") +
          //"#loglevel=DEBUG" + 
          //System.getProperty("line.separator") +
          //"# Change authentication method, valid options BASIC, CERT. Default BASIC" +
          //System.getProperty("line.separator") +
          //"authmethod=BASIC" + 
          //System.getProperty("line.separator") +
          //"# URL to send uploads to" + 
          //System.getProperty("line.separator") +
          //"upload_url=https://sat57.usersys.redhat.com/redhataccess/rs/telemetry" + 
          //System.getProperty("line.separator") +
          //"# URL to send API requests to" + 
          //System.getProperty("line.separator") +
          //"api_url=https://sat57.usersys.redhat.com/redhataccess/rs/telemetry/api" + 
          //System.getProperty("line.separator") +
          //"username=" +
          //System.getProperty("line.separator") +
          //"password=");
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

  /**
   * Check if a user is the satellite administrator
   */
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
