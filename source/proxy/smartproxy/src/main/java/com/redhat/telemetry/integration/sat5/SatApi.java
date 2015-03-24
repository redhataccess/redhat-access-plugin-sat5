package com.redhat.telemetry.integration.sat5;

import java.net.MalformedURLException;
import java.net.URL;

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
    String method = "user.listUsers";
    Object[] response = (Object[]) makeRequest(method, params);
    return response;
  }

  public static Object 
  getUserDetails(String sessionKey, String login) {
    Object[] params = new Object[] {sessionKey, login};
    String method = "user.getDetails";
    Object userDetails = (Object) makeRequest(method, params);
    return userDetails;
  }

  public static Object[]
  listUserRoles(String sessionKey, String username) {
    Object[] params = new Object[] {sessionKey, username};
    Object[] roles = (Object[]) makeRequest("user.listRoles", params);
    return roles;
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
