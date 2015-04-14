package com.redhat.telemetry.integration.sat5.satellite;

import com.redhat.telemetry.integration.sat5.util.Constants;

public class Server6System extends AbstractSystem{
  
  public Server6System(String sessionKey) {
    this.sessionKey = sessionKey;
  }

  public Server6System(String sessionKey, int systemId) {
    this.sessionKey = sessionKey;
    this.systemId = systemId;
  }

  public String getChannelLabel() {
    return Constants.CHANNEL_LABEL_RHEL6;
  }

  public String getChannelName() {
    return Constants.CHANNEL_NAME_RHEL6;
  } 
  public String getChannelSummary() {
    return Constants.CHANNEL_SUMMARY_RHEL6;
  } 
  public String getChannelArch() {
    return Constants.CHANNEL_ARCH_RHEL6;
  } 
  public String getChannelParent() {
    return Constants.CHANNEL_PARENT_RHEL6;
  } 
  public String getRepoLabel() {
    return Constants.REPO_LABEL_RHEL6;
  } 
  public String getRepoUrl() {
    return Constants.REPO_URL_RHEL6;
  } 
}
