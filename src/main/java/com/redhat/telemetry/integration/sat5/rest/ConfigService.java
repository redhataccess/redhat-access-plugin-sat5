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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.redhat.telemetry.integration.sat5.json.Config;
import com.redhat.telemetry.integration.sat5.json.Connection;
import com.redhat.telemetry.integration.sat5.json.PortalResponse;
import com.redhat.telemetry.integration.sat5.json.Status;
import com.redhat.telemetry.integration.sat5.portal.InsightsApiClient;
import com.redhat.telemetry.integration.sat5.satellite.json.ApiSystem;
import com.redhat.telemetry.integration.sat5.satellite.SatApi;
import com.redhat.telemetry.integration.sat5.satellite.ScheduleCache;
import com.redhat.telemetry.integration.sat5.satellite.SatelliteSystem;
import com.redhat.telemetry.integration.sat5.util.Constants;
import com.redhat.telemetry.integration.sat5.util.PropertiesHandler;
import com.redhat.telemetry.integration.sat5.util.Util;


@Path("/config")
public class ConfigService {
  @Context ServletContext context;
  private Logger LOG = LoggerFactory.getLogger(ConfigService.class);

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
          PropertiesHandler.getEnabled(), 
          PropertiesHandler.getDebug()); 
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
      String portalUrl = PropertiesHandler.getPortalUrl();
      if (portalUrl != null && portalUrl != "") {
        PropertiesHandler.setProperty(Constants.PORTALURL_PROPERTY, portalUrl);
      }
      PropertiesHandler.setProperty(Constants.ENABLED_PROPERTY, Boolean.toString(config.getEnabled()));
      PropertiesHandler.setProperty(Constants.DEBUG_PROPERTY, Boolean.toString(config.getDebug()));
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
    for (Object apiSys : apiSystems) {
      HashMap<Object, Object> apiSysMap = (HashMap<Object, Object>) apiSys;
      ApiSystem sys = new ApiSystem(
          (Integer) apiSysMap.get("id"),
          (String) apiSysMap.get("name"),
          "");
      systems.add(sys);
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

    Object systemName = SatApi.getSystemName(sessionKey, Integer.parseInt(id));
    HashMap<Object, Object> apiSysMap = (HashMap<Object, Object>) systemName;

    Object systemDetails = SatApi.getSystemDetails(sessionKey, Integer.parseInt(id));
    HashMap<Object, Object> systemDetailsMap = (HashMap<Object, Object>) systemDetails;

    String type = "";   
    if (systemDetailsMap.get("virtualization") != null) {
      type = Constants.SYSTEM_TYPE_GUEST; //TODO: find out which values mean guest vs host
    } else {
      type = Constants.SYSTEM_TYPE_PHYSICAL;
    }

    ApiSystem sys = new ApiSystem((Integer) apiSysMap.get("id"), (String) apiSysMap.get("name"), type);
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

    //TODO: get map of redhat-access-insights packageId <=> arch

    try {
      for (Status sys : systems) {
        SatelliteSystem system = new SatelliteSystem(sessionKey, sys.getId());

        int installedPackageId = system.isPackageInstalled();
        if (sys.getEnabled() && installedPackageId == -1) {
          int packageId = system.getAvailablePackageId();
          LOG.debug("Installing redhat-access-insights on system... SystemID: " + 
              sys.getId() + " | PackageId: " + packageId);
          //install the package
          ArrayList<Integer> packageIds = new ArrayList<Integer>();
          packageIds.add(packageId);
          int actionId = 
            SatApi.schedulePackageInstall(sessionKey, sys.getId(), packageIds, 60000);
          ScheduleCache.getInstance().addSchedule(sys.getId(), actionId, Constants.INSTALL_SCHEDULED);
        } else if (!sys.getEnabled() && installedPackageId > -1) { //remove installed pieces
          LOG.debug("Uninstalling redhat-access-insights from system... SystemID: " + 
              sys.getId() + " | PackageId: " + installedPackageId);
          system.unregister();
          ArrayList<Integer> packageIds = new ArrayList<Integer>();
          packageIds.add(installedPackageId);
          int actionId = 
            SatApi.schedulePackageRemove(sessionKey, sys.getId(), packageIds);
          ScheduleCache.getInstance().addSchedule(sys.getId(), actionId, Constants.UNINSTALL_SCHEDULED);
        }
      }
    } catch (Exception e) {
      LOG.error("Exception in POST /config/systems", e);
      throw new WebApplicationException(
          new Throwable(Constants.INTERNAL_SERVER_ERROR_MESSAGE), 
          Response.Status.INTERNAL_SERVER_ERROR);
    }
  }

  @GET
  @Path("/status")
  @Produces(MediaType.APPLICATION_JSON)
  public ArrayList<Status> getMultipleSystemStatus(
      @CookieParam("pxt-session-cookie") String sessionKey,
      @QueryParam("systems") String systemIds) {

    try {
      ArrayList<Status> statusList = new ArrayList<Status>();
      List<String> systemIdsList = Arrays.asList(systemIds.split("\\s*,\\s*"));
      try {
        for (String id : systemIdsList) {
          SatelliteSystem system = new SatelliteSystem(sessionKey, Integer.parseInt(id));
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
  @Path("/status/{id}")
  @Produces(MediaType.APPLICATION_JSON)
  public Status getSingleSystemStatus(
      @CookieParam("pxt-session-cookie") String sessionKey,
      @PathParam("id") String id) {

    Status status = null;
    try {
      SatelliteSystem system = new SatelliteSystem(sessionKey, Integer.parseInt(id));
      status = system.getStatus();
    } catch (Exception e) {
      LOG.error("Exception in GET /status/{id}", e);
      throw new WebApplicationException(
          new Throwable(Constants.INTERNAL_SERVER_ERROR_MESSAGE), 
          Response.Status.INTERNAL_SERVER_ERROR);
    }
    return status;
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
      debugIsOn = PropertiesHandler.getDebug();
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
