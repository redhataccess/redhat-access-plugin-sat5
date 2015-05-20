package com.redhat.telemetry.integration.sat5.satellite;

import java.util.ArrayList;
import java.util.HashMap;

import com.redhat.telemetry.integration.sat5.util.Constants;

public abstract class AbstractSystem {

  protected String sessionKey;
  protected int systemId;
  public abstract String getChannelLabel();
  public abstract String getChannelName();
  public abstract String getChannelSummary();
  public abstract String getChannelArch();
  public abstract String getChannelParent();
  public abstract String getRepoLabel();
  public abstract String getRepoUrl();

  private Object[] userRepos = null;

  public void createRepo() {
    if (parentChannelExists() && !insightsRepoExists()) {
      SatApi.createRepo(
          this.sessionKey, 
          this.getRepoLabel(), 
          "YUM", 
          this.getRepoUrl());
    } 
  }

  @SuppressWarnings("unchecked")
  private boolean repoExists(String repoLabel) {
    populateUserRepos();
    boolean exists = false;
    if (this.userRepos != null) {
      for (Object repo : this.userRepos) {
        HashMap<Object, Object> repoMap = (HashMap<Object, Object>) repo;
        String label = (String) repoMap.get("label"); 
        if (label.equals(repoLabel)) {
          exists = true;
        }
      }
    }
    return exists;
  }

  private void populateUserRepos() {
    if (userRepos == null) {
      this.userRepos = SatApi.listUserRepos(this.sessionKey);
    }
  }

  public boolean insightsRepoExists() {
    return repoExists(getRepoLabel());
  }

  public boolean createChannel() {
    boolean response = false;

    if (parentChannelExists() && !insightsChannelExists()) {
      int created = SatApi.createChannel(
          this.sessionKey, 
          getChannelLabel(),
          getChannelName(),
          getChannelSummary(),
          getChannelArch(),
          getChannelParent());
      if (created == 0) {
        response = false;
      } else {
        response = true;
        //associate repo with this channel
        SatApi.associateRepo(this.sessionKey, getChannelLabel(), getRepoLabel());
        SatApi.syncRepo(this.sessionKey, getChannelLabel());
      }
    } else {
      //channel already created
      //TODO: verify the repo is associated?
      response = true;
    }
    return response;
  }

  @SuppressWarnings("unchecked")
  public boolean channelExists(String channelLabel) {
    Object[] channels = SatApi.listSoftwareChannels(this.sessionKey);
    boolean response = false;
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

  public boolean insightsChannelExists() {
    return channelExists(getChannelLabel());
  }

  public boolean parentChannelExists() {
    return channelExists(getChannelParent());
  }

  /**
   * Check if a system has the RPM installed
   */
  @SuppressWarnings("unchecked")
  public boolean rpmInstalled() {
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
        if (packageName.equals(Constants.PACKAGE_NAME)) {
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

  /**
   * (Un)Subscribe a system to the insights software channel
   */
  @SuppressWarnings("unchecked")
  public boolean updateSoftwareChannels(boolean addInsightsChannel) {
    //list existing channels system is subscribed to
    Object[] systemChannels = SatApi.listSystemChannels(this.sessionKey, systemId);
    ArrayList<String> systemChannelLabels = new ArrayList<String>();
    String channelLabel = getChannelLabel();
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
      SatApi.setChildChannels(this.sessionKey, systemId, systemChannelLabels);
    if (response != 1) {
      return false;
    } else {
      return true;
    }
  }

  @SuppressWarnings("unchecked")
  public int getPackageId() {
    String channelLabel = getChannelLabel();
    //grab the redhat-access-proactive packageId from the channel
    Object[] channelPackages = 
      SatApi.listAllPackagesInChannel(this.sessionKey, channelLabel);
    int packageId = -1;
    if (channelPackages != null) {
      for (Object channelPackage : channelPackages) {
        HashMap<Object, Object> channelPackageMap = (HashMap<Object, Object>) channelPackage;
        String packageName = (String) channelPackageMap.get("name");
        if (packageName.equals(Constants.PACKAGE_NAME)) {
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
  public boolean softwareChannelAssociated() {
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

  /**
   * Check if a system has the config channel associated
   */
  @SuppressWarnings("unchecked")
  public boolean configChannelAssociated() {
    Object[] channels = SatApi.listConfigChannels(this.sessionKey, systemId);
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
   * Check if a system has the latest config file rev deployed
   */
  @SuppressWarnings("unchecked")
  public boolean configDeployed() {
    boolean response = false;
    int latestRevision = getLatestFileRevision();
    if (latestRevision > 0) {
      ArrayList<String> paths = new ArrayList<String>();
      paths.add(Constants.CONFIG_PATH);
      Object[] fileInfos = SatApi.lookupFileInfo(this.sessionKey, systemId, paths, 1);
      if (fileInfos != null) {
        for (Object fileInfo : fileInfos) {
          HashMap<Object, Object> fileInfoMap = (HashMap<Object, Object>) fileInfo;
          String channel = (String) fileInfoMap.get("channel");
          if (channel.equals(Constants.CONFIG_CHANNEL_NAME)) {
            int revision = (Integer) fileInfoMap.get("revision");
            if (revision == latestRevision) {
              response = true;
            }
          }
        }
      }
    }

    return response;
  }

  /**
   * Get the latest insights config file revision
   */
  @SuppressWarnings("unchecked")
  public int getLatestFileRevision() {
    Object[] revisions =
      (Object[]) SatApi.getFileRevisions(
          this.sessionKey, Constants.CONFIG_CHANNEL_LABEL, Constants.CONFIG_PATH);
    int version = -1;
    if (revisions != null) {
      for (Object revision : revisions) {
        HashMap<Object, Object> revisionMap = (HashMap<Object, Object>) revision;
        int currentVersion = (Integer) revisionMap.get("revision");
        if (currentVersion > version) {
          version = currentVersion;
        }
      }
    }
    return version;
  }
}
