package com.redhat.telemetry.integration.sat5.util;

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
        false);
    return enabled;
  }

  public static boolean getDebug() throws ConfigurationException {
    boolean debug = (Boolean) getProperty(
        Constants.DEBUG_PROPERTY, 
        Constants.BOOLEAN_TYPE, 
        false);
    return debug;
  }

  public static String getPortalUrl() throws ConfigurationException {
    String portalUrl = (String) getProperty(
        Constants.PORTALURL_PROPERTY, 
        Constants.STRING_TYPE, 
        Constants.PORTAL_URL);
    return portalUrl;
  }

  private static Object getProperty(
      String propertyName, 
      String propertyType,
      Object defaultValue) throws ConfigurationException {

    Object property = defaultValue;
    try {
      property = getPropertyFromFile(propertyName, propertyType);
      if (property == null) {
        LOG.info(
            propertyName + 
            " property is null. Setting it to the default value: " + 
            defaultValue);
        setProperty(propertyName, defaultValue.toString());
        property = defaultValue;
      }
    } catch (Exception e) {
      LOG.info(
          "Problem while retrieving property, " + propertyName + ". Setting it to the default value: " + 
          defaultValue, e);
      setProperty(propertyName, defaultValue.toString());
      property = defaultValue;
    }
    return property;
  }

  public static void setProperty(String name, String value) throws ConfigurationException {
    PropertiesConfiguration properties = new PropertiesConfiguration(Constants.PROPERTIES_URL);
    properties.setProperty(name, value);
    properties.save();
  }

  private static Object getPropertyFromFile(String property, String type) throws ConfigurationException {
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
