package com.redhat.telemetry.integration.sat5.util;

import org.apache.commons.configuration.ConfigurationException;

public class PropertiesHandler extends AbstractPropertiesHandler {
  protected String getFileUrl() {
    return Constants.PROPERTIES_URL;
  }

  public String getRPMName() throws ConfigurationException {
    String rpmName = (String) getProperty(
        Constants.RPM_NAME_PROPERTY,
        Constants.STRING_TYPE,
        Constants.INSIGHTS_CLIENT_RPM_NAME);
    return rpmName;
  }

  public boolean getEnabled() throws ConfigurationException {
    boolean enabled = (Boolean) getProperty(
        Constants.ENABLED_PROPERTY,
        Constants.BOOLEAN_TYPE,
        false);
    return enabled;
  }

  public boolean getDebug() throws ConfigurationException {
    boolean debug = (Boolean) getProperty(
        Constants.DEBUG_PROPERTY, 
        Constants.BOOLEAN_TYPE, 
        false);
    return debug;
  }

  public String getPortalUrl() throws ConfigurationException {
    String portalUrl = (String) getProperty(
        Constants.PORTALURL_PROPERTY, 
        Constants.STRING_TYPE, 
        Constants.PORTAL_URL);
    return portalUrl;
  }

}
