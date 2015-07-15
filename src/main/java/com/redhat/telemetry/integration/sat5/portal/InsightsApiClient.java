package com.redhat.telemetry.integration.sat5.portal;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.security.cert.CertificateException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.SSLContext;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.WebApplicationException;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.redhat.telemetry.integration.sat5.json.PortalResponse;
import com.redhat.telemetry.integration.sat5.util.Constants;
import com.redhat.telemetry.integration.sat5.util.PropertiesHandler;

public class InsightsApiClient {
  private Logger LOG = LoggerFactory.getLogger(InsightsApiClient.class);
  private CloseableHttpClient client = null;
  private HttpClientContext context = HttpClientContext.create();
  private String portalUrl = Constants.PORTAL_URL;
  private RequestConfig requestConfig = null;

  public InsightsApiClient() 
      throws NoSuchAlgorithmException, 
             KeyStoreException, 
             CertificateException, 
             IOException, 
             KeyManagementException,
             ConfigurationException {
    
    loadPortalUrl();
    client = HttpClients.custom()
            .setSSLSocketFactory(loadSSLKeystore())
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
    if (method == Constants.METHOD_GET) {
      request = new HttpGet(this.portalUrl + path);
    } else if (method == Constants.METHOD_POST) {
        request = new HttpPost(this.portalUrl + path);
        request.addHeader(HttpHeaders.CONTENT_TYPE, requestContentType);
        if (requestBody != null) {
          if (requestBody instanceof HttpEntity) {
            ((HttpPost) request).setEntity((HttpEntity) requestBody);
          } else {
            ((HttpPost) request).setEntity(new ByteArrayEntity((byte[]) requestBody));
          }
        }
    } else if (method == Constants.METHOD_DELETE) {
      request = new HttpDelete(this.portalUrl + path);
    } else {
      throw new WebApplicationException(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    }

    loadProxyInfo();
    request.setConfig(requestConfig);
    request.addHeader(HttpHeaders.ACCEPT, responseContentType);
    request.addHeader(Constants.SYSTEMID_HEADER, getSatelliteSystemId());
    HttpResponse response = client.execute(request, context);
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


  /**
   * Load rhai.keystore
   */
  private SSLConnectionSocketFactory loadSSLKeystore() 
      throws NoSuchAlgorithmException, 
             KeyStoreException, 
             CertificateException, 
             IOException, 
             KeyManagementException {
    LOG.debug("Loading rhai.keystore");
    SSLContext sslcontext = SSLContexts.custom()
            .loadTrustMaterial(
                new File(Constants.SSL_KEY_STORE_LOC), 
                Constants.SSL_KEY_STORE_PW.toCharArray(),
                new TrustSelfSignedStrategy())
            .build();
    SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
            sslcontext,
            new String[] { "TLSv1" },
            null,
            SSLConnectionSocketFactory.getDefaultHostnameVerifier());
    return sslsf;
  }

  /**
   * Set this.portalUrl to value from properties file
   */
  private void loadPortalUrl() throws ConfigurationException {
    String configPortalUrl = PropertiesHandler.getPortalUrl();
    if (configPortalUrl != null) {
      if (configPortalUrl.charAt(configPortalUrl.length() - 1) != '/') {
        configPortalUrl = configPortalUrl + "/";
      }
      this.portalUrl = configPortalUrl;
    }
  }

  /**
   * Add proxy info to this.requestConfig if proxy is defined in rhn.conf
   */
  private void loadProxyInfo() throws ConfigurationException {
    PropertiesConfiguration properties = new PropertiesConfiguration();
    properties.load(Constants.RHN_CONF_LOC);
    String proxyHostColonPort = properties.getString(Constants.RHN_CONF_HTTP_PROXY);
    if (proxyHostColonPort != null && proxyHostColonPort != "") {
      //pull out the port from the http_proxy property
      int proxyPort = 80;
      String hostname = "";
      if (proxyHostColonPort.contains(":")) {
        Pattern portPattern = Pattern.compile("(.*):([0-9]*)$");
        Matcher portMatcher = portPattern.matcher(proxyHostColonPort);
        if (portMatcher.matches()) {
          hostname = portMatcher.group(1);
          proxyPort = Integer.parseInt(portMatcher.group(2));
        }
      } else {
        hostname = proxyHostColonPort;
      }

      //set the username/password for the proxy
      String proxyUser = properties.getString(Constants.RHN_CONF_HTTP_PROXY_USERNAME);
      String proxyPassword = properties.getString(Constants.RHN_CONF_HTTP_PROXY_PASSWORD);
      if (proxyUser != null && proxyUser != "" && proxyPassword != null && proxyPassword != "") {
        CredentialsProvider credsProvider = new BasicCredentialsProvider();
        credsProvider.setCredentials(
            new AuthScope(hostname, proxyPort),
            new UsernamePasswordCredentials(proxyUser, proxyPassword));
        context.setCredentialsProvider(credsProvider);
        LOG.debug("Proxyuser: " + proxyUser);
      }

      LOG.debug("Satellite is configured to use a proxy. Host: " + hostname + " | Port: " + Integer.toString(proxyPort));
      HttpHost proxy = new HttpHost(hostname, proxyPort);
      this.requestConfig = RequestConfig.custom().setProxy(proxy).build();
    }
  }
}
