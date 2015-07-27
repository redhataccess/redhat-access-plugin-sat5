package com.redhat.telemetry.integration.sat5.portal;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.cert.CertificateException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.WebApplicationException;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.redhat.telemetry.integration.sat5.json.PortalResponse;
import com.redhat.telemetry.integration.sat5.util.Constants;
import com.redhat.telemetry.integration.sat5.util.Util;

public class InsightsApiClient {
  private Logger LOG = LoggerFactory.getLogger(InsightsApiClient.class);
  private CloseableHttpClient client = null;
  private String portalUrl = Constants.PORTAL_URL;

  public InsightsApiClient() 
      throws NoSuchAlgorithmException, 
             KeyStoreException, 
             CertificateException, 
             IOException, 
             KeyManagementException,
             ConfigurationException {
    
    this.portalUrl = Util.loadPortalUrl();
    client = HttpClients.custom()
            .setSSLSocketFactory(Util.loadSSLKeystore())
            .build();
  }

  /**
   * Close the CloseableHttpClient
   */
  public void close() throws IOException {
    client.close();
  }

  /**
   * Make a request to the portal
   */
  public PortalResponse makeRequest(
      String method, 
      String path,
      Object requestBody,
      String requestContentType,
      String responseContentType) throws ConfigurationException, IOException {

    HttpRequestBase request;
    String fullPath = this.portalUrl + path;

    if (method == Constants.METHOD_GET) {
      request = new HttpGet(fullPath);
    } else if (method == Constants.METHOD_POST) {
        LOG.debug("content type: " + requestContentType);
        request = new HttpPost(fullPath);
        request.addHeader(HttpHeaders.CONTENT_TYPE, requestContentType);
        if (requestBody != null) {
          if (requestBody instanceof HttpEntity) {
            ((HttpPost) request).setEntity((HttpEntity) requestBody);
          } else {
            ((HttpPost) request).setEntity(new ByteArrayEntity((byte[]) requestBody));
          }
        }
    } else if (method == Constants.METHOD_DELETE) {
      request = new HttpDelete(fullPath);
    } else {
      throw new WebApplicationException(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    }

    RequestConfig proxyInfo = Util.loadProxyInfo();
    request.setConfig(proxyInfo);
    LOG.debug("Accept header: " + responseContentType);
    request.addHeader(HttpHeaders.ACCEPT, responseContentType);
    request.addHeader(Constants.SYSTEMID_HEADER, getSatelliteSystemId());
    HttpResponse response = client.execute(request, Util.loadProxyCreds());
    HttpEntity responseEntity = response.getEntity();
    String stringEntity = "";
    if (responseEntity != null) {
      stringEntity = EntityUtils.toString(response.getEntity(), "UTF-8");
    }
    PortalResponse portalResponse = 
      new PortalResponse(
          response.getStatusLine().getStatusCode(), 
          stringEntity,
          response.getAllHeaders());
    request.releaseConnection();
    return portalResponse;
  }

  /**
   * Returns systemid file as a string
   */
  private String getSatelliteSystemId() throws IOException {
    CommandLine cmdLine = CommandLine.parse("/usr/sbin/redhat-access-systemid");
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    DefaultExecutor executor = new DefaultExecutor();
    PumpStreamHandler streamHandler = new PumpStreamHandler(outputStream);
    executor.setStreamHandler(streamHandler);
    executor.execute(cmdLine);
    String systemIdXml = outputStream.toString();
    systemIdXml = systemIdXml.replace(System.getProperty("line.separator"), "");
    return(systemIdXml);
  }


}

