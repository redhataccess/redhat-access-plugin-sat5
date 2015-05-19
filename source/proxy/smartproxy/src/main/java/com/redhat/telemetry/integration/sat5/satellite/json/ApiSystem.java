package com.redhat.telemetry.integration.sat5.satellite.json;

public class ApiSystem {

  int id;
  String name;
  String type;

  public ApiSystem() {
  }

  public ApiSystem(
      int id, 
      String name,
      String type) {

    this.id = id;
    this.name = name;
    this.type = type;
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

  public void setType(String type) {
    this.type = type;
  }

  public String getType() {
    return this.type;
  }
}
