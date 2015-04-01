package com.redhat.telemetry.integration.sat5;

public class SystemInstallStatus {


  boolean rpmInstalled;
  boolean softwareChannelAssociated;
  boolean configChannelAssociated;
  boolean configDeployed;

  public SystemInstallStatus() {
    rpmInstalled = false;
    softwareChannelAssociated = false;
    configChannelAssociated = false;
    configDeployed = false;
  }

  public SystemInstallStatus(
      boolean softwareChannelAssociated,
      boolean configChannelAssociated,
      boolean rpmInstalled,
      boolean configDeployed) {

    this.rpmInstalled = rpmInstalled;
    this.softwareChannelAssociated = softwareChannelAssociated;
    this.configChannelAssociated = configChannelAssociated;
    this.configDeployed = configDeployed;
  }

  public boolean getRpmInstalled() {
    return this.rpmInstalled;
  }

  public boolean getSoftwareChannelAssociated() {
    return this.softwareChannelAssociated;
  }

  public boolean getConfigChannelAssociated() {
    return this.configChannelAssociated;
  }

  public boolean getConfigDeployed() {
    return this.configDeployed;
  }

  public void setRpmInstalled(boolean rpmInstalled) {
    this.rpmInstalled = rpmInstalled;
  }

  public void setSoftwareChannelAssociated(boolean softwareChannelAssociated) {
    this.softwareChannelAssociated = softwareChannelAssociated;
  }

  public void setConfigChannelAssociated(boolean configChannelAssociated) {
    this.configChannelAssociated = configChannelAssociated;
  }

  public void setConfigDeployed(boolean configDeployed) {
    this.configDeployed = configDeployed;
  }
}
