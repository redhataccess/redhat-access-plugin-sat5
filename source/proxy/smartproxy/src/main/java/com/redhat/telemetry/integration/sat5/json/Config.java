package com.redhat.telemetry.integration.sat5.json;

public class Config {

  private boolean configenabled;
  private boolean enabled;
  private String username;
  private String password;

  public Config() {
  }

  public Config(boolean enabled, String username, String password, boolean configenabled) {
    this.enabled = enabled;
    this.username = username;
    this.password = password;
    this.configenabled = configenabled;
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

  public void setConfigenabled(boolean configenabled) {
    this.configenabled = configenabled;
  }

  public boolean getConfigenabled() {
    return this.configenabled;
  }

}
