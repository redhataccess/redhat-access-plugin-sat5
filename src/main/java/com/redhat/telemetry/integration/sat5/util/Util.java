package com.redhat.telemetry.integration.sat5.util;

import javax.xml.bind.DatatypeConverter;

import org.bouncycastle.openssl.PEMReader;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.StringReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedInputStream;
import java.io.FileReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.net.InetAddress;
import java.security.KeyPair;
import java.security.Security;
import java.security.spec.InvalidKeySpecException;
import java.security.KeyManagementException;
import java.security.UnrecoverableKeyException;
import java.security.KeyFactory;
import java.security.spec.KeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.KeyStoreException;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.CertificateException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.Enumeration;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.SSLContext;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.ssl.SSLContexts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import com.redhat.telemetry.integration.sat5.satellite.SatApi;
import com.redhat.telemetry.integration.sat5.auth.CertAuth;

import ch.qos.logback.classic.Level;

public class Util {
  private static Logger LOG = LoggerFactory.getLogger(Util.class);

  public static String getSatelliteHostname() throws IOException, InterruptedException {
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

    return hostname;
  }

  public static void setLogLevel(boolean debug) {
    if (debug) {
      enableDebugMode();
    } else {
      disableDebugMode();
    }
  }

  public static void disableDebugMode() {
    ch.qos.logback.classic.Logger root = (ch.qos.logback.classic.Logger) LoggerFactory
        .getLogger(Logger.ROOT_LOGGER_NAME);
    root.setLevel(Level.INFO);

    org.apache.log4j.Logger log4jRoot = org.apache.log4j.LogManager.getRootLogger();
    log4jRoot.setLevel(org.apache.log4j.Level.INFO);
  }

  public static void enableDebugMode() {
    ch.qos.logback.classic.Logger root = (ch.qos.logback.classic.Logger) LoggerFactory
        .getLogger(Logger.ROOT_LOGGER_NAME);
    root.setLevel(Level.DEBUG);

    org.apache.log4j.Logger log4jRoot = org.apache.log4j.LogManager.getRootLogger();
    log4jRoot.setLevel(org.apache.log4j.Level.DEBUG);
  }

  public static String getLog(Date currentDate) throws IOException {
    BufferedReader reader = new BufferedReader(new FileReader("/var/log/rhn/rhai.log"));
    try {
      StringBuilder builder = new StringBuilder();
      String line = reader.readLine();
      boolean afterCurrentDate = false;
      while (line != null) {
        try {
          Date lineDate = extractLineDate(line);
          if (!afterCurrentDate && lineDate.after(currentDate)) {
            afterCurrentDate = true;
          }
          if (afterCurrentDate) {
            builder.append(line);
            builder.append("\n");
          }
        } catch (Exception e) {
        }
        line = reader.readLine();
      }
      return builder.toString();
    } finally {
      reader.close();
    }
  };

  private static Date extractLineDate(String line) throws java.text.ParseException {
    Date date = null;
    int firstSpaceIndex = line.indexOf(" ");
    if (firstSpaceIndex != -1) {
      int secondSpaceIndex = line.indexOf(" ", firstSpaceIndex + 1);
      if (secondSpaceIndex != -1) {
        String timestamp = line.substring(0, secondSpaceIndex);
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss,SSS");
        date = format.parse(timestamp);
      }
    }
    return date;
  }

  /**
   * Check if the session is valid for this user
   */
  public static boolean sessionIsValid(String sessionKey, String userID) {

    try {
      /*
       * We don't care what the roles are.
       * The listUserRoles will return without error if  either sessionKey is valid and belongs to
       * userID , or sessionKey belongs to a user with the right permission to list roles. Either
       * case is adequate to determine that sessionKey is a valid key
       */
      Object roles = SatApi.listUserRoles(sessionKey, userID);
      if (roles == null) {
        return false;
      }
      return true;
    } catch (Exception e) {
      return false;
    }

  }

  /**
   * Check if a user is the satellite administrator
   */
  public static boolean userIsAdmin(String sessionKey, String username) {
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

  /**
   * Retrieve the portal_url property from disk
   */
  public static String loadPortalUrl() throws ConfigurationException {
    String response = Constants.PORTAL_URL;
    PropertiesHandler propertiesHandler = new PropertiesHandler();
    String configPortalUrl = propertiesHandler.getPortalUrl();
    if (configPortalUrl != null) {
      if (configPortalUrl.charAt(configPortalUrl.length() - 1) != '/') {
        configPortalUrl = configPortalUrl + "/";
      }
      response = configPortalUrl;
    }
    return response;
  }

  /**
   * Add proxy info to this.requestConfig if proxy is defined in rhn.conf
   */
  public static RequestConfig loadProxyInfo() throws ConfigurationException {
    RequestConfig proxyInfo = null;
    String hostname = getProxyHostname();
    int port = getProxyPort();
    if (hostname != null && !hostname.equals("")) {
      LOG.debug("Satellite is configured to use a proxy. Host: " + hostname + " | Port: " + Integer.toString(port));
      HttpHost proxy = new HttpHost(hostname, port);
      proxyInfo = RequestConfig.custom().setProxy(proxy).build();
    }
    return proxyInfo;
  }

  /**
   * Retrieve hostname as http_proxy value from rhn.conf
   */
  public static String getProxyHostname() throws ConfigurationException {
    PropertiesConfiguration properties = new PropertiesConfiguration();
    properties.load(Constants.RHN_CONF_LOC);
    String proxyHostColonPort = properties.getString(Constants.RHN_CONF_HTTP_PROXY);
    String hostname = "";
    if (proxyHostColonPort != null) {
      if (proxyHostColonPort.contains(":")) {
        Pattern portPattern = Pattern.compile("(.*):([0-9]*)$");
        Matcher portMatcher = portPattern.matcher(proxyHostColonPort);
        if (portMatcher.matches()) {
          hostname = portMatcher.group(1);
        }
      } else {
        hostname = proxyHostColonPort;
      }
    }
    return hostname;
  }

  /**
   * Retrieve port from http_proxy value in rhn.conf
   */
  public static int getProxyPort() throws ConfigurationException {
    PropertiesConfiguration properties = new PropertiesConfiguration();
    properties.load(Constants.RHN_CONF_LOC);
    String proxyHostColonPort = properties.getString(Constants.RHN_CONF_HTTP_PROXY);
    int port = -1;
    if (proxyHostColonPort != null) {
      port = 80;
      if (proxyHostColonPort.contains(":")) {
        Pattern portPattern = Pattern.compile("(.*):([0-9]*)$");
        Matcher portMatcher = portPattern.matcher(proxyHostColonPort);
        if (portMatcher.matches()) {
          port = Integer.parseInt(portMatcher.group(2));
        }
      }
    }
    return port;
  }

  /**
   * Retrieve creds as HttpClientContext from rhn.conf
   */
  public static HttpClientContext loadProxyCreds() throws ConfigurationException {
    HttpClientContext context = HttpClientContext.create();
    PropertiesConfiguration properties = new PropertiesConfiguration();
    properties.load(Constants.RHN_CONF_LOC);
    String proxyUser = properties.getString(Constants.RHN_CONF_HTTP_PROXY_USERNAME);
    String proxyPassword = properties.getString(Constants.RHN_CONF_HTTP_PROXY_PASSWORD);
    if (proxyUser != null && proxyUser != "" && proxyPassword != null && proxyPassword != "") {
      CredentialsProvider credsProvider = new BasicCredentialsProvider();
      credsProvider.setCredentials(new AuthScope(getProxyHostname(), getProxyPort()),
          new UsernamePasswordCredentials(proxyUser, proxyPassword));
      context.setCredentialsProvider(credsProvider);
      LOG.debug("Proxyuser: " + proxyUser);
    }
    return context;
  }

  /**
   * Load rhai.keystore
   */
  public static SSLConnectionSocketFactory loadSSLKeystore() throws NoSuchAlgorithmException, KeyStoreException,
      CertificateException, IOException, UnrecoverableKeyException, InvalidKeySpecException, KeyManagementException {
    LOG.debug("Loading rhai.keystore");

    Security.addProvider(new BouncyCastleProvider());

    //read cert from manifest.zip
    String keyAndCert = CertAuth.getInstance().getKeyAndCert();
    LOG.debug(keyAndCert);
    //end read cert

    //ssl mutual auth
    PEMReader reader = new PEMReader(new StringReader(keyAndCert));
    X509Certificate[] chain = new X509Certificate[1];
    chain[0] = (X509Certificate) reader.readObject();
    KeyPair keyPair = (KeyPair) reader.readObject();

    KeyStore.PrivateKeyEntry pke = new KeyStore.PrivateKeyEntry(keyPair.getPrivate(), chain);

    KeyStore keyStore = KeyStore.getInstance("PKCS12");
    char[] password = "xY28DXepvBeYGHT3gPpc".toCharArray();
    keyStore.load(null, password);
    keyStore.setEntry("insights", pke, new KeyStore.PasswordProtection(password));
    //end ssl mutual auth

    SSLContext sslcontext = SSLContexts.custom().loadKeyMaterial(keyStore, password)
        .loadTrustMaterial(new File(Constants.SSL_KEY_STORE_LOC), Constants.SSL_KEY_STORE_PW.toCharArray(),
            new TrustSelfSignedStrategy())
        .build();
    SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslcontext, new String[] { "TLSv1" }, null,
        SSLConnectionSocketFactory.getDefaultHostnameVerifier());
    return sslsf;
  }

  public static Response buildErrorResponse(Integer code, String body) {
    ResponseBuilder response = Response.status(code);
    response.entity(body);
    return response.build();
  }

  public static String buildJsonMessage(String message) {
    JSONObject obj = new JSONObject();
    obj.put("message", message);
    return obj.toString();
  }
}
