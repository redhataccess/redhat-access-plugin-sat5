package com.redhat.telemetry.integration.sat5.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
}
