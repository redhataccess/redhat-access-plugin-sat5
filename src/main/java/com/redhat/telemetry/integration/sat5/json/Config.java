package com.redhat.telemetry.integration.sat5.json;

public class Config {

  private boolean enabled;

  public Config() {
  }

  public Config(boolean enabled) {
    this.enabled = enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public boolean getEnabled() {
    return this.enabled;
  }

}
