package com.redhat.telemetry.integration.sat5.json;

public class SystemInstallStatus {


  boolean rpmInstalled;
  boolean softwareChannelAssociated;
  boolean rpmScheduled;

  public SystemInstallStatus() {
    rpmScheduled = false;
    rpmInstalled = false;
    softwareChannelAssociated = false;
  }

  public SystemInstallStatus(
      boolean softwareChannelAssociated,
      boolean rpmInstalled,
      boolean rpmScheduled) {

    this.rpmInstalled = rpmInstalled;
    this.softwareChannelAssociated = softwareChannelAssociated;
    this.rpmScheduled = rpmScheduled;
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

  public void setRpmScheduled(boolean rpmScheduled) {
    this.rpmScheduled = rpmScheduled;
  }

  public boolean getRpmScheduled () {
    return this.rpmScheduled;
  }
}
