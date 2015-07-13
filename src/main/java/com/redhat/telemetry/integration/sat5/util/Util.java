package com.redhat.telemetry.integration.sat5.util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.redhat.telemetry.integration.sat5.satellite.SatApi;

import ch.qos.logback.classic.Level;

public class Util {
  public static String getSatelliteHostname() throws IOException, InterruptedException {
    Logger LOG = LoggerFactory.getLogger(Util.class);
    String hostname = "";
    try {
      hostname = InetAddress.getLocalHost().getHostName();
    } catch (Exception e) {
      LOG.debug("Hostname lookup failed. Falling back to Runtime.getRuntime().exec('hostname')");
      Process p = Runtime.getRuntime().exec("hostname");
      p.waitFor();
      BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
      hostname = reader.readLine();
    }

    return hostname;
  }

  public static void setLogLevel(boolean debug) {
    if (debug) {
      enableDebugMode();
    } else {
      disableDebugMode();
    }
  }

  public static void disableDebugMode() {
    ch.qos.logback.classic.Logger root = 
      (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
    root.setLevel(Level.INFO);

    org.apache.log4j.Logger log4jRoot = org.apache.log4j.LogManager.getRootLogger();
    log4jRoot.setLevel(org.apache.log4j.Level.INFO);
  }

  public static void enableDebugMode() {
    ch.qos.logback.classic.Logger root = 
      (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
    root.setLevel(Level.DEBUG);

    org.apache.log4j.Logger log4jRoot = org.apache.log4j.LogManager.getRootLogger();
    log4jRoot.setLevel(org.apache.log4j.Level.DEBUG);
  }

  public static String getLog() throws IOException {
    BufferedReader reader = new BufferedReader(new FileReader("/var/log/rhn/rhai.log"));
    try {
      StringBuilder builder = new StringBuilder();
      String line = reader.readLine();
      while (line != null) {
        builder.append(line);
        builder.append("\n");
        line = reader.readLine();
      }
      return builder.toString();
    } finally {
      reader.close();
    }
  };

  /**
   * Check if a user is the satellite administrator
   */
  public static boolean userIsAdmin(String sessionKey, String username) {
    Object[] userRoles = SatApi.listUserRoles(sessionKey, username);
    boolean response = false;
    if (userRoles != null) {
      for (Object role : userRoles) {
        if (role.equals("satellite_admin")) {
          response = true;
        }
      }
    }
    return response;
  }
}
