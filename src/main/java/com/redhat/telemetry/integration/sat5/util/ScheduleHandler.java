package com.redhat.telemetry.integration.sat5.util;

import org.apache.commons.configuration.ConfigurationException;

import com.redhat.telemetry.integration.sat5.util.AbstractPropertiesHandler;

public class ScheduleHandler extends AbstractPropertiesHandler {
  protected String getFileUrl() {
    return Constants.SCHEDULE_CACHE_FILE_URL;
  }

  public void add(Integer systemId, Integer actionId, String type) throws ConfigurationException {
    setProperty(Integer.toString(systemId), type + "_" + Integer.toString(actionId));
  }

  public void remove(Integer systemId) throws ConfigurationException {
    setProperty(Integer.toString(systemId), Constants.NOT_SCHEDULED + "_-1");
  }

  public String getType(Integer systemId) throws ConfigurationException {
    String typeAndActionId = (String) getPropertyFromFile(Integer.toString(systemId), Constants.STRING_TYPE);
    String type = null;
    if (typeAndActionId != null) {
      String[] parts = typeAndActionId.split("_");
      type = parts[0];
    }
    return type;
  }

  public Integer getAction(Integer systemId) throws ConfigurationException {
    String typeAndActionId = (String) getPropertyFromFile(Integer.toString(systemId), Constants.STRING_TYPE);
    String actionId = "-1";
    if (typeAndActionId != null) {
      String[] parts = typeAndActionId.split("_");
      actionId = parts[1];
    }
    return Integer.parseInt(actionId);
  }
}
