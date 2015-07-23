package com.redhat.telemetry.integration.sat5.util;

import java.io.File;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PropertiesHandler {
  private static Logger LOG = LoggerFactory.getLogger(PropertiesHandler.class);

  public static String getRHEL6ChannelLabel() throws ConfigurationException {
    String rhel6ChannelLabel = (String) getProperty(
        Constants.RHEL6_CHANNEL_LABEL_PROPERTY,
        Constants.STRING_TYPE,
        "rhel-x86_64-server-6");
    return rhel6ChannelLabel;
  }

  public static String getRHEL7ChannelLabel() throws ConfigurationException {
    String rhel7ChannelLabel = (String) getProperty(
        Constants.RHEL7_CHANNEL_LABEL_PROPERTY,
        Constants.STRING_TYPE,
        "rhel-x86_64-server-7");
    return rhel7ChannelLabel;
  }

  public static String getRPMName() throws ConfigurationException {
    String rpmName = (String) getProperty(
        Constants.RPM_NAME_PROPERTY,
        Constants.STRING_TYPE,
        Constants.INSIGHTS_CLIENT_RPM_NAME);
    return rpmName;
  }

  public static boolean getEnabled() throws ConfigurationException {
    boolean enabled = (Boolean) getProperty(
        Constants.ENABLED_PROPERTY,
        Constants.BOOLEAN_TYPE,
        Boolean.toString(false));
    return enabled;
  }

  public static boolean getDebug() throws ConfigurationException {
    boolean debug = (Boolean) getProperty(
        Constants.DEBUG_PROPERTY, 
        Constants.BOOLEAN_TYPE, 
        Boolean.toString(false));
    return debug;
  }

  private static Object getProperty(
      String propertyName, 
      String propertyType,
      String defaultValue) throws ConfigurationException {

    Object property = defaultValue;
    try {
      property = getProperty(propertyName, propertyType);
      if (property == null) {
        LOG.info(
            propertyName + 
            " property is missing. Setting it to the default value: " + 
            defaultValue);
        property = defaultValue;
      }
      LOG.debug(propertyName + " property: " + property);
    } catch (Exception e) {
      LOG.info(
          propertyName + 
          " property is missing. Setting it to the default value: " + 
          defaultValue);
      setProperty(propertyName, defaultValue);
    }
    return property;
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
