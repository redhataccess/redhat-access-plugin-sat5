package com.redhat.telemetry.integration.sat5.json;

import com.redhat.telemetry.integration.sat5.util.Constants;

public class SystemInstallStatus {


  boolean rpmInstalled;
  boolean rpmAvailable;
  String rpmScheduled;

  public SystemInstallStatus() {
    rpmScheduled = Constants.NOT_SCHEDULED;
    rpmInstalled = false;
    rpmAvailable = false;
  }

  public SystemInstallStatus(
      boolean rpmAvailable,
      boolean rpmInstalled,
      String rpmScheduled) {

    this.rpmInstalled = rpmInstalled;
    this.rpmAvailable = rpmAvailable;
    this.rpmScheduled = rpmScheduled;
  }

  public boolean getRpmInstalled() {
    return this.rpmInstalled;
  }

  public boolean getRpmAvailable() {
    return this.rpmAvailable;
  }

  public void setRpmInstalled(boolean rpmInstalled) {
    this.rpmInstalled = rpmInstalled;
  }

  public void setRpmAvailable(boolean rpmAvailable) {
    this.rpmAvailable = rpmAvailable;
  }

  public void setRpmScheduled(String rpmScheduled) {
    this.rpmScheduled = rpmScheduled;
  }

  public String getRpmScheduled () {
    return this.rpmScheduled;
  }
}
