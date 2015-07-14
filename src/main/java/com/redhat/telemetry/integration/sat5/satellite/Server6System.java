package com.redhat.telemetry.integration.sat5.satellite;

import org.apache.commons.configuration.ConfigurationException;

import com.redhat.telemetry.integration.sat5.util.PropertiesHandler;

public class Server6System extends AbstractSystem{
  
  public Server6System(String sessionKey) {
    this.sessionKey = sessionKey;
  }

  public Server6System(String sessionKey, int systemId) {
    this.sessionKey = sessionKey;
    this.systemId = systemId;
  }

  public String getChannelLabel() throws ConfigurationException {
    return PropertiesHandler.getRHEL6ChannelLabel();
  }
}
