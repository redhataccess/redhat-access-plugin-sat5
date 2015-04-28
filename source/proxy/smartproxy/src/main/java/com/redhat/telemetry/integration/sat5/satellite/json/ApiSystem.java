package com.redhat.telemetry.integration.sat5.satellite.json;

public class ApiSystem {

  int id;
  String name;

  public ApiSystem() {
  }

  public ApiSystem(
      int id, 
      String name) {

    this.id = id;
    this.name = name;
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
}
