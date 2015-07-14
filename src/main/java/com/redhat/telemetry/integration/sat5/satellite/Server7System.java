package com.redhat.telemetry.integration.sat5.satellite;

import org.apache.commons.configuration.ConfigurationException;

import com.redhat.telemetry.integration.sat5.util.PropertiesHandler;

public class Server7System extends AbstractSystem{
  
  public Server7System(String sessionKey) {
    this.sessionKey = sessionKey;
  }

  public Server7System(String sessionKey, int systemId) {
    this.sessionKey = sessionKey;
    this.systemId = systemId;
  }

  public String getChannelLabel() throws ConfigurationException {
    return PropertiesHandler.getRHEL7ChannelLabel();
  }
}
