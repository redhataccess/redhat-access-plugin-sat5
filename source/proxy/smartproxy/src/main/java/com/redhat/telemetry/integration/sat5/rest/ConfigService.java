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
import com.redhat.telemetry.integration.sat5.satellite.AbstractSystem;
import com.redhat.telemetry.integration.sat5.satellite.SatApi;
import com.redhat.telemetry.integration.sat5.satellite.Server6System;
import com.redhat.telemetry.integration.sat5.satellite.Server7System;
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
      Server6System server6System = new Server6System(sessionKey);
      server6System.createRepo();
      server6System.createChannel();
      Server7System server7System = new Server7System(sessionKey);
      server7System.createRepo();
      server7System.createChannel();
      createConfigChannel(sessionKey);
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
      AbstractSystem system = null;
      if (systemVersion.equals(Constants.VERSION_RHEL6_SERVER)) {
        system = new Server6System(sessionKey, systemId);
      } else if (systemVersion.equals(Constants.VERSION_RHEL7_SERVER)) {
        system = new Server7System(sessionKey, systemId);
      }
      if (system != null) {
        validSystem = true;
        if(system.channelExists()) {
          if (system.rpmInstalled()) {
            installationStatus.setRpmInstalled(true);
            enabled = true;
          }
          if (system.softwareChannelAssociated()) {
            installationStatus.setSoftwareChannelAssociated(true);
            enabled = true;
          }
          if (system.configChannelAssociated()) {
            installationStatus.setConfigChannelAssociated(true);
            enabled = true;
          }
          if (system.configDeployed()) {
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
        AbstractSystem system = null;
        if (sys.getVersion().equals(Constants.VERSION_RHEL6_SERVER)) {
          system = new Server6System(sessionKey, sys.getId());
        } else if (sys.getVersion().equals(Constants.VERSION_RHEL7_SERVER)) {
          system = new Server7System(sessionKey, sys.getId());
        }
        int packageId = system.getPackageId();

        if (sys.getEnabled()) { //install missing pieces
          system.updateSoftwareChannels(true);

          //install the package
          if (!system.rpmInstalled()) {;
            ArrayList<Integer> packageIds = new ArrayList<Integer>();
            packageIds.add(packageId);
            SatApi.schedulePackageInstall(sessionKey, sys.getId(), packageIds, 60000);
          }

          //subscribe system to Red Hat Insights config channel
          if (!system.configChannelAssociated()) {
            ArrayList<Integer> systemIds = new ArrayList<Integer>();
            systemIds.add(sys.getId());
            ArrayList<String> channelLabels = new ArrayList<String>();
            channelLabels.add(Constants.CONFIG_CHANNEL_LABEL);
            SatApi.addConfigChannelsToSystem(sessionKey, systemIds, channelLabels, true);
          }
        } else { //remove installed pieces
          if (system.softwareChannelAssociated()) {
            system.updateSoftwareChannels(false);
          }
          if (system.rpmInstalled()) {
            ArrayList<Integer> packageIds = new ArrayList<Integer>();
            packageIds.add(packageId);
            SatApi.schedulePackageRemove(sessionKey, sys.getId(), packageIds);
          }
          if (system.configChannelAssociated()) {
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
