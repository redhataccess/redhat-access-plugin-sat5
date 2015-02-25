package com.redhat.telemetry.integration.sat5;

import org.apache.http.Header;

public class PortalResponse {

  private String entity;
  private int statusCode;
  private Header[] headers;

  public PortalResponse(int statusCode, String entity, Header[] headers) {
    this.statusCode = statusCode;
    this.entity = entity;
    this.headers = headers;
  }

  public int getStatusCode() {
    return this.statusCode;
  }

  public void setStatusCode(int statusCode) {
    this.statusCode = statusCode;
  }

  public String getEntity() {
    return this.entity;
  }

  public void setEntity(String entity) {
    this.entity = entity;
  }

  public Header[] getHeaders() {
    return this.headers;
  }

  public void setHeaders(Header[] headers) {
    this.headers = headers;
  }
}
