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

  public void createRepo() {
    if (!repoExists()) {
      SatApi.createRepo(
          this.sessionKey, 
          this.getRepoLabel(), 
          "YUM", 
          this.getRepoUrl());
    } 
  }

  @SuppressWarnings("unchecked")
  public boolean repoExists() {
    Object[] repos = SatApi.listUserRepos(this.sessionKey);
    boolean exists = false;
    if (repos != null) {
      for (Object repo : repos) {
        HashMap<Object, Object> repoMap = (HashMap<Object, Object>) repo;
        String label = (String) repoMap.get("label"); 
        if (label.equals(getRepoLabel())) {
          exists = true;
        }
      }
    }
    return exists;
  }

  public boolean createChannel() {
    boolean response = false;

    if (!channelExists()) {
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
  public boolean channelExists() {
    Object[] channels = SatApi.listSoftwareChannels(this.sessionKey);
    boolean response = false;
    if (channels != null) {
      for (Object channel : channels) {
        HashMap<Object, Object> channelMap = (HashMap<Object, Object>) channel;
        String label = (String) channelMap.get("label");
        if (label.equals(getChannelLabel())) {
          response = true;
        }
      }
    }
    return response;
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
        found = true;
      }
    }
    return found;
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
        int currentVersion = (int) revisionMap.get("revision");
        if (currentVersion > version) {
          version = currentVersion;
        }
      }
    }
    return version;
  }
}
