package com.redhat.telemetry.integration.sat5.json;

public class Product {

  private String type;
  private String major_version;
  private String minor_version;

  public Product() {
  }

  public Product(String type, String major_version, String minor_version) {
    this.type = type;
    this.major_version = major_version;
    this.minor_version = minor_version;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getType() {
    return this.type;
  }

  public void setMajor_version(String major_version) {
    this.major_version = major_version;
  }

  public String getMajor_version() {
    return this.major_version;
  }

  public void setMinor_version(String minor_version) {
    this.minor_version = minor_version;
  }

  public String getMinor_version() {
    return this.minor_version;
  }
}
