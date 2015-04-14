package com.redhat.telemetry.integration.sat5.satellite;

import com.redhat.telemetry.integration.sat5.util.Constants;

public class Server7System extends AbstractSystem{
  
  public Server7System(String sessionKey) {
    this.sessionKey = sessionKey;
  }

  public Server7System(String sessionKey, int systemId) {
    this.sessionKey = sessionKey;
    this.systemId = systemId;
  }

  public String getChannelLabel() {
    return Constants.CHANNEL_LABEL_RHEL7;
  }

  public String getChannelName() {
    return Constants.CHANNEL_NAME_RHEL7;
  } 
  public String getChannelSummary() {
    return Constants.CHANNEL_SUMMARY_RHEL7;
  } 
  public String getChannelArch() {
    return Constants.CHANNEL_ARCH_RHEL7;
  } 
  public String getChannelParent() {
    return Constants.CHANNEL_PARENT_RHEL7;
  } 
  public String getRepoLabel() {
    return Constants.REPO_LABEL_RHEL7;
  } 
  public String getRepoUrl() {
    return Constants.REPO_URL_RHEL7;
  } 
}
