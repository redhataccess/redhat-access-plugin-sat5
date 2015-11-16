package com.redhat.telemetry.integration.sat5.portal;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;

import org.apache.commons.configuration.ConfigurationException;
import org.jboss.resteasy.spi.InternalServerErrorException;
import org.jboss.resteasy.spi.NotFoundException;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.redhat.telemetry.integration.sat5.json.PortalResponse;
import com.redhat.telemetry.integration.sat5.util.Constants;
import com.redhat.telemetry.integration.sat5.util.Util;

public class InsightsApiUtils {

  private static Logger LOG = LoggerFactory.getLogger(InsightsApiUtils.class);

  public static String leafIdToMachineId(String leafId) 
    throws JSONException, 
           IOException, 
           ConfigurationException,
           InterruptedException,
           NoSuchAlgorithmException,
           KeyStoreException,
           CertificateException,
           KeyManagementException {

    LOG.debug("leafId: " + leafId);
    String branchId = Util.getSatelliteHostname();
    InsightsApiClient client = new InsightsApiClient();
    PortalResponse getIdResponse = client.makeRequest(
      Constants.METHOD_GET,
      Constants.API_URL + Constants.BRANCH_URL + branchId + "/" + Constants.LEAF_URL + leafId,
      null,
      null,
      MediaType.APPLICATION_JSON);

    String machineId = null;
    if (getIdResponse.getStatusCode() == HttpServletResponse.SC_OK) {
      JSONObject responseJson = new JSONObject(getIdResponse.getEntity());
      machineId = (String) responseJson.get(Constants.MACHINE_ID_KEY);
    } else if (getIdResponse.getStatusCode() == HttpServletResponse.SC_NOT_FOUND) {
      throw new NotFoundException(
          "Machine ID not found. Verify the system has been registered with " + 
          "'redhat-access-insights --register'");
    } else {
      throw new InternalServerErrorException(
          "Unable to retrieve Machine ID from Red Hat Insights API.");
    }

    if (machineId == null || machineId.equals("")) {
      throw new NotFoundException(
          "Machine ID not found. Verify the system has been registered with " + 
          "'redhat-access-insights --register'");
    }
    return machineId;
  }
}
