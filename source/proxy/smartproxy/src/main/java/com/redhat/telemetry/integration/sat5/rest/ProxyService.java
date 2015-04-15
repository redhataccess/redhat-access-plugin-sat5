package com.redhat.telemetry.integration.sat5.rest;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.CookieParam;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.UriInfo;
import javax.xml.bind.DatatypeConverter;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.lang.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import org.jboss.resteasy.spi.ForbiddenException;
import org.json.JSONException;
import org.json.JSONObject;

import com.redhat.telemetry.integration.sat5.json.BranchInfo;
import com.redhat.telemetry.integration.sat5.json.PortalResponse;
import com.redhat.telemetry.integration.sat5.util.Constants;

@Path("/rs/telemetry")
public class ProxyService {
  @Context ServletContext context;


  @GET
  @Path("/api/v1/branch_info")
  @Produces("application/json")
  public BranchInfo getBranchId() throws UnknownHostException, JSONException {
    BranchInfo branchInfo = new BranchInfo(InetAddress.getLocalHost().getHostName(), -1);
    return branchInfo;
  }

  @GET
  @Path("/")
  @Produces(MediaType.APPLICATION_JSON)
  public Response proxyRootGetMultiPart(
      @Context Request request,
      @Context UriInfo uriInfo,
      @CookieParam("pxt-session-cookie") String user)
      throws JSONException, IOException, ConfigurationException {
    return proxy("", user, uriInfo, request, null, MediaType.MULTIPART_FORM_DATA, null);
  }

  @POST
  @Path("/")
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  @Produces(MediaType.APPLICATION_JSON)
  public Response proxyRootPostMultiPart(
      @Context Request request,
      @Context UriInfo uriInfo,
      @HeaderParam("Content-Type") String contentType,
      @CookieParam("pxt-session-cookie") String user,
      byte[] body) 
      throws JSONException, IOException, ConfigurationException {
    return proxy("", user, uriInfo, request, contentType, MediaType.APPLICATION_JSON, body);
  }

  @GET
  @POST
  @Path("/{path: .*}")
  @Produces(MediaType.TEXT_PLAIN)
  public Response proxyGetTextPlain(
      @Context Request request,
      @Context UriInfo uriInfo,
      @PathParam("path") String path,
      @CookieParam("pxt-session-cookie") String user)
      throws JSONException, IOException, ConfigurationException {
    return proxy(path, user, uriInfo, request, null, MediaType.TEXT_PLAIN, null);
  }

  @POST
  @Path("/{path: .*}")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response proxyPost(
      @Context Request request,
      @Context UriInfo uriInfo,
      @PathParam("path") String path,
      @CookieParam("pxt-session-cookie") String user,
      byte[] body) 
      throws JSONException, IOException, ConfigurationException {
    return proxy(path, user, uriInfo, request, MediaType.APPLICATION_JSON, MediaType.APPLICATION_JSON, body);
  }

  @GET
  @POST
  @Path("/{path: .*}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response proxyGet(
      @Context Request request,
      @Context UriInfo uriInfo,
      @PathParam("path") String path,
      @CookieParam("pxt-session-cookie") String user)
      throws JSONException, IOException, ConfigurationException {
    return proxy(path, user, uriInfo, request, null, MediaType.APPLICATION_JSON, null);
  }

  private Response proxy(
      String path, 
      String user, 
      UriInfo uriInfo, 
      Request request,
      String requestType,
      String responseType,
      byte[] body) 
          throws JSONException, IOException, ConfigurationException {
    //load config to check if service is enabled
    PropertiesConfiguration properties = new PropertiesConfiguration();
    properties.load(context.getResourceAsStream(Constants.PROPERTIES_URL));
    boolean enabled = properties.getBoolean(Constants.ENABLED_PROPERTY);
    if (!enabled) {
      throw new ForbiddenException("Red Hat Access Insights service was disabled by the Satellite 5 administrator. The administrator must enable Red Hat Access Insights via the Satellite 5 GUI to continue using this service.");
    }

    CloseableHttpClient client = HttpClientBuilder.create().build();
    String branchId = InetAddress.getLocalHost().getHostName();
    ArrayList<Integer> leafIds = new ArrayList<Integer>();
    String subsetHash = null;

    //TODO: add a header to let the client decide when to follow subset path. Default no.
    if (user != null) {
      leafIds = getUsersSystemIDs(user);
      subsetHash = createSubsetHash(leafIds, branchId);
    }
    path = addQueryToPath(path, uriInfo.getRequestUri().toString());

    HashMap<String, Integer> pathType = parsePathType(path);
    int pathTypeInt = pathType.get("type");

    if (pathTypeInt == Constants.SYSTEM_REPORTS_PATH) {
      String leafId = Integer.toString(pathType.get("id"));
      PortalResponse getIdResponse = proxyRequest(
        client,
        user,
        request.getMethod(),
        Constants.API_URL + Constants.BRANCH_URL + branchId + "/" + Constants.LEAF_URL + leafId,
        null,
        requestType,
        responseType,
        body);
      JSONObject responseJson = new JSONObject(getIdResponse.getEntity());
      String machineId = (String) responseJson.get(Constants.MACHINE_ID_KEY);
      path = Constants.SYSTEMS_URL + machineId + "/" + Constants.REPORTS_URL;
    } else if (pathTypeInt == Constants.ACKS_PATH || pathTypeInt == Constants.RULES_PATH) {
      String prepend = "?";
      if (path.contains("?")) {
        prepend = "&";
      }
      path = path + prepend + Constants.BRANCH_ID_KEY + "=" + branchId;
    } else if (user != null) {
      path = addSubsetToPath(path, subsetHash);
    }

    PortalResponse portalResponse = 
      proxyRequest(
          client, 
          user, 
          request.getMethod(), 
          path, 
          null,
          requestType,
          responseType,
          body);
    if (portalResponse.getStatusCode() == 
        HttpServletResponse.SC_PRECONDITION_FAILED) { 
      portalResponse =
        proxyRequest(
            client, 
            user,
            Constants.METHOD_POST, 
            Constants.API_URL + Constants.SUBSETS_URL, 
            buildNewSubsetPostBody(subsetHash, leafIds, branchId),
            MediaType.APPLICATION_JSON,
            MediaType.APPLICATION_JSON,
            body);
      if (portalResponse.getStatusCode() ==
          HttpServletResponse.SC_CREATED) {
        portalResponse =
          proxyRequest(
              client, 
              user, 
              request.getMethod(), 
              path, 
              null,
              requestType,
              responseType,
              body);
      }
    }
    Response finalResponse = buildFinalResponse(portalResponse);
    client.close();
    return finalResponse;
  }

  /**
   * Make a request to the portal
   */
  private PortalResponse proxyRequest(
      CloseableHttpClient client,
      String user,
      String method, 
      String path,
      HttpEntity entity,
      String requestType,
      String responseType,
      byte[] body) throws ConfigurationException, IOException {

    HttpRequestBase request;
    switch (method) {
      case Constants.METHOD_GET: 
        request = new HttpGet(Constants.PORTAL_URL + path);
        break;
      case Constants.METHOD_DELETE: 
        request = new HttpDelete(Constants.PORTAL_URL + path);
        break;
      case Constants.METHOD_POST: 
        request = new HttpPost(Constants.PORTAL_URL + path);
        request.addHeader(HttpHeaders.CONTENT_TYPE, requestType);
        if (entity != null) {
          ((HttpPost) request).setEntity(entity);
        } else if (body != null) {
          ((HttpPost) request).setEntity(new ByteArrayEntity(body));
        }
        break;
      case Constants.METHOD_PUT: 
        request = new HttpPut(Constants.PORTAL_URL + path);
        request.addHeader(HttpHeaders.CONTENT_TYPE, requestType);
        if (entity != null) {
          ((HttpPut) request).setEntity(entity);
        } else if (body != null) {
          ((HttpPut) request).setEntity(new ByteArrayEntity(body));
        }
        break;
      default:
        throw new WebApplicationException(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    }

    request.addHeader(getBasicAuthHeader());
    request.addHeader(HttpHeaders.ACCEPT, responseType);
    HttpResponse response = client.execute(request);
    PortalResponse portalResponse = 
      new PortalResponse(
          response.getStatusLine().getStatusCode(), 
          EntityUtils.toString(response.getEntity(), "UTF-8"), 
          response.getAllHeaders());
    request.releaseConnection();
    return portalResponse;
  }

  /**
   * Add the query params from the jax-rs request to the apache request to the portal
   */
  private String addQueryToPath(
      String path, 
      String fullUri) {
	  
    String response = path;
    int index = fullUri.indexOf("?");
    if (index == -1) {
      index = fullUri.indexOf("&");
    }
    if (index != -1) {
      String query = fullUri.substring(index);
      if (query.isEmpty()) {
        response = path;
      } else {
        response = path + query;
      }
    }
    return response;
  }

  /**
   * Given a path, determine the type
   *
   * Returns map with type, index to start of type, [id]
   *
   *  0 - /systems
   *  1 - /systems/.../reports
   *  2 - /reports
   *  3 - /acks
   *  4 - /rules
   * -1 - undefined
   */
  private HashMap<String, Integer> parsePathType(String path) {
    Pattern systemPattern = Pattern.compile("api/v1/systems/?(\\?.*)?$");
    Matcher systemMatcher = systemPattern.matcher(path);

    Pattern systemReportsPattern = Pattern.compile("api/v1/systems/(.*)/reports/?(\\?.*)?$");
    Matcher systemReportsMatcher = systemReportsPattern.matcher(path);

    Pattern reportsPattern = Pattern.compile("api/v1/reports/?(\\?.*)?$");
    Matcher reportsMatcher = reportsPattern.matcher(path);

    Pattern acksPattern = Pattern.compile("api/v1/acks/?(\\?.*)$");
    Matcher acksMatcher = acksPattern.matcher(path);

    Pattern rulesPattern = Pattern.compile("api/v1/rules/?(\\?.*)$");
    Matcher rulesMatcher = rulesPattern.matcher(path);

    HashMap<String, Integer> response = new HashMap<String, Integer>();
    if (systemMatcher.matches()) {
      response.put("type", Constants.SYSTEMS_PATH);
      response.put("index", path.indexOf("systems"));
    } else if (systemReportsMatcher.matches()) {
      response.put("type", Constants.SYSTEM_REPORTS_PATH);
      response.put("index", path.indexOf("reports"));
      response.put("id", Integer.parseInt(systemReportsMatcher.group(1)));
    } else if (reportsMatcher.matches()) {
      response.put("type", Constants.REPORTS_PATH);
      response.put("index", path.indexOf("reports"));
    } else if (acksMatcher.matches()) {
      response.put("type", Constants.ACKS_PATH);
      response.put("index", path.indexOf("acks"));
    } else if (rulesMatcher.matches()) {
      response.put("type", Constants.RULES_PATH);
      response.put("index", path.indexOf("rules"));
    } else {
      response.put("type", -1);
      response.put("index", -1);
    }
    return response;
  }

  /**
   * Manipulate the original request path by inserting subset/<id> after api/v1
   */
  private String addSubsetToPath(String path, String hash) {
    int index = -1;
    HashMap<String, Integer> pathType = parsePathType(path);
    index = pathType.get("index");

    if (index != -1) {
      path = new StringBuilder(path).insert(
          index, Constants.SUBSETS_URL + hash + "/").toString();
    }
    return path;
  }

  /**
   * Build the JSON request to make a new subset
   */
  private StringEntity buildNewSubsetPostBody(
      String hash, 
      ArrayList<Integer> ids,
      String branchId) 
      throws UnknownHostException {
    StringEntity entity = new StringEntity(
      "{\"" + Constants.HASH_KEY + "\":\"" + hash + "\"," + 
       "\"" + Constants.LEAF_IDS_KEY + "\":[" + StringUtils.join(ids.toArray(), ",") + "]," +
       "\"" + Constants.BRANCH_ID_KEY + "\":\"" + branchId + "\"}",
      ContentType.APPLICATION_JSON);
    return entity;
  }

  /**
   * Map the response from the portal to the proxy's response
   */
  private Response buildFinalResponse(PortalResponse portalResponse)
        throws IOException {
    ResponseBuilder finalResponse = 
      Response.status(portalResponse.getStatusCode());
    for (Header header : portalResponse.getHeaders()) {
      //TODO: probably want to white list headers instead
      if (!header.getName().equals(HttpHeaders.TRANSFER_ENCODING) && 
          !header.getName().equals(HttpHeaders.VARY)) {
        finalResponse.header(header.getName(), header.getValue());
      }
    }
    finalResponse.entity(portalResponse.getEntity());
    return finalResponse.build();
  }

  /**
   * Read from the disk to retrieve the basic auth credentials
   */
  private BasicHeader getBasicAuthHeader() 
      throws ConfigurationException, UnsupportedEncodingException {
    PropertiesConfiguration properties = new PropertiesConfiguration();
    properties.load(context.getResourceAsStream("WEB-INF/insights.properties"));
    BasicHeader authHeader = new BasicHeader(HttpHeaders.AUTHORIZATION,
      "Basic " + DatatypeConverter.printBase64Binary(
                (properties.getString("username") + ":" + 
                 properties.getString("password")).getBytes("UTF-8")));
    return authHeader;
  }

  /**
   * Sort leafIds alphabetically, concat into a string, then sha1
   * to build the subset ID.
   */
  private String createSubsetHash(ArrayList<Integer> leafIds, String branchId) {
    Collections.sort(leafIds);
    return branchId + "__" + DigestUtils.sha1Hex(StringUtils.join(leafIds.toArray()));
  }

  /**
   * Get the list of satellite systems visible to the logged in user.
   */
  @SuppressWarnings("unchecked")
  private ArrayList<Integer> getUsersSystemIDs(String user) {
    XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
    try {
      config.setServerURL(new URL(Constants.SAT5_RPC_API_URL));
    } catch (MalformedURLException e) {
      e.printStackTrace();
    }
    XmlRpcClient satClient = new XmlRpcClient();
    satClient.setConfig(config);

    ArrayList<Integer> systemList = new ArrayList<Integer>();
    try {
      Object[] params = new Object[] {user};
      Object[] systems = (Object[]) satClient.execute("system.listSystems", params);
      for (Object system : systems) {
        int id = (Integer) ((HashMap<Object, Object>) system).get("id");
        systemList.add(id);
      }
    } catch (XmlRpcException e) {
      e.printStackTrace();
    }
    return systemList;
  }
}
