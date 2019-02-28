package com.redhat.telemetry.integration.sat5;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

import com.redhat.telemetry.integration.sat5.rest.ConfigService;
import com.redhat.telemetry.integration.sat5.rest.ProxyService;
import com.redhat.telemetry.integration.sat5.util.PropertiesHandler;
import com.redhat.telemetry.integration.sat5.util.Util;
import com.redhat.telemetry.integration.sat5.auth.CertAuth;

@ApplicationPath("/")
public class SmartProxyApplication extends Application {
   private Set<Object> singletons = new HashSet<Object>();
   private Logger LOG = LoggerFactory.getLogger(SmartProxyApplication.class);

   public SmartProxyApplication() {
      try {
        PropertiesHandler propertiesHandler = new PropertiesHandler();
        boolean debug = propertiesHandler.getDebug();
        Util.setLogLevel(debug);
      } catch (Exception e) {
        LOG.error("Unable to determine debug level at startup.");
      }

      try {
        CertAuth certAuth = CertAuth.getInstance();
        certAuth.loadCertFromManifest();
        LOG.debug("Manifest certificate loaded");
      } catch (Exception e) {
        LOG.error("Unable to load certificate!", e);
      }

      singletons.add(new ProxyService());
      singletons.add(new ConfigService());
   }

   @Override
   public Set<Object> getSingletons() {
      return singletons;
   }
}
