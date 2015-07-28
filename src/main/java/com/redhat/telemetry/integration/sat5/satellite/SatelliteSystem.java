package com.redhat.telemetry.integration.sat5.satellite;

import java.util.HashMap;

import org.apache.commons.configuration.ConfigurationException;

import com.redhat.telemetry.integration.sat5.json.Status;
import com.redhat.telemetry.integration.sat5.json.SystemInstallStatus;
import com.redhat.telemetry.integration.sat5.util.Constants;
import com.redhat.telemetry.integration.sat5.util.PropertiesHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SatelliteSystem {
  private Logger LOG = LoggerFactory.getLogger(SatelliteSystem.class);

  private String sessionKey;
  private int systemId;
  private int packageId = -1;

  public SatelliteSystem(String sessionKey, int systemId) {
    this.sessionKey = sessionKey;
    this.systemId = systemId;
  }


  public int getInstalledPackageId() {
    int packageId = -1;

    return packageId;
  }

  @SuppressWarnings("unchecked")
  public int getAvailablePackageId() {
    Object[] installablePackages = 
      SatApi.listAllInstallablePackages(sessionKey, systemId);
    Object[] insightsPackages = 
      SatApi.searchPackageByName(sessionKey, Constants.INSIGHTS_CLIENT_RPM_NAME);

    int packageId = -1;
    for (Object installablePackage : installablePackages) {
      HashMap<Object, Object> installablePackageMap = 
        (HashMap<Object, Object>) installablePackage;
      for (Object insightsPackage : insightsPackages) {
        HashMap<Object, Object> insightsPackageMap = 
          (HashMap<Object, Object>) insightsPackage;
        int installablePackageId = (Integer) installablePackageMap.get("id");
        int insightsPackageId = (Integer) insightsPackageMap.get("id");
        if (insightsPackageId == installablePackageId) {
          packageId = insightsPackageId;
        }
      }
    }

    LOG.debug("Package ID for System (" + systemId + "): " + packageId);

    return packageId;
  }

  @SuppressWarnings("unchecked")
  public int isPackageInstalled() throws ConfigurationException {
    Object[] installedPackages = 
      SatApi.listInstalledPackages(this.sessionKey, this.systemId);
    HashMap<Object, Object> insightsPackage = null;
    if (installedPackages != null) {
      for (Object installedPackage : installedPackages) {
        HashMap<Object, Object> installedPackageMap = 
          (HashMap<Object, Object>) installedPackage;
        String installedPackageName = (String) installedPackageMap.get("name");
        if (installedPackageName.equals(PropertiesHandler.getRPMName())) {
          insightsPackage = installedPackageMap;
        }
      }
    }

    int insightsPackageId = -1;
    if (insightsPackage != null) {
      String name = (String) insightsPackage.get("name");
      String version = (String) insightsPackage.get("version");
      String release = (String) insightsPackage.get("release");
      String epoch = (String) insightsPackage.get("epoch");
      String arch = (String) insightsPackage.get("arch");
      Object[] insightsPackages = 
        SatApi.findPackageByNVREA(this.sessionKey, name, version, release, epoch, arch);
      //TODO: could there be multiple packages found?
      HashMap<Object, Object> insightsPackagesMap = (HashMap<Object, Object>) insightsPackages[0]; 
      insightsPackageId = (Integer) insightsPackagesMap.get("id");
    }
    return insightsPackageId;
  }

  public Status getStatus() throws ConfigurationException {
    SystemInstallStatus installStatus = new SystemInstallStatus();
    boolean enabled = false;

    if (this.isPackageInstalled() != -1) {
      installStatus.setRpmInstalled(true);
      enabled = true;
    } else {
      installStatus.setRpmInstalled(false);
    }

    String scheduledStatus = this.rpmScheduled();
    if (this.rpmScheduled() != null) {
      installStatus.setRpmScheduled(scheduledStatus);
    } else {
      installStatus.setRpmScheduled(Constants.NOT_SCHEDULED);
    }

    if (!installStatus.getRpmInstalled()) {
      this.packageId = this.getAvailablePackageId();
      if (packageId == -1) {
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
  private String rpmScheduled() {
    String scheduled = null;
    Schedule schedule = ScheduleCache.getInstance().getSystemSchedule(this.systemId);
    if (schedule != null) {
      Object[] actions = SatApi.listInProgressSystems(this.sessionKey, schedule.getId());
      if (actions != null) {
        for (Object action : actions) {
          HashMap<Object, Object> actionMap = (HashMap<Object, Object>) action;
          Integer serverId = (Integer) actionMap.get("server_id");
          if (serverId == this.systemId) {
            scheduled = schedule.getType();
          }
        }
      }
      //assume a stale cache entry, clear it out
      if (scheduled == null) {
        ScheduleCache.getInstance().remove(this.systemId);
      }
    }
    return scheduled;
  }

  public void unregister() {

  }
}
