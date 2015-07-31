package com.redhat.telemetry.integration.sat5.util;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractPropertiesHandler {
  private static Logger LOG = LoggerFactory.getLogger(AbstractPropertiesHandler.class);
  abstract protected String getFileUrl();

  protected Object getProperty(
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

  public void setProperty(String name, String value) throws ConfigurationException {
    PropertiesConfiguration properties = new PropertiesConfiguration(getFileUrl());
    properties.setProperty(name, value);
    properties.save();
  }

  protected void removeProperty(String name) throws ConfigurationException {
    PropertiesConfiguration properties = new PropertiesConfiguration(getFileUrl());
    properties.setProperty(name, "-1");
    properties.save();
  }

  protected Object getPropertyFromFile(String property, String type) throws ConfigurationException {
    PropertiesConfiguration properties = new PropertiesConfiguration();
    properties.load(getFileUrl());
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
