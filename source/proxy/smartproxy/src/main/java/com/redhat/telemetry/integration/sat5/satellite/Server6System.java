package com.redhat.telemetry.integration.sat5.satellite;

import com.redhat.telemetry.integration.sat5.util.Constants;

public class Server6System extends AbstractSystem{
  
  //rhel6 software channel
  private String channelLabel = Constants.CHANNEL_LABEL_RHEL6;
  private String channelName = Constants.CHANNEL_NAME_RHEL6;
  private String channelSummary = Constants.CHANNEL_SUMMARY_RHEL6;
  private String channelArch = Constants.CHANNEL_ARCH_RHEL6;
  private String channelParent = Constants.CHANNEL_PARENT_RHEL6;
  private String repoLabel = Constants.REPO_LABEL_RHEL6;
  private String repoUrl = Constants.REPO_URL_RHEL6;

  public Server6System(String sessionKey) {
    this.sessionKey = sessionKey;
  }

  public Server6System(String sessionKey, int systemId) {
    this.sessionKey = sessionKey;
    this.systemId = systemId;
  }

  public String getChannelLabel() {
    return this.channelLabel;
  }

  public String getChannelName() {
    return this.channelName;
  } 
  public String getChannelSummary() {
    return this.channelSummary;
  } 
  public String getChannelArch() {
    return this.channelArch;
  } 
  public String getChannelParent() {
    return this.channelParent;
  } 
  public String getRepoLabel() {
    return this.repoLabel;
  } 
  public String getRepoUrl() {
    return this.repoUrl;
  } 
}
