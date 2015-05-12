package com.redhat.telemetry.integration.sat5.util;

import java.io.IOException;
import java.security.Principal;
import java.util.List;
import java.util.Map.Entry;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.Provider;

import org.jboss.resteasy.logging.Logger;

@Provider
public class TrafficLogger implements ContainerRequestFilter, ContainerResponseFilter {

  Logger log = Logger.getLogger(TrafficLogger.class);

  //ContainerRequestFilter
  @Override
  public void filter(ContainerRequestContext requestContext) throws IOException {
    log(requestContext);
  }
  //ContainerResponseFilter
  @Override
  public void filter(
      ContainerRequestContext requestContext, 
      ContainerResponseContext responseContext) throws IOException {

    log(responseContext);
  }

  void log(ContainerRequestContext requestContext) {
    SecurityContext securityContext = requestContext.getSecurityContext();
    String authentication = securityContext.getAuthenticationScheme();
    Principal userPrincipal = securityContext.getUserPrincipal();
    UriInfo uriInfo = requestContext.getUriInfo();
    String method = requestContext.getMethod();
    //List<Object> matchedResources = uriInfo.getMatchedResources();

    log.trace("********************************************************************************");
    log.trace("* HTTP REQUEST *****************************************************************");
    log.trace("********************************************************************************");
    log.trace("uriInfo: " + uriInfo.toString());
    log.trace("method: " + method);
    log.trace("User:  " + userPrincipal.toString());
    log.trace("Auth Scheme:  " + authentication);
  }

  void log(ContainerResponseContext responseContext) {
    MultivaluedMap<String, String> stringHeaders = responseContext.getStringHeaders();
    //Object entity = responseContext.getEntity();
    log.trace("********************************************************************************");
    log.trace("* HTTP RESPONSE ****************************************************************");
    log.trace("********************************************************************************");
    log.trace("Headers:");
    for (Entry<String, List<String>> header : stringHeaders.entrySet()) {
      log.trace(header.getKey() + " : " + header.getValue());
    }
  }
}
