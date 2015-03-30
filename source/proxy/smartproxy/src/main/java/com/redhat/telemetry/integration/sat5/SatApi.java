package com.redhat.telemetry.integration.sat5;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;

public class SatApi {

  private final static String SAT5_RPC_API_URL = 
    "http://127.0.0.1/rpc/api";

  public SatApi() {
  }

  public static Object[]
  listUsersInOrg(String sessionKey) {
    Object[] params = new Object[] {sessionKey};
    Object[] response = 
      (Object[]) makeRequest("user.listUsers", params);
    return response;
  }

  public static Object 
  getUserDetails(String sessionKey, String login) {
    Object[] params = new Object[] {sessionKey, login};
    Object userDetails = 
      (Object) makeRequest("user.getDetails", params);
    return userDetails;
  }

  public static Object 
  getSystemDetails(String sessionKey, int serverId) {
    Object[] params = new Object[] {sessionKey, serverId};
    Object response = 
      (Object) makeRequest("system.getDetails", params);
    return response;
  }

  public static int
  schedulePackageInstall(String sessionKey, int serverId, ArrayList<Integer> packageIds) {
    Object[] params = new Object[] {sessionKey, serverId, packageIds, new Date(System.currentTimeMillis() + 60000)};
    int response = 
      (int) makeRequest("system.schedulePackageInstall", params);
    return response;
  }

  public static Object[]
  listAllInstallablePackages(String sessionKey, int serverId) {
    Object[] params = new Object[] {sessionKey, serverId};
    Object[] response = 
      (Object[]) makeRequest("system.listAllInstallablePackages", params);
    return response;
  }

  public static Object[]
  listInstalledPackagesFromChannel(String sessionKey, int serverId, String channelLabel) {
    Object[] params = new Object[] {sessionKey, serverId, channelLabel};
    Object[] response = 
      (Object[]) makeRequest("system.listPackagesFromChannel", params);
    return response;
  }

  public static Object[]
  listAllPackagesInChannel(String sessionKey, String channelLabel) {
    Object[] params = new Object[] {sessionKey, channelLabel};
    Object[] response = 
      (Object[]) makeRequest("channel.software.listAllPackages", params);
    return response;
  }

  public static Object[]
  listUserRoles(String sessionKey, String username) {
    Object[] params = new Object[] {sessionKey, username};
    Object[] roles = 
      (Object[]) makeRequest("user.listRoles", params);
    return roles;
  }

  public static Object[]
  listSystems(String sessionKey) {
    Object[] params = new Object[] {sessionKey};
    Object[] response = 
      (Object[]) makeRequest("system.listSystems", params);
    return response;
  }

  public static Object[]
  listSoftwareChannels(String sessionKey) {
    Object[] params = new Object[] {sessionKey};
    Object[] response = 
      (Object[]) makeRequest("channel.listSoftwareChannels", params);
    return response;
  }

  public static Object[]
  listUserRepos(String sessionKey) {
    Object[] params = new Object[] {sessionKey};
    Object[] response = 
      (Object[]) makeRequest("channel.software.listUserRepos", params);
    return response;
  }

  public static Object[]
  listArches(String sessionKey) {
    Object[] params = new Object[] {sessionKey};
    Object[] response = 
      (Object[]) makeRequest("channel.software.listArches", params);
    return response;
  }

  public static Object
  associateRepo(String sessionKey, String channelLabel, String repoLabel) {
    Object[] params = new Object[] {sessionKey, channelLabel, repoLabel};
    Object response = 
      (Object) makeRequest("channel.software.associateRepo", params);
    return response;
  }

  public static int
  syncRepo(String sessionKey, String channelLabel) {
    Object[] params = new Object[] {sessionKey, channelLabel};
    int response = 
      (int) makeRequest("channel.software.syncRepo", params);
    return response;
  }

  public static int
  setChildChannels(String sessionKey, int serverId, ArrayList<String> channelLabels) {
    Object[] params = new Object[] {sessionKey, serverId, channelLabels};
    int response = 
      (int) makeRequest("system.setChildChannels", params);
    return response;
  }

  public static Object[]
  listVendorChannels(String sessionKey) {
    Object[] params = new Object[] {sessionKey};
    Object[] response = 
      (Object[]) makeRequest("channel.listVendorChannels", params);
    return response;
  }

  public static Object[]
  listSystemChannels(String sessionKey, int serverId) {
    Object[] params = new Object[] {sessionKey, serverId};
    Object[] response = 
      (Object[]) makeRequest("channel.software.listSystemChannels", params);
    return response;
  }

  @SuppressWarnings("unchecked")
  public static int
  createRepo(String sessionKey, String label, String type, String url) {
    Object[] params = new Object[] {sessionKey, label, type, url};
    Object response =
      (Object) makeRequest("channel.software.createRepo", params);
    if (response != null) {
      HashMap<Object, Object> responseMap = (HashMap<Object, Object>) response;
      if (responseMap.get("id") != null) {
        return (int)responseMap.get("id");
      } else {
        return -1;
      }
    } else {
      return -1;
    }
  }

  public static int
  createChannel(
      String sessionKey, 
      String label, 
      String name, 
      String summary, 
      String archLabel,
      String parent) {

    Object[] params = new Object[] {sessionKey, label, name, summary, archLabel, parent};
    Object response =
      (Object) makeRequest("channel.software.create", params);
    if (response != null) {
      return (int)response;
    } else {
      return -1;
    }
  }

  private static Object 
  makeRequest(String method, Object[] params) {
    XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
    try {
      config.setServerURL(new URL(SAT5_RPC_API_URL));
    } catch (MalformedURLException e) {
      e.printStackTrace();
    }
    XmlRpcClient satClient = new XmlRpcClient();
    satClient.setConfig(config);

    Object response = null;
    try {
      response = satClient.execute(method, params);
    } catch (XmlRpcException e) {
      e.printStackTrace();
    }
    return response;
  }
}
