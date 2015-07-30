package com.redhat.telemetry.integration.sat5.satellite;

public class Schedule {
  private Integer id = -1;
  private String type = null;

  public Schedule(Integer id, String type) {
    this.id = id;
    this.type = type;
  }

  public Integer getId() {
    return this.id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public String getType() {
    return this.type;
  }

  public void setType(String type) {
    this.type = type;
  }
}
