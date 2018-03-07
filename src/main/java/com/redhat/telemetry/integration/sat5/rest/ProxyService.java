package com.redhat.telemetry.integration.sat5.rest;

import java.io.IOException;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.UnrecoverableKeyException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.CookieParam;
import javax.ws.rs.DELETE;
import javax.ws.rs.Encoded;
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

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.lang.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.jboss.resteasy.spi.NotFoundException;
import org.json.JSONException;

import com.redhat.telemetry.integration.sat5.json.BranchInfo;
import com.redhat.telemetry.integration.sat5.json.Product;
import com.redhat.telemetry.integration.sat5.json.PortalResponse;
import com.redhat.telemetry.integration.sat5.portal.InsightsApiClient;
import com.redhat.telemetry.integration.sat5.portal.InsightsApiUtils;
import com.redhat.telemetry.integration.sat5.satellite.SatApi;
import com.redhat.telemetry.integration.sat5.util.Constants;
import com.redhat.telemetry.integration.sat5.util.Util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Encoded
@Path("/r/insights")
public class ProxyService {
  @Context ServletContext context;
  private Logger LOG = LoggerFactory.getLogger(ProxyService.class);

  @GET
  @Encoded
  @Path("/v1/branch_info")
  @Produces("application/json")
  public BranchInfo getBranchId() {
    String hostname = "";
    try {
      hostname = Util.getSatelliteHostname();
    } catch (Exception e) {
      LOG.error("Unable to retrieve Satellite hostname at GET /r/insights/v1/branch_info", e);
      throw new WebApplicationException(
          new Throwable(Constants.INTERNAL_SERVER_ERROR_MESSAGE),
          Response.Status.INTERNAL_SERVER_ERROR);
    }
    BranchInfo branchInfo = new BranchInfo(hostname, -1, new Product("SAT", "5", "7"), hostname);
    return branchInfo;
  }

  @GET
  @Encoded
  @Path("/")
  @Produces(MediaType.APPLICATION_JSON)
  public Response proxyRootGetMultiPart(
      @Context Request request,
      @Context UriInfo uriInfo,
      @HeaderParam("user-agent") String userAgent,
      @HeaderParam("systemid") String systemId,
      @HeaderParam("satellite_user") String userID,
      @CookieParam("pxt-session-cookie") String sessionCookie) {
    try {
      return proxy(
          "",
          sessionCookie,
          userID,
          uriInfo,
          request,
          null,
          MediaType.MULTIPART_FORM_DATA,
          null,
          userAgent,
          systemId);
    } catch (Exception e) {
      LOG.error("Exception in ProxyService GET /", e);
      throw new WebApplicationException(
          new Throwable(Constants.INTERNAL_SERVER_ERROR_MESSAGE),
          Response.Status.INTERNAL_SERVER_ERROR);
    }
  }

  @POST
  @Encoded
  @Path("/{path: .*}")
  @Consumes({MediaType.MULTIPART_FORM_DATA,MediaType.APPLICATION_FORM_URLENCODED})
  @Produces(MediaType.APPLICATION_JSON)
  public Response proxyRootPostMultiPart(
      @Context Request request,
      @Context UriInfo uriInfo,
      @PathParam("path") String path,
      @HeaderParam("Content-Type") String contentType,
      @HeaderParam("user-agent") String userAgent,
      @HeaderParam("systemid") String systemId,
      @HeaderParam("satellite_user") String userID,
      @CookieParam("pxt-session-cookie") String sessionCookie,
      byte[] body) {
    try {
      return proxy(
          path,
          sessionCookie,
          userID,
          uriInfo,
          request,
          contentType,
          MediaType.APPLICATION_JSON,
          body,
          userAgent,
          systemId);
    } catch (Exception e) {
      LOG.error("Exception in ProxyService POST /* (Content-Type: " + contentType, e);
      throw new WebApplicationException(
          new Throwable(Constants.INTERNAL_SERVER_ERROR_MESSAGE),
          Response.Status.INTERNAL_SERVER_ERROR);
    }
  }

  @GET
  @POST
  @Encoded
  @Path("/{path: .*}")
  @Produces(MediaType.TEXT_PLAIN)
  public Response proxyGetTextPlain(
      @Context Request request,
      @Context UriInfo uriInfo,
      @PathParam("path") String path,
      @HeaderParam("user-agent") String userAgent,
      @HeaderParam("systemid") String systemId,
      @HeaderParam("satellite_user") String userID,
      @CookieParam("pxt-session-cookie") String sessionCookie) {
    try {
      return proxy(
          path,
          sessionCookie,
          userID,
          uriInfo,
          request,
          null,
          MediaType.TEXT_PLAIN,
          null,
          userAgent,
          systemId);
    } catch (Exception e) {
      LOG.error("Exception in ProxyService GET /* (Accept: Text/plain)", e);
      throw new WebApplicationException(
          new Throwable(Constants.INTERNAL_SERVER_ERROR_MESSAGE),
          Response.Status.INTERNAL_SERVER_ERROR);
    }
  }

  @POST
  @Encoded
  @Path("/{path: .*}")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response proxyPost(
      @Context Request request,
      @Context UriInfo uriInfo,
      @PathParam("path") String path,
      @CookieParam("pxt-session-cookie") String sessionCookie,
      @HeaderParam("systemid") String systemId,
      @HeaderParam("satellite_user") String userID,
      @HeaderParam("user-agent") String userAgent,
      byte[] body) {
    try {
      return proxy(
          path,
          sessionCookie,
          userID,
          uriInfo,
          request,
          MediaType.APPLICATION_JSON,
          MediaType.APPLICATION_JSON,
          body,
          userAgent,
          systemId);
    } catch (Exception e) {
      LOG.error("Exception in ProxyService POST /*", e);
      throw new WebApplicationException(
          new Throwable(Constants.INTERNAL_SERVER_ERROR_MESSAGE),
          Response.Status.INTERNAL_SERVER_ERROR);
    }
  }

  @GET
  @POST
  @DELETE
  @Encoded
  @Path("/{path: .*}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response proxyGet(
      @Context Request request,
      @Context UriInfo uriInfo,
      @PathParam("path") String path,
      @HeaderParam("user-agent") String userAgent,
      @HeaderParam("systemid") String systemId,
      @HeaderParam("satellite_user") String userID,
      @CookieParam("pxt-session-cookie") String sessionCookie,
      byte[] body) {
    try {
      return proxy(
          path,
          sessionCookie,
          userID,
          uriInfo,
          request,
          null,
          MediaType.APPLICATION_JSON,
          body,
          userAgent,
          systemId);
    } catch (NotFoundException e) {
      LOG.debug("Resource not found: " + path);
      throw e;
    } catch (Exception e) {
      LOG.error("Exception in ProxyService GET /* (Accept: application/json)", e);
      throw new WebApplicationException(
          new Throwable(Constants.INTERNAL_SERVER_ERROR_MESSAGE),
          Response.Status.INTERNAL_SERVER_ERROR);
    }
  }

  //@Loggable
  private Response proxy(
      String path,
      String sessionCookie,
      String userID,
      UriInfo uriInfo,
      Request request,
      String requestType,
      String responseType,
      byte[] body,
      String userAgent,
      String systemId)
          throws JSONException,
                 IOException,
                 ConfigurationException,
                 NoSuchAlgorithmException,
                 KeyStoreException,
                 CertificateException,
                 KeyManagementException,
                 UnrecoverableKeyException,
                 InvalidKeySpecException,
                 InterruptedException {
    LOG.debug("checking if request originated from a valid gui session...");
    if ((sessionCookie != null) && (Util.sessionIsValid(sessionCookie,userID))) {
      LOG.debug("valid session");
    } else {
      LOG.debug("invalid session. Check for systemid header");

      if (systemId == null) {
        LOG.debug("No systemid header or session. Rejecting the request.");
        return Util.buildErrorResponse(
            Response.Status.UNAUTHORIZED.getStatusCode(),
            Util.buildJsonMessage(Constants.MISSING_SYSTEMID_MESSAGE));
      }
      try {
        LOG.debug("Validating systemid header...");
        Object monitorIsEnabled = SatApi.isMonitoringEnabledBySystemId(systemId);
        LOG.debug("monitorIsEnabled: " + monitorIsEnabled.toString());
      } catch (Exception e) {
        LOG.debug("systemid is invalid!");
        return Util.buildErrorResponse(
            Response.Status.UNAUTHORIZED.getStatusCode(),
            Util.buildJsonMessage(Constants.UNAUTHORIZED_MESSAGE));
      }
    }

    InsightsApiClient client = new InsightsApiClient();
    String branchId = Util.getSatelliteHostname();
    ArrayList<Integer> leafIds = new ArrayList<Integer>();
    String subsetHash = null;
    boolean isPassThrough = isPassThruRequest(userAgent);
    LOG.debug("Request user agent " + userAgent);
    LOG.debug("Request is pass through? " + isPassThrough);

    //If user is set, assume call originated from satellite, not uploader
    if ((sessionCookie != null) && (!isPassThrough)) {
      LOG.debug("Creating subset hash.");
      leafIds = SatApi.getUsersSystemIDs(sessionCookie);
      subsetHash = createSubsetHash(leafIds, branchId);
      LOG.debug("User: " + sessionCookie);
      LOG.debug("Subset Hash: " + subsetHash);
    }
    path = addQueryToPath(path, uriInfo.getRequestUri().toString());
    LOG.debug("Path with query: " + path);
    LOG.debug("Determining request type from path.");
    HashMap<String, String> pathType = parsePathType(path);
    String pathTypeInt = pathType.get("type");
    LOG.debug("Pathtype: " + pathTypeInt);
    if (!isPassThrough){
      //Only inspect and modify request if its NOT a pass through
      if (pathTypeInt.equals(Constants.SYSTEM_REPORTS_PATH) && pathType.get("id") != null) {
        LOG.debug("Request is for an individual system's reports. GET machine ID from portal.");
        String leafId = pathType.get("id");
        String machineId = InsightsApiUtils.leafIdToMachineId(leafId);
        path = Constants.SYSTEMS_URL + machineId + "/" + Constants.REPORTS_URL;
        LOG.debug("MachineID Path: " + path);
      } else if (sessionCookie != null &&
          !pathTypeInt.equals(Constants.RULES_PATH) &&
          !pathTypeInt.equals(Constants.ACKS_PATH)) { //TODO: whitelist subset paths instead of blacklist
        path = addSubsetToPath(path, subsetHash);
        LOG.debug("Path with subset: " + path);
      }

      if (!pathTypeInt.equals(Constants.UPLOADS_PATH)) {
        LOG.debug("Adding branchID query param to URI.");
        String prepend = "?";
        if (path.contains("?")) {
          prepend = "&";
        }
        path = path + prepend + Constants.BRANCH_ID_KEY + "=" + branchId;
        LOG.debug("Path with branchId query param: " + path);
      }
        
    }
    LOG.debug("Forwarding request to portal.");
    PortalResponse portalResponse =
      client.makeRequest(
          request.getMethod(),
          path,
          body,
          requestType,
          responseType);
    if (portalResponse.getStatusCode() == HttpServletResponse.SC_PRECONDITION_FAILED &&
        ! pathTypeInt.equals(Constants.UPLOADS_PATH) && !isPassThrough) {
      LOG.debug("Got a 412. Assuming this means the subset doesn't exist. Create the subset.");
      portalResponse =
        client.makeRequest(
            Constants.METHOD_POST,
            Constants.API_URL + Constants.SUBSETS_URL,
            buildNewSubsetPostBody(subsetHash, leafIds, branchId),
            MediaType.APPLICATION_JSON,
            MediaType.APPLICATION_JSON);
      if (portalResponse.getStatusCode() ==
          HttpServletResponse.SC_CREATED) {
        LOG.debug("Subset created successfully. Forward the original request a second time.");
        portalResponse =
          client.makeRequest(
              request.getMethod(),
              path,
              body,
              requestType,
              responseType);
      }
    }
    Response finalResponse = buildFinalResponse(portalResponse);
    client.close();
    return finalResponse;
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
    Pattern systemPattern = Pattern.compile("v[0-9]+/systems/?(\\?.*)?$");
    Matcher systemMatcher = systemPattern.matcher(path);

    Pattern systemStatusPattern = Pattern.compile("v[0-9]+/systems/status/?(\\?.*)?$");
    Matcher systemStatusMatcher = systemStatusPattern.matcher(path);

    Pattern systemReportsPattern = Pattern.compile("v[0-9]+/systems/(.*)/reports/?(\\?.*)?$");
    Matcher systemReportsMatcher = systemReportsPattern.matcher(path);

    Pattern reportsPattern = Pattern.compile("v[0-9]+/reports/?(\\?.*)?$");
    Matcher reportsMatcher = reportsPattern.matcher(path);

    Pattern acksPattern = Pattern.compile("v[0-9]+/acks/?(\\?.*)$");
    Matcher acksMatcher = acksPattern.matcher(path);

    Pattern rulesPattern = Pattern.compile("v[0-9]+/rules/?(\\?.*)$");
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
    } else if (systemStatusMatcher.matches()) {
      response.put("type", Constants.SYSTEMS_STATUS_PATH);
      response.put("index", Integer.toString(path.indexOf("systems")));
    } else {
      response.put("type", "-1");
      response.put("index", "-1");
    }
    return response;
  }

  /**
   * Manipulate the original request path by inserting subset/<id>
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
   * Sort leafIds alphabetically, concat into a string, then sha1
   * to build the subset ID.
   */
  private String createSubsetHash(ArrayList<Integer> leafIds, String branchId) {
    if (leafIds.isEmpty()){
         //We make a single entry subset that  represents a "NULL" subset and will never match any systems
         //We need this because an empty machine ID list causes a 400 HTTP error code
         leafIds.add(-1);   
    }
    Collections.sort(leafIds);
    return branchId + "__" + DigestUtils.sha1Hex(StringUtils.join(leafIds.toArray()));
  }

  /**
   * Return true if the request is coming from another management application
   * such as CFME
   */
  private boolean  isPassThruRequest(String userAgent){
      //TODO we can make this more generic, but there are no plans to support anything
      //other than CFME at this time.
      if (userAgent == null){
        return false;
      }
      return userAgent.contains(Constants.CFME_USER_AGENT_BASE) ? true : false;
  }
}
