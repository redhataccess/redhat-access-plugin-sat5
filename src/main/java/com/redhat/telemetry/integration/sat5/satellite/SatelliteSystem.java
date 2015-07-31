package com.redhat.telemetry.integration.sat5.satellite;

import java.util.HashMap;

import org.apache.commons.configuration.ConfigurationException;

import com.redhat.telemetry.integration.sat5.json.Status;
import com.redhat.telemetry.integration.sat5.json.SystemInstallStatus;
import com.redhat.telemetry.integration.sat5.util.Constants;
import com.redhat.telemetry.integration.sat5.util.ScheduleHandler;

//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

public class SatelliteSystem {
  //private Logger LOG = LoggerFactory.getLogger(SatelliteSystem.class);

  private String sessionKey;
  private Integer systemId;
  private Integer packageId = -1;
  private Integer availablePackageId = -1;

  public SatelliteSystem(String sessionKey, Integer systemId, Integer packageId) {
    this.sessionKey = sessionKey;
    this.systemId = systemId;
    this.packageId = packageId;
  }

  public Integer getPackageId() {
    return this.packageId;
  }

  public Integer getAvailablePackageId() {
    return this.availablePackageId;
  }

  @SuppressWarnings("unchecked")
  public void findAvailablePackageId(
      HashMap<String, Integer> channelLabels) throws ConfigurationException {
    Object[] channels = 
      SatApi.listSystemChannels(this.sessionKey, this.systemId);
    if (channels != null) {
      for (Object channel : channels) {
        HashMap<Object, Object> channelMap = (HashMap<Object, Object>) channel;
        String label = (String) channelMap.get("label");
        Integer availablePackageId = channelLabels.get(label);
        if (availablePackageId != null) {
          this.availablePackageId = availablePackageId;
          break;
        }
      }
    }
  }

  public boolean isPackageInstalled() {
    boolean response = false;
    if (this.packageId != null && this.packageId != -1) {
      response = true;
    }
    return response;
  }

  public boolean isPackageAvailable()  {
    boolean response = false;
    if (this.availablePackageId != null && this.availablePackageId != -1) {
      response = true;
    }
    return response;
  }

  public Status getStatus() throws ConfigurationException {
    SystemInstallStatus installStatus = new SystemInstallStatus();
    boolean enabled = false;

    String scheduledStatus = this.rpmScheduled();
    if (this.rpmScheduled() != null) {
      installStatus.setRpmScheduled(scheduledStatus);
    } else {
      installStatus.setRpmScheduled(Constants.NOT_SCHEDULED);
    }

    if (installStatus.getRpmScheduled().equals(Constants.NOT_SCHEDULED)) {
      boolean packageIsInstalled = this.isPackageInstalled();
      if (packageIsInstalled) {
        installStatus.setRpmInstalled(true);
        enabled = true;
      } else {
        installStatus.setRpmInstalled(false);
      }
    }

    if (!installStatus.getRpmInstalled() && installStatus.getRpmScheduled().equals(Constants.NOT_SCHEDULED)) {
      boolean packageIsAvailable = this.isPackageAvailable();
      if (!packageIsAvailable) {
        installStatus.setRpmAvailable(false);
      } else {
        installStatus.setRpmAvailable(true);
      }
    }

    Status status = new Status(
        this.systemId,
        installStatus,
        enabled);
    return status;
  }

  @SuppressWarnings("unchecked")
  private String rpmScheduled() throws ConfigurationException {
    String type = null;
    ScheduleHandler scheduleHandler = new ScheduleHandler();
    Integer actionId = scheduleHandler.getAction(this.systemId);
    if (actionId != -1) {
      Object[] actions = SatApi.listInProgressSystems(this.sessionKey, actionId);
      if (actions != null) {
        for (Object action : actions) {
          HashMap<Object, Object> actionMap = (HashMap<Object, Object>) action;
          Integer serverId = (Integer) actionMap.get("server_id");
          if (serverId.equals(this.systemId)) {
            type = scheduleHandler.getType(this.systemId);
          }
        }
      }
      //assume a stale cache entry, clear it out
      if (type == null) {
        scheduleHandler.remove(this.systemId);
      }
    }
    return type;
  }

  public void unregister() {

  }
}
