package com.redhat.telemetry.integration.sat5.rest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
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
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.jasypt.util.text.StrongTextEncryptor;
import org.json.JSONException;
import org.json.JSONObject;

import com.jcabi.aspects.Loggable;
import com.redhat.telemetry.integration.sat5.json.BranchInfo;
import com.redhat.telemetry.integration.sat5.json.PortalResponse;
import com.redhat.telemetry.integration.sat5.satellite.SatApi;
import com.redhat.telemetry.integration.sat5.util.Constants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/r/insights")
@Loggable
public class ProxyService {
  @Context ServletContext context;
  private String portalUrl = "https://access.redhat.com/r/insights/";
  private Logger LOG = LoggerFactory.getLogger(ProxyService.class);

  @GET
  @Path("/v1/branch_info")
  @Produces("application/json")
  public BranchInfo getBranchId() throws UnknownHostException, JSONException, IOException, InterruptedException {
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
    BranchInfo branchInfo = new BranchInfo(hostname, -1);
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
  @Path("/{path: .*}")
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  @Produces(MediaType.APPLICATION_JSON)
  public Response proxyRootPostMultiPart(
      @Context Request request,
      @Context UriInfo uriInfo,
      @PathParam("path") String path,
      @HeaderParam("Content-Type") String contentType,
      @CookieParam("pxt-session-cookie") String user,
      byte[] body) 
      throws JSONException, IOException, ConfigurationException {
    return proxy(path, user, uriInfo, request, contentType, MediaType.APPLICATION_JSON, body);
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

  @Loggable
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
    properties.load(Constants.PROPERTIES_URL);
    boolean enabled = properties.getBoolean(Constants.ENABLED_PROPERTY);
    String configPortalUrl = properties.getString(Constants.PORTALURL_PROPERTY);
    if (configPortalUrl != null) {
      if (configPortalUrl.charAt(configPortalUrl.length() - 1) != '/') {
        configPortalUrl = configPortalUrl + "/";
      }
      this.portalUrl = configPortalUrl;
    }
    if (!enabled) {
      throw new WebApplicationException(new Throwable("Red Hat Access Insights service was disabled by the Satellite 5 administrator. The administrator must enable Red Hat Access Insights via the Satellite 5 GUI to continue using this service."), Response.Status.FORBIDDEN);
    }

    CloseableHttpClient client = HttpClientBuilder.create().build();
    String branchId = InetAddress.getLocalHost().getHostName();
    ArrayList<Integer> leafIds = new ArrayList<Integer>();
    String subsetHash = null;

    //TODO: add a header to let the client decide when to follow subset path. Default no.
    if (user != null) {
      leafIds = SatApi.getUsersSystemIDs(user);
      subsetHash = createSubsetHash(leafIds, branchId);
    }
    path = addQueryToPath(path, uriInfo.getRequestUri().toString());

    HashMap<String, String> pathType = parsePathType(path);
    String pathTypeInt = pathType.get("type");
    LOG.debug("Pathtype: " + pathTypeInt);

    if (pathTypeInt.equals(Constants.SYSTEM_REPORTS_PATH) && pathType.get("id") != null) {
      String leafId = pathType.get("id");
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
    } else if (user != null && !pathTypeInt.equals(Constants.SYSTEMS_STATUS_PATH)) {
      path = addSubsetToPath(path, subsetHash);
    }

    if (!pathTypeInt.equals(Constants.UPLOADS_PATH)) {
      String prepend = "?";
      if (path.contains("?")) {
        prepend = "&";
      }
      path = path + prepend + Constants.BRANCH_ID_KEY + "=" + branchId;
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
    if (portalResponse.getStatusCode() == HttpServletResponse.SC_PRECONDITION_FAILED &&
        ! pathTypeInt.equals(Constants.UPLOADS_PATH)) {
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
    if (method == Constants.METHOD_GET) {
      request = new HttpGet(this.portalUrl + path);
    } else if (method == Constants.METHOD_POST) {
        request = new HttpPost(this.portalUrl + path);
        request.addHeader(HttpHeaders.CONTENT_TYPE, requestType);
        if (entity != null) {
          ((HttpPost) request).setEntity(entity);
        } else if (body != null) {
          ((HttpPost) request).setEntity(new ByteArrayEntity(body));
        }
    } else if (method == Constants.METHOD_DELETE) {
      request = new HttpDelete(this.portalUrl + path);
    } else {
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
   *  5 - /uploads
   * -1 - undefined
   */
  private HashMap<String, String> parsePathType(String path) {
    LOG.debug("Path: " + path);
    Pattern systemPattern = Pattern.compile("v1/systems/?(\\?.*)?$");
    Matcher systemMatcher = systemPattern.matcher(path);

    Pattern systemStatusPattern = Pattern.compile("v1/systems/status/?(\\?.*)?$");
    Matcher systemStatusMatcher = systemStatusPattern.matcher(path);

    Pattern systemReportsPattern = Pattern.compile("v1/systems/(.*)/reports/?(\\?.*)?$");
    Matcher systemReportsMatcher = systemReportsPattern.matcher(path);

    Pattern reportsPattern = Pattern.compile("v1/reports/?(\\?.*)?$");
    Matcher reportsMatcher = reportsPattern.matcher(path);

    Pattern acksPattern = Pattern.compile("v1/acks/?(\\?.*)$");
    Matcher acksMatcher = acksPattern.matcher(path);

    Pattern rulesPattern = Pattern.compile("v1/rules/?(\\?.*)$");
    Matcher rulesMatcher = rulesPattern.matcher(path);

    Pattern uploadsPattern = Pattern.compile("uploads(/.*)?(/\\?.*)?$");
    Matcher uploadsMatcher = uploadsPattern.matcher(path);

    HashMap<String, String> response = new HashMap<String, String>();
    if (systemMatcher.matches()) {
      response.put("type", Constants.SYSTEMS_PATH);
      response.put("index", Integer.toString(path.indexOf("systems")));
    } else if (systemReportsMatcher.matches()) {
      response.put("type", Constants.SYSTEM_REPORTS_PATH);
      response.put("index", Integer.toString(path.indexOf("reports")));
      String id = (String) systemReportsMatcher.group(1);
      if (id != null) {
        response.put("id", id);
      }
    } else if (reportsMatcher.matches()) {
      response.put("type", Constants.REPORTS_PATH);
      response.put("index", Integer.toString(path.indexOf("reports")));
    } else if (acksMatcher.matches()) {
      response.put("type", Constants.ACKS_PATH);
      response.put("index", Integer.toString(path.indexOf("acks")));
    } else if (rulesMatcher.matches()) {
      response.put("type", Constants.RULES_PATH);
      response.put("index", Integer.toString(path.indexOf("rules")));
    } else if (uploadsMatcher.matches()) {
      response.put("type", Constants.UPLOADS_PATH);
      response.put("index", Integer.toString(path.indexOf("uploads")));
    } else if (systemStatusPattern.matches()) {
      response.put("type", Constants.SYSTEMS_STATUS_PATH);
      response.put("index", Integer.toString(path.indexOf("systems")));
    } else {
      response.put("type", "-1");
      response.put("index", "-1");
    }
    return response;
  }

  /**
   * Manipulate the original request path by inserting subset/<id> after api/v1
   */
  private String addSubsetToPath(String path, String hash) {
    String index = "-1";
    HashMap<String, String> pathType = parsePathType(path);
    index = pathType.get("index");

    if (index != "-1") {
      path = new StringBuilder(path).insert(
          Integer.parseInt(index), Constants.SUBSETS_URL + hash + "/").toString();
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
    properties.load(Constants.PROPERTIES_URL);
    String password = decryptPassword(properties.getString("password"));
    BasicHeader authHeader = new BasicHeader(HttpHeaders.AUTHORIZATION,
      "Basic " + DatatypeConverter.printBase64Binary(
                (properties.getString("username") + ":" + 
                 password).getBytes("UTF-8")));
    return authHeader;
  }

  /**
   * Decrypt the stored password
   */
  private String decryptPassword(String encryptedPassword) {
    StrongTextEncryptor textEncryptor = new StrongTextEncryptor();
    textEncryptor.setPassword(Constants.ENCRYPTION_PASSWORD);
    return textEncryptor.decrypt(encryptedPassword);
  }

  /**
   * Sort leafIds alphabetically, concat into a string, then sha1
   * to build the subset ID.
   */
  private String createSubsetHash(ArrayList<Integer> leafIds, String branchId) {
    Collections.sort(leafIds);
    return branchId + "__" + DigestUtils.sha1Hex(StringUtils.join(leafIds.toArray()));
  }
}
