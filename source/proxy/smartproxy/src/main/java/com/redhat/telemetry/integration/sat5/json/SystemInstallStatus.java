package com.redhat.telemetry.integration.sat5.json;

public class SystemInstallStatus {


  boolean rpmInstalled;
  boolean softwareChannelAssociated;

  public SystemInstallStatus() {
    rpmInstalled = false;
    softwareChannelAssociated = false;
  }

  public SystemInstallStatus(
      boolean softwareChannelAssociated,
      boolean configChannelAssociated,
      boolean rpmInstalled,
      boolean configDeployed) {

    this.rpmInstalled = rpmInstalled;
    this.softwareChannelAssociated = softwareChannelAssociated;
  }

  public boolean getRpmInstalled() {
    return this.rpmInstalled;
  }

  public boolean getSoftwareChannelAssociated() {
    return this.softwareChannelAssociated;
  }

  public void setRpmInstalled(boolean rpmInstalled) {
    this.rpmInstalled = rpmInstalled;
  }

  public void setSoftwareChannelAssociated(boolean softwareChannelAssociated) {
    this.softwareChannelAssociated = softwareChannelAssociated;
  }
}
