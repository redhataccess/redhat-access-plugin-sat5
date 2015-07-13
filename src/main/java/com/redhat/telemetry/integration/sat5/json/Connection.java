package com.redhat.telemetry.integration.sat5.json;

public class Connection {

  private boolean connected;
  private int statusCode;
  private String responseBody;
  private String detailedMessage;

  public Connection() {
  }

  public Connection(boolean connected, int statusCode, String responseBody, String detailedMessage) {
    this.connected = connected;
    this.statusCode = statusCode;
    this.responseBody = responseBody;
    this.detailedMessage = detailedMessage;
  }

  public void setConnected(boolean connected) {
    this.connected = connected;
  }

  public boolean getConnected() {
    return this.connected;
  }

  public void setStatusCode(int statusCode) {
    this.statusCode = statusCode;
  }

  public int getStatusCode() {
    return this.statusCode;
  }

  public void setResponseBody(String responseBody) {
    this.responseBody = responseBody;
  }

  public String getResponseBody() {
    return this.responseBody;
  }

  public void setDetailedMessage(String detailedMessage) {
    this.detailedMessage = detailedMessage;
  }

  public String getDetailedMessage() {
    return this.detailedMessage;
  }
}
