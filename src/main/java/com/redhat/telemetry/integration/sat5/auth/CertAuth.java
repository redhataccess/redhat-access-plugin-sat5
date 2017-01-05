package com.redhat.telemetry.integration.sat5.auth;

import com.redhat.telemetry.integration.sat5.util.Constants;

import java.io.IOException;
import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.zip.ZipInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.Enumeration;

import java.io.BufferedOutputStream;
import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;

public class CertAuth {
  private static Logger LOG = LoggerFactory.getLogger(CertAuth.class);
  private static CertAuth instance = null;

  private static String keyAndCert = "";

  protected CertAuth() {
  }

  public static CertAuth getInstance() {
    if(instance == null) {
      instance = new CertAuth();
    }
    return instance;
  }

	private static void unzip(String path) throws IOException {
		File manifestFile = new File(path);
    ZipFile zipFile = new ZipFile(manifestFile, ZipFile.OPEN_READ);

    Enumeration<? extends ZipEntry> zipFileEntries = zipFile.entries();
		while (zipFileEntries.hasMoreElements()) {
      ZipEntry entry = (ZipEntry) zipFileEntries.nextElement();

      File destFile =
        new File(Constants.MANIFEST_EXTRACTION_LOC, entry.getName());
      destFile.getParentFile().mkdirs();
      destFile.createNewFile();

      BufferedInputStream is =
        new BufferedInputStream(zipFile.getInputStream(entry));
      int currentByte;
      // establish buffer for writing file
      byte data[] = new byte[2048];

      // write the current file to disk
      FileOutputStream fos = new FileOutputStream(destFile);
      BufferedOutputStream dest =
              new BufferedOutputStream(fos, 2048);

      // read and write until last byte is encountered
      while ((currentByte = is.read(data, 0, 2048)) != -1) {
          dest.write(data, 0, currentByte);
      }
      dest.flush();
      dest.close();
      is.close();
		}
	}

	private static String extractCertFromConsumerJSON() throws IOException, ParseException {
		File upstreamConsumerDir =
			new File(Constants.MANIFEST_UPSTREAM_CONSUMER_LOC);
		File consumerJSONFile = upstreamConsumerDir.listFiles()[0];

		JSONParser parser = new JSONParser();
		Object obj = parser.parse(new FileReader(consumerJSONFile));

		JSONObject jsonObject = (JSONObject) obj;

		String key = (String) jsonObject.get("key");
		String cert = (String) jsonObject.get("cert");
    String keyAndCert = cert.concat(key);
    keyAndCert = keyAndCert.replaceAll("\n", "");
    keyAndCert =
      keyAndCert.replace(
          "-----BEGIN CERTIFICATE-----", "-----BEGIN CERTIFICATE-----\n");
    keyAndCert =
      keyAndCert.replace("-----END CERTIFICATE-----", "\n-----END CERTIFICATE-----\n");
    keyAndCert =
      keyAndCert.replace(
          "-----BEGIN RSA PRIVATE KEY-----", "-----BEGIN RSA PRIVATE KEY-----\n");
    keyAndCert =
      keyAndCert.replace(
          "-----END RSA PRIVATE KEY-----", "\n-----END RSA PRIVATE KEY-----");

		return keyAndCert;
	}

  private static void unzipManifest() throws IOException, ParseException {
		unzip(Constants.RHSM_MANIFEST_ZIP);
		unzip(Constants.MANIFEST_CONSUMER_EXPORT_ZIP);
  }

  public void loadCertFromManifest() throws IOException, ParseException {
    unzipManifest();
    keyAndCert = extractCertFromConsumerJSON();
  }

  public String getKeyAndCert() {
    return keyAndCert;
  }
}
