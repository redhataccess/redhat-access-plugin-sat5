package com.redhat.telemetry.integration.sat5;

import java.io.File;
import java.net.MalformedURLException;

import javax.servlet.ServletContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.CookieParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
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
      @CookieParam("pxt-session-cookie") String user) throws ConfigurationException, MalformedURLException {

    //TODO: only let satellite admin users read this file
    PropertiesConfiguration properties = new PropertiesConfiguration();
    //properties.setFile(new File(context.getRealPath("WEB-INF/insights.properties")));
    properties.load(context.getResourceAsStream("WEB-INF/insights.properties"));
    String username = properties.getString("username");
    String password = properties.getString("password");

    PortalCredentials credentials = new PortalCredentials(username, password);
    return credentials;
  }

  @POST
  @Path("/credentials")
  @Consumes("application/json")
  public Response postCreds(
      PortalCredentials credentials,
      @CookieParam("pxt-session-cookie") String user) throws ConfigurationException, MalformedURLException {

    //TODO: only let satellite admin update properties file
    PropertiesConfiguration properties = new PropertiesConfiguration();
    properties.setFile(new File(context.getRealPath("WEB-INF/insights.properties")));
    properties.setProperty("username", credentials.getUsername());
    properties.setProperty("password", credentials.getPassword());
    properties.save();
    return Response.status(200).build();
  }
}
