package com.redhat.telemetry.integration.sat5.json;

import com.redhat.telemetry.integration.sat5.json.SystemInstallStatus;

public class Status {

  int id;
  String version;
  SystemInstallStatus installationStatus;
  boolean enabled; // 0 -> unchecked, 1 -> partial, 2 -> checked
  boolean validType;

  public Status() {
  }

  public Status(
      int id, 
      String version, 
      SystemInstallStatus installationStatus, 
      boolean enabled,
      boolean validType) {

    this.id = id;
    this.version = version;
    this.installationStatus = installationStatus;
    this.enabled = enabled;
    this.validType = validType;
  }

  public void setId(int id) {
    this.id = id;
  }

  public int getId() {
    return this.id;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public String getVersion() {
    return this.version;
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

  public void setValidType(boolean validType) {
    this.validType = validType;
  }

  public boolean getValidType() {
    return this.validType;
  }
}
