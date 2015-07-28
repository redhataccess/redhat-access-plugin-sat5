package com.redhat.telemetry.integration.sat5.json;

import com.redhat.telemetry.integration.sat5.json.SystemInstallStatus;

public class Status {

  int id;
  SystemInstallStatus installationStatus;
  boolean enabled; 

  public Status() {
  }

  public Status(
      int id, 
      SystemInstallStatus installationStatus, 
      boolean enabled) {

    this.id = id;
    this.installationStatus = installationStatus;
    this.enabled = enabled;
  }

  public void setId(int id) {
    this.id = id;
  }

  public int getId() {
    return this.id;
  }

  public void setInstallationStatus(SystemInstallStatus installationStatus) {
    this.installationStatus = installationStatus;
  }

  public SystemInstallStatus getInstallationStatus() {
    return this.installationStatus;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public boolean getEnabled() {
    return this.enabled;
  }
}
