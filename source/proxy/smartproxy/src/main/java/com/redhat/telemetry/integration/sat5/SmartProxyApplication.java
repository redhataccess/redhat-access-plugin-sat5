package com.redhat.telemetry.integration.sat5;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import java.util.HashSet;
import java.util.Set;

import com.redhat.telemetry.integration.sat5.rest.ConfigService;
import com.redhat.telemetry.integration.sat5.rest.ProxyService;

@ApplicationPath("/")
public class SmartProxyApplication extends Application {
   private Set<Object> singletons = new HashSet<Object>();

   public SmartProxyApplication() {
      singletons.add(new ProxyService());
      singletons.add(new ConfigService());
   }

   @Override
   public Set<Object> getSingletons() {
      return singletons;
   }
}
