package com.redhat.telemetry.integration.sat5.json;

public class Config {

  private boolean enabled;
  private String username;
  private String password;

  public Config() {
  }

  public Config(boolean enabled, String username, String password) {
    this.enabled = enabled;
    this.username = username;
    this.password = password;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public boolean getEnabled() {
    return this.enabled;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public String getPassword() {
    return this.password;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getUsername() {
    return this.username;
  }
}
