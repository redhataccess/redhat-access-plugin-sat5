package com.redhat.telemetry.integration.sat5.satellite;

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

  public static Object 
  getSystemName(String sessionKey, int serverId) {
    Object[] params = new Object[] {sessionKey, serverId};
    Object response = 
      (Object) makeRequest("system.getName", params);
    return response;
  }

  public static int
  schedulePackageInstall(String sessionKey, int serverId, ArrayList<Integer> packageIds, int delay) {
    Object[] params = new Object[] {sessionKey, serverId, packageIds, new Date(System.currentTimeMillis() + delay)};
    int response = 
      (Integer) makeRequest("system.schedulePackageInstall", params);
    return response;
  }

  public static int
  schedulePackageRemove(String sessionKey, int serverId, ArrayList<Integer> packageIds) {
    Object[] params = new Object[] {sessionKey, serverId, packageIds, new Date(System.currentTimeMillis())};
    int response = 
      (Integer) makeRequest("system.schedulePackageRemove", params);
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
      (Integer) makeRequest("channel.software.syncRepo", params);
    return response;
  }

  public static int
  setChildChannels(String sessionKey, int serverId, ArrayList<String> channelLabels) {
    Object[] params = new Object[] {sessionKey, serverId, channelLabels};
    int response = 
      (Integer) makeRequest("system.setChildChannels", params);
    return response;
  }

  public static int
  configChannelExists(String sessionKey, String configChannelLabel) {
    Object[] params = new Object[] {sessionKey, configChannelLabel};
    int response = 
      (Integer) makeRequest("configchannel.channelExists", params);
    return response;
  }

  public static int
  addConfigChannelsToSystem(
      String sessionKey,
      ArrayList<Integer> systemIds,
      ArrayList<String> channelLabels,
      boolean addToTop) {
    Object[] params = new Object[] {sessionKey, systemIds, channelLabels, addToTop};
    int response = 
      (Integer) makeRequest("system.config.addChannels", params);
    return response;
  }

  public static int
  removeConfigChannelsFromSystem(
      String sessionKey,
      ArrayList<Integer> systemIds,
      ArrayList<String> channelLabels) {
    Object[] params = new Object[] {sessionKey, systemIds, channelLabels};
    int response = 
      (Integer) makeRequest("system.config.removeChannels", params);
    return response;
  }

  public static int
  deployAllSystems(
      String sessionKey,
      String channelLabel) {
    Object[] params = new Object[] {sessionKey, channelLabel};
    int response = 
      (Integer) makeRequest("configchannel.deployAllSystems", params);
    return response;
  }

  public static Object
  configCreateOrUpdatePath(
      String sessionKey, 
      String channelLabel, 
      String path,
      boolean isDir, 
      HashMap<String, Object> pathInfo) {

    Object[] params = new Object[] {sessionKey, channelLabel, path, isDir, pathInfo};
    Object response = 
      (Object) makeRequest("configchannel.createOrUpdatePath", params);
    return response;
  }

  public static Object
  createConfigChannel(
      String sessionKey, 
      String channelLabel, 
      String channelName, 
      String channelDescription) {

    Object[] params = new Object[] {sessionKey, channelLabel, channelName, channelDescription};
    Object response = 
      (Object) makeRequest("configchannel.create", params);
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

  public static Object[]
  lookupFileInfo(
      String sessionKey, 
      int serverId, 
      ArrayList<String> paths, 
      int searchLocal) {
    Object[] params = new Object[] {sessionKey, serverId, paths, searchLocal};
    Object[] response = 
      (Object[]) makeRequest("system.config.lookupFileInfo", params);
    return response;
  }

  public static Object[]
  listConfigChannels(String sessionKey, int serverId) {
    Object[] params = new Object[] {sessionKey, serverId};
    Object[] response = 
      (Object[]) makeRequest("system.config.listChannels", params);
    return response;
  }

  public static Object[]
  getFileRevisions(String sessionKey, String channelLabel, String filePath) {
    Object[] params = new Object[] {sessionKey, channelLabel, filePath};
    Object[] response = 
      (Object[]) makeRequest("configchannel.getFileRevisions", params);
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
        return (Integer)responseMap.get("id");
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
    HashMap<String, String> gpgKey = new HashMap<String, String>();
    gpgKey.put("url", "http://sat57.usersys.redhat.com/pub/rhinsights.gpg");
    gpgKey.put("id", "A3753C6C");
    gpgKey.put("fingerprint", "C08B 170E 016E CDEB 10A5  AFCB 7649 4742 A375 3C6C");

    Object[] params = new Object[] {
      sessionKey, 
      label, 
      name, 
      summary, 
      archLabel, 
      parent, 
      "sha256", 
      gpgKey};
    Object response =
      (Object) makeRequest("channel.software.create", params);
    if (response != null) {
      return (Integer)response;
    } else {
      return -1;
    }
  }
  
  @SuppressWarnings("unchecked")
  public static ArrayList<Integer> getUsersSystemIDs(String user) {
    Object[] params = new Object[] {user};
    Object[] systems = (Object[]) makeRequest("system.listSystems", params);
    ArrayList<Integer> systemList = new ArrayList<Integer>();
    for (Object system : systems) {
      int id = (Integer) ((HashMap<Object, Object>) system).get("id");
      systemList.add(id);
    }
    return systemList;
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
