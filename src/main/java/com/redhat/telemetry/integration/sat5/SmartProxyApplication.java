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

@ApplicationPath("/")
public class SmartProxyApplication extends Application {
   private Set<Object> singletons = new HashSet<Object>();
   private Logger LOG = LoggerFactory.getLogger(SmartProxyApplication.class);

   public SmartProxyApplication() {
      try {
        boolean debug = PropertiesHandler.getDebug();
        Util.setLogLevel(debug);
      } catch (Exception e) {
        LOG.error("Unable to determine debug level at startup.");
      }

      singletons.add(new ProxyService());
      singletons.add(new ConfigService());
   }

   @Override
   public Set<Object> getSingletons() {
      return singletons;
   }
}
