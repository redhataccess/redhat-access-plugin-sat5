package com.redhat.telemetry.integration.sat5.rest;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.servlet.ServletContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.CookieParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.configuration.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.redhat.telemetry.integration.sat5.json.Config;
import com.redhat.telemetry.integration.sat5.json.Connection;
import com.redhat.telemetry.integration.sat5.json.PortalResponse;
import com.redhat.telemetry.integration.sat5.json.Status;
import com.redhat.telemetry.integration.sat5.portal.InsightsApiClient;
import com.redhat.telemetry.integration.sat5.satellite.json.ApiSystem;
import com.redhat.telemetry.integration.sat5.satellite.SatApi;
import com.redhat.telemetry.integration.sat5.satellite.SatelliteSystem;
import com.redhat.telemetry.integration.sat5.util.Constants;
import com.redhat.telemetry.integration.sat5.util.PropertiesHandler;
import com.redhat.telemetry.integration.sat5.util.ScheduleHandler;
import com.redhat.telemetry.integration.sat5.util.Util;


@Path("/config")
public class ConfigService {
  @Context ServletContext context;
  private Logger LOG = LoggerFactory.getLogger(ConfigService.class);
  private PropertiesHandler propertiesHandler = new PropertiesHandler();

  /**
   * Retrieve general config values
   */
  @GET
  @Path("/general")
  @Produces(MediaType.APPLICATION_JSON)
  public Config getConfig(
      @CookieParam("pxt-session-cookie") String sessionKey,
      @QueryParam("satellite_user") String satelliteUser) {

    Config config = null;

    try {
      config = new Config(
          propertiesHandler.getEnabled(), 
          propertiesHandler.getDebug()); 
    } catch (Exception e) {
      LOG.error("Exception in GET /config/general", e);
      throw new WebApplicationException(
          new Throwable(Constants.INTERNAL_SERVER_ERROR_MESSAGE), 
          Response.Status.INTERNAL_SERVER_ERROR);
    }
    return config;
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
      @QueryParam("satellite_user") String satelliteUser) {

    if (!Util.userIsAdmin(sessionKey, satelliteUser)) {
      throw new WebApplicationException(
          new Throwable("Must be satellite admin."), 
          Response.Status.UNAUTHORIZED);
    }
    Util.setLogLevel(config.getDebug());

    try {
      String portalUrl = propertiesHandler.getPortalUrl();
      if (portalUrl != null && portalUrl != "") {
        propertiesHandler.setProperty(Constants.PORTALURL_PROPERTY, portalUrl);
      }
      propertiesHandler.setProperty(Constants.ENABLED_PROPERTY, Boolean.toString(config.getEnabled()));
      propertiesHandler.setProperty(Constants.DEBUG_PROPERTY, Boolean.toString(config.getDebug()));
    } catch (Exception e) {
      LOG.error("Exception in POST /config/general", e);
      throw new WebApplicationException(
          new Throwable(Constants.INTERNAL_SERVER_ERROR_MESSAGE), 
          Response.Status.INTERNAL_SERVER_ERROR);
    }

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
  public ArrayList<ApiSystem> getSystems(
      @CookieParam("pxt-session-cookie") String sessionKey) {

    Object[] apiSystems = SatApi.listSystems(sessionKey);
    ArrayList<ApiSystem> systems = new ArrayList<ApiSystem>();
    if (apiSystems != null) {
      for (Object apiSys : apiSystems) {
        HashMap<Object, Object> apiSysMap = (HashMap<Object, Object>) apiSys;
        ApiSystem sys = new ApiSystem(
            (Integer) apiSysMap.get("id"),
            (String) apiSysMap.get("name"),
            "");
        systems.add(sys);
      }
    }

    return systems;
  }

  @SuppressWarnings("unchecked")
  @GET
  @Path("/systems/{id}")
  @Produces(MediaType.APPLICATION_JSON)
  public ApiSystem getSystem(
      @PathParam("id") String id,
      @CookieParam("pxt-session-cookie") String sessionKey) {
    ApiSystem sys = new ApiSystem(-1, "", "");

    Object systemName = SatApi.getSystemName(sessionKey, Integer.parseInt(id));
    Object systemDetails = SatApi.getSystemDetails(sessionKey, Integer.parseInt(id));

    if (systemDetails != null && systemName != null) {
      HashMap<Object, Object> systemDetailsMap = (HashMap<Object, Object>) systemDetails;
      HashMap<Object, Object> apiSysMap = (HashMap<Object, Object>) systemName;

      String type = "";   
      if (systemDetailsMap.get("virtualization") != null) {
        type = Constants.SYSTEM_TYPE_GUEST; //TODO: find out which values mean guest vs host
      } else {
        type = Constants.SYSTEM_TYPE_PHYSICAL;
      }

      sys = new ApiSystem((Integer) apiSysMap.get("id"), (String) apiSysMap.get("name"), type);
    }
    return sys;
  }

  /**
   * (Un)Install insights on multiple systems
   */
  @POST
  @Path("/systems")
  @Consumes(MediaType.APPLICATION_JSON)
  public void postSystems(
      @CookieParam("pxt-session-cookie") String sessionKey,
      ArrayList<Status> systems) {

    try {

      Object[] rhaiPackages = 
        SatApi.searchPackageByName(sessionKey, propertiesHandler.getRPMName());
      HashMap<Integer, Integer> systemToPackageMap = 
        buildListOfSystemsWithRPMInstalled(sessionKey, rhaiPackages);
      for (Status sys : systems) {
        Integer packageId = systemToPackageMap.get(sys.getId());
        SatelliteSystem system = new SatelliteSystem(sessionKey, sys.getId(), packageId);

        boolean packageIsInstalled = system.isPackageInstalled();
        Integer installedPackageId = system.getPackageId();
        ScheduleHandler scheduleHandler = new ScheduleHandler();
        if (sys.getEnabled() && !packageIsInstalled) {
          HashMap<String, Integer> channelLabels = buildListOfChannelsWithRPM(sessionKey, rhaiPackages);
          system.findAvailablePackageId(channelLabels);
          Integer availablePackageId = system.getAvailablePackageId();
          LOG.debug("Installing redhat-access-insights on system... SystemID: " + 
              sys.getId() + " | PackageId: " + availablePackageId);
          //install the package
          ArrayList<Integer> packageIds = new ArrayList<Integer>();
          packageIds.add(availablePackageId);
          Integer actionId = 
            SatApi.schedulePackageInstall(sessionKey, sys.getId(), packageIds, 60000);
          LOG.debug("Install action id for system (" + sys.getId() + "): " + actionId);
          scheduleHandler.add(sys.getId(), actionId, Constants.INSTALL_SCHEDULED);
        } else if (!sys.getEnabled() && packageIsInstalled) { //remove installed pieces
          LOG.debug("Uninstalling redhat-access-insights from system... SystemID: " + 
              sys.getId() + " | PackageId: " + installedPackageId);
          try {
              system.unregister();
          } catch (NotFoundException e){
                //This is a valid response if the insights rpm was manually installed
                //but the client system was not registered to Insights service
                LOG.debug("System not found in Portal inventory, assuming it was never registered...");
          }
          ArrayList<Integer> packageIds = new ArrayList<Integer>();
          packageIds.add(installedPackageId);
          Integer actionId = 
            SatApi.schedulePackageRemove(sessionKey, sys.getId(), packageIds);
          LOG.debug("Uninstall action id for system (" + sys.getId() + "): " + actionId);
          scheduleHandler.add(sys.getId(), actionId, Constants.UNINSTALL_SCHEDULED);
        }
      }
    } catch (Exception e) {
      LOG.error("Exception in POST /config/systems", e);
      throw new WebApplicationException(
          new Throwable(Constants.INTERNAL_SERVER_ERROR_MESSAGE), 
          Response.Status.INTERNAL_SERVER_ERROR);
    }
  }

  @SuppressWarnings("unchecked")
  private HashMap<String, Integer> buildListOfChannelsWithRPM(
      String sessionKey, Object[] rhaiPackages) throws ConfigurationException {

    HashMap<String, Integer> channelLabels = new HashMap<String, Integer>();
    if (rhaiPackages != null) {
      for (Object rhaiPackage : rhaiPackages) {
        HashMap<Object, Object> rhaiPackageMap = (HashMap<Object, Object>) rhaiPackage;
        Integer packageId = (Integer) rhaiPackageMap.get("id");
        if (packageId != null) {
          Object[] channels = SatApi.listProvidingChannels(sessionKey, packageId);
          if (channels != null) {
            for (Object channel : channels) {
              HashMap<Object, Object> channelsMap = (HashMap<Object, Object>) channel;
              String label = (String) channelsMap.get("label");
              channelLabels.put(label, packageId);
            }
          }
        }
      }
    }
    return channelLabels;
  }

  @SuppressWarnings("unchecked")
  private HashMap<Integer, Integer> buildListOfSystemsWithRPMInstalled(
      String sessionKey,
      Object[] rhaiPackages) throws ConfigurationException {

    HashMap<Integer, Integer> systemToPackageMap = new HashMap<Integer, Integer>();
    if (rhaiPackages != null) {
      for (Object rhaiPackage : rhaiPackages) {
        HashMap<Object, Object> rhaiPackageMap = (HashMap<Object, Object>) rhaiPackage;
        int packageId = (Integer) rhaiPackageMap.get("id");
        Object[] systemsWithPackage = SatApi.listSystemsWithPackage(sessionKey, packageId);
        if (systemsWithPackage != null) {
          for (Object systemWithPackage : systemsWithPackage) {
            HashMap<Object, Object> systemWithPackageMap = (HashMap<Object, Object>) systemWithPackage;
            int systemId = (Integer) systemWithPackageMap.get("id");
            systemToPackageMap.put(systemId, packageId);
          }
        }
      }
    }
    return systemToPackageMap;
  }

  @GET
  @Path("/status")
  @Produces(MediaType.APPLICATION_JSON)
  public ArrayList<Status> getMultipleSystemStatus(
      @CookieParam("pxt-session-cookie") String sessionKey,
      @QueryParam("systems") String systemIds) {

    try {
      Object[] rhaiPackages = 
        SatApi.searchPackageByName(sessionKey, propertiesHandler.getRPMName());
      HashMap<Integer, Integer> systemToPackageMap = 
        buildListOfSystemsWithRPMInstalled(sessionKey, rhaiPackages);
      HashMap<String, Integer> channelLabels = buildListOfChannelsWithRPM(sessionKey, rhaiPackages);

      ArrayList<Status> statusList = new ArrayList<Status>();
      List<String> systemIdsList = Arrays.asList(systemIds.split("\\s*,\\s*"));
      try {
        for (String id : systemIdsList) {
          Integer packageId = systemToPackageMap.get(Integer.parseInt(id));
          SatelliteSystem system = new SatelliteSystem(sessionKey, Integer.parseInt(id), packageId);
          system.findAvailablePackageId(channelLabels);
          Status status = system.getStatus();
          statusList.add(status);
        }
      } catch (Exception e) {
        LOG.error("Exception in GET /config/status", e);
        throw new WebApplicationException(
            new Throwable(Constants.INTERNAL_SERVER_ERROR_MESSAGE), 
            Response.Status.INTERNAL_SERVER_ERROR);
      }
      return statusList;
    } catch (Exception e) {
      LOG.error("Exception in GET /status", e);
      throw new WebApplicationException(
          new Throwable(Constants.INTERNAL_SERVER_ERROR_MESSAGE), 
          Response.Status.INTERNAL_SERVER_ERROR);
    }
  }

  @GET
  @Path("/connection")
  @Produces(MediaType.APPLICATION_JSON)
  public Connection testConnection(
      @CookieParam("pxt-session-cookie") String sessionKey,
      @QueryParam("satellite_user") String satelliteUser) {
    
    if (!Util.userIsAdmin(sessionKey, satelliteUser)) {
      throw new WebApplicationException(
          new Throwable("Must be satellite admin."), 
          Response.Status.UNAUTHORIZED);
    }
    boolean debugIsOn = false;

    try {
      debugIsOn = propertiesHandler.getDebug();
    } catch (Exception e) {
      LOG.error("Unable to load debugProperty in GET /config/connection", e);
      throw new WebApplicationException(
          new Throwable(Constants.INTERNAL_SERVER_ERROR_MESSAGE), 
          Response.Status.INTERNAL_SERVER_ERROR);
    }

    if (!debugIsOn) {
      Util.enableDebugMode();
    }
    Calendar cal = Calendar.getInstance();
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss,SSS");
    String currentTimestamp = dateFormat.format(cal.getTime());

    try {
      LOG.debug("Verifying connection to customer portal.");
      InsightsApiClient client = new InsightsApiClient();
      PortalResponse response = client.makeRequest(
        Constants.METHOD_GET, 
        "/me",
        null,
        null,
        MediaType.APPLICATION_JSON);
      if (!debugIsOn) {
        Util.disableDebugMode();
      }
      if (response.getStatusCode() == 200) {
        return new Connection(true, response.getStatusCode(), "", "", currentTimestamp);
      } else {
        return new Connection(false, response.getStatusCode(), "", "", currentTimestamp);
      }
    } catch (Exception e) {
      String message = "Unable to verify connection to Red Hat Customer Portal.";
      LOG.error(message, e);
      return new Connection(false, 500, "", "", currentTimestamp);
    }
  } 

  @GET
  @Path("/log")
  @Produces(MediaType.TEXT_PLAIN)
  public String getLog(
      @CookieParam("pxt-session-cookie") String sessionKey,
      @QueryParam("timestamp") String timestamp,
      @QueryParam("satellite_user") String satelliteUser) {

    if (!Util.userIsAdmin(sessionKey, satelliteUser)) {
      throw new WebApplicationException(
          new Throwable("Must be satellite admin."), 
          Response.Status.UNAUTHORIZED);
    }
    DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss,SSS");
    Date currentDate = null;
    String logString = "";
    try {
      currentDate = format.parse(timestamp);
      logString = Util.getLog(currentDate);
    } catch (Exception e) {
      LOG.error("Exception in GET /config/log", e);
      throw new WebApplicationException(
          new Throwable(Constants.INTERNAL_SERVER_ERROR_MESSAGE), 
          Response.Status.INTERNAL_SERVER_ERROR);
    }

    return logString;
  }
}
