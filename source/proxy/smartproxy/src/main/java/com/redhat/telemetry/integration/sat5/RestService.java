package com.redhat.telemetry.integration.sat5;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
 
@Path("/")
public class RestService {

  @GET
  @Path("/systems")
  public Response getSystemList() {
    String result = "System list";
    return Response.status(200).entity(result).build();
  }
}
