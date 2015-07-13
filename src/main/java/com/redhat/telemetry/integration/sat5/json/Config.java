package com.redhat.telemetry.integration.sat5.json;

public class Config {

  private boolean enabled;
  private boolean debug;

  public Config() {
  }

  public Config(boolean enabled, boolean debug) {
    this.enabled = enabled;
    this.debug = debug;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public boolean getEnabled() {
    return this.enabled;
  }

  public boolean getDebug() {
    return debug;
  }

  public void setDebug(boolean debug) {
    this.debug = debug;
  }

}
