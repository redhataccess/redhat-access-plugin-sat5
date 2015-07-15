package com.redhat.telemetry.integration.sat5.util;

import java.io.File;
import java.util.NoSuchElementException;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PropertiesHandler {
  private static Logger LOG = LoggerFactory.getLogger(PropertiesHandler.class);

  public static String getRHEL6ChannelLabel() throws ConfigurationException {
    String label = "rhel-x86_64-server-6";
    try {
      label = (String) getProperty(Constants.RHEL6_CHANNEL_LABEL_PROPERTY, Constants.STRING_TYPE);
    } catch (NoSuchElementException e) {
      setProperty(Constants.RHEL6_CHANNEL_LABEL_PROPERTY, label);
    }
    return label;
  }

  public static String getRHEL7ChannelLabel() throws ConfigurationException {
    String label = "rhel-x86_64-server-7.1.z";
    try {
      label = (String) getProperty(Constants.RHEL7_CHANNEL_LABEL_PROPERTY, Constants.STRING_TYPE);
    } catch (NoSuchElementException e) {
      LOG.debug("rhel7channellabel property missing, setting it to default");
      setProperty(Constants.RHEL7_CHANNEL_LABEL_PROPERTY, label);
    }
    return label;
  }

  public static String getRPMName() throws ConfigurationException {
    String rpmName = "redhat-access-insights";
    try {
      rpmName = (String) getProperty(Constants.RPM_NAME_PROPERTY, Constants.STRING_TYPE);
    } catch (NoSuchElementException e) {
      LOG.debug("rpmname property missing, setting it to default");
      setProperty(Constants.RPM_NAME_PROPERTY, rpmName);
    }
    return rpmName;
  }

  public static boolean getEnabled() throws ConfigurationException {
    boolean enabled = false;
    try {
      enabled = (Boolean) getProperty(Constants.ENABLED_PROPERTY, Constants.BOOLEAN_TYPE);
    } catch (NoSuchElementException e) {
      LOG.debug("enabled property missing, setting it to default");
      setProperty(Constants.ENABLED_PROPERTY, Boolean.toString(enabled));
    }
    return enabled;
  }

  public static boolean getDebug() throws ConfigurationException {
    boolean debug = false;
    try {
      debug = (Boolean) getProperty(Constants.DEBUG_PROPERTY, Constants.BOOLEAN_TYPE);
    } catch (NoSuchElementException e) {
      LOG.debug("debug property missing, setting it to default");
      setProperty(Constants.DEBUG_PROPERTY, Boolean.toString(debug));
    }
    return debug;
  }

  public static String getPortalUrl() throws ConfigurationException {
    return (String) getProperty(Constants.PORTALURL_PROPERTY, Constants.STRING_TYPE);
  }

  public static void setProperty(String name, String value) throws ConfigurationException {
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
    if (!name.equals(Constants.RPM_NAME_PROPERTY)) {
      properties.setProperty(Constants.RPM_NAME_PROPERTY, getRPMName());
    }
    if (!name.equals(Constants.RHEL6_CHANNEL_LABEL_PROPERTY)) {
      properties.setProperty(Constants.RHEL6_CHANNEL_LABEL_PROPERTY, getRHEL6ChannelLabel());
    }
    if (!name.equals(Constants.RHEL7_CHANNEL_LABEL_PROPERTY)) {
      properties.setProperty(Constants.RHEL7_CHANNEL_LABEL_PROPERTY, getRHEL7ChannelLabel());
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
