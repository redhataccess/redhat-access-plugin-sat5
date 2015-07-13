package com.redhat.telemetry.integration.sat5.util;

import java.io.File;
import java.util.NoSuchElementException;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PropertiesHandler {
  private static Logger LOG = LoggerFactory.getLogger(PropertiesHandler.class);

  public static boolean getEnabled() throws ConfigurationException {
    return (Boolean) getProperty(Constants.ENABLED_PROPERTY, Constants.BOOLEAN_TYPE);
  }

  public static boolean getDebug() throws ConfigurationException {
    boolean debug = false;
    try {
      debug = (Boolean) getProperty(Constants.DEBUG_PROPERTY, Constants.BOOLEAN_TYPE);
    } catch (NoSuchElementException e) {
      setProperty(Constants.DEBUG_PROPERTY, "false");
    }
    return debug;
  }

  public static String getPortalUrl() throws ConfigurationException {
    return (String) getProperty(Constants.PORTALURL_PROPERTY, Constants.STRING_TYPE);
  }

  private static void setProperty(String name, String value) throws ConfigurationException {
    PropertiesConfiguration properties = new PropertiesConfiguration();
    properties.setFile(new File(Constants.PROPERTIES_URL));
    if (!name.equals(Constants.ENABLED_PROPERTY)) {
      properties.setProperty(Constants.ENABLED_PROPERTY, getEnabled());
    }
    if (!name.equals(Constants.DEBUG_PROPERTY)) {
      properties.setProperty(Constants.DEBUG_PROPERTY, getDebug());
    }
    if (!name.equals(Constants.PORTALURL_PROPERTY)) {
      properties.setProperty(Constants.PORTALURL_PROPERTY, getPortalUrl());
    }
    properties.setProperty(name, value);
    properties.save();
  }

  private static Object getProperty(String property, String type) throws ConfigurationException {
    LOG.debug("Loading properties file.");
    PropertiesConfiguration properties = new PropertiesConfiguration();
    properties.load(Constants.PROPERTIES_URL);
    Object response = null;
    if (type.equals("string")) {
      response = properties.getString(property);
    } else if (type.equals("boolean")) {
      response = properties.getBoolean(property);
    } else {
      throw new IllegalArgumentException("Invalid property type.");
    }
    return response;
  }
}
