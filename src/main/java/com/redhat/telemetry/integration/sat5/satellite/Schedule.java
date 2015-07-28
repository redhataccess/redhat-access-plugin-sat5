package com.redhat.telemetry.integration.sat5.satellite;

public class Schedule {
  private int id = -1;
  private String type = null;

  public Schedule(int id, String type) {
    this.id = id;
    this.type = type;
  }

  public int getId() {
    return this.id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getType() {
    return this.type;
  }

  public void setType(String type) {
    this.type = type;
  }
}
