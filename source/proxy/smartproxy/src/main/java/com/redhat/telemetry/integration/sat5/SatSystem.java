package com.redhat.telemetry.integration.sat5;

public class SatSystem {

  int id;
  String name;
  String version;

  public SatSystem() {
  }

  public SatSystem(int id, String name, String version) {
    this.id = id;
    this.name = name;
    this.version = version;
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
}
