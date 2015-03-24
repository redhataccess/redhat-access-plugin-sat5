package com.redhat.telemetry.integration.sat5;

import java.io.File;
import java.net.MalformedURLException;

import javax.servlet.ServletContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.CookieParam;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.ConfigurationException;

@Path("/config")
public class ConfigService {
  @Context ServletContext context;

  @GET
  @Path("/credentials")
  @Produces("application/json")
  public PortalCredentials getCreds(
      @CookieParam("pxt-session-cookie") String sessionKey,
      @QueryParam("satellite_user") String satelliteUser) 
          throws ConfigurationException, MalformedURLException {

    if (userIsAdmin(sessionKey, satelliteUser)) {
      PropertiesConfiguration properties = new PropertiesConfiguration();
      properties.load(context.getResourceAsStream("WEB-INF/insights.properties"));
      String username = properties.getString("username");
      PortalCredentials creds = new PortalCredentials(username, "");
      return creds;
    } else {
      throw new ForbiddenException("Must be satellite admin.");
    }   
  }

  @POST
  @Path("/credentials")
  @Consumes("application/json")
  public Response postCreds(
      PortalCredentials credentials,
      @CookieParam("pxt-session-cookie") String sessionKey,
      @QueryParam("satellite_user") String satelliteUser) 
          throws ConfigurationException, MalformedURLException {

    if (userIsAdmin(sessionKey, satelliteUser)) {
      PropertiesConfiguration properties = new PropertiesConfiguration();
      properties.setFile(new File(context.getRealPath("WEB-INF/insights.properties")));
      properties.setProperty("username", credentials.getUsername());
      properties.setProperty("password", credentials.getPassword());
      properties.save();
      return Response.status(200).build();
    } else {
      throw new ForbiddenException("Must be satellite admin.");
    }
  }

  private boolean userIsAdmin(String sessionKey, String username) {
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
