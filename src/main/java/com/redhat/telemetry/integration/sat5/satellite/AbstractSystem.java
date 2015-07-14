package com.redhat.telemetry.integration.sat5.satellite;

import java.util.HashMap;

import org.apache.commons.configuration.ConfigurationException;

import com.redhat.telemetry.integration.sat5.util.PropertiesHandler;

public abstract class AbstractSystem {

  protected String sessionKey;
  protected int systemId;
  public abstract String getChannelLabel() throws ConfigurationException;

  /**
   * Check if a system has the RPM installed
   */
  @SuppressWarnings("unchecked")
  public boolean rpmInstalled() throws ConfigurationException {
    Object[] installedPackages = 
      SatApi.listInstalledPackagesFromChannel(
          this.sessionKey, 
          this.systemId, 
          getChannelLabel());
    boolean found = false;
    if (installedPackages != null) {
      for (Object installedPackage : installedPackages) {
        HashMap<Object, Object> packageMap = (HashMap<Object, Object>) installedPackage;
        String packageName = (String) packageMap.get("name");
        if (packageName.equals(PropertiesHandler.getRPMName())) {
          found = true;
        }
      }
    }
    return found;
  }

  @SuppressWarnings("unchecked")
  public boolean rpmScheduled() {
    boolean scheduled = false;
    Integer actionId = ScheduleCache.getInstance().getSystemSchedule(this.systemId);
    if (actionId != null) {
      Object[] actions = SatApi.listInProgressSystems(this.sessionKey, actionId);
      if (actions != null) {
        for (Object action : actions) {
          HashMap<Object, Object> actionMap = (HashMap<Object, Object>) action;
          Integer serverId = (Integer) actionMap.get("server_id");
          if (serverId == this.systemId) {
            scheduled = true;
          }
        }
      }
      //assume a stale cache entry, clear it out
      if (!scheduled) {
        ScheduleCache.getInstance().remove(this.systemId);
      }
    }
    return scheduled;
  }

  @SuppressWarnings("unchecked")
  public int getPackageId() throws ConfigurationException {
    String channelLabel = getChannelLabel();
    //grab the redhat-access-proactive packageId from the channel
    Object[] channelPackages = 
      SatApi.listAllPackagesInChannel(this.sessionKey, channelLabel);
    int packageId = -1;
    if (channelPackages != null) {
      for (Object channelPackage : channelPackages) {
        HashMap<Object, Object> channelPackageMap = (HashMap<Object, Object>) channelPackage;
        String packageName = (String) channelPackageMap.get("name");
        if (packageName.equals(PropertiesHandler.getRPMName())) {
          packageId = (Integer) channelPackageMap.get("id");
        }
      }
    }
    return packageId;
  }

  /**
   * Check if a system has the insights channel associated
   */
  @SuppressWarnings("unchecked")
  public boolean softwareChannelAssociated() throws ConfigurationException {
    Object[] channels = SatApi.listSystemChannels(this.sessionKey, this.systemId);
    boolean found = false;
    if (channels != null) {
      for (Object channel : channels) {
        HashMap<Object, Object> channelMap = (HashMap<Object, Object>) channel;
        if (channelMap.get("label").equals(getChannelLabel())) {
          found = true;
        }
      }
    }
    return found;
  }

  public void unregister() {

  }
}
