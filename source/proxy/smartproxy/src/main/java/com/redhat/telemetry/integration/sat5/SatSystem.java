package com.redhat.telemetry.integration.sat5;

public class SatSystem {

  int id;
  String name;
  String version;
  SystemInstallStatus installationStatus;
  boolean enabled;
  boolean validType;

  public SatSystem() {
  }

  public SatSystem(
      int id, 
      String name, 
      String version, 
      SystemInstallStatus installationStatus, 
      boolean enabled,
      boolean validType) {

    this.id = id;
    this.name = name;
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

  public void setName(String name) {
    this.name = name;
  }

  public String getName() {
    return this.name;
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
