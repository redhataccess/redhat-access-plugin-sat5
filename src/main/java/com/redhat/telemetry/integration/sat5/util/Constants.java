package com.redhat.telemetry.integration.sat5.util;

public class Constants {
  public static final String PORTAL_URL = "https://access.redhat.com/r/insights/";

  public static final String SAT5_RPC_API_URL = "http://127.0.0.1/rpc/api";

  public final static String API_URL =
    "v1/";
  public final static String SYSTEMS_URL =
    API_URL + "systems/";
  public final static String SYSTEMS_URL_PLAIN =
    "systems/";
  public final static String SUBSETS_URL =
    "subsets/";
  public final static String REPORTS_URL =
    "reports/";
  public final static String BRANCH_URL =
    "branches/";
  public final static String LEAF_URL =
    "leaves/";
  public final static String METHOD_GET = "GET";
  public final static String METHOD_POST = "POST";
  public final static String METHOD_PUT = "PUT";
  public final static String METHOD_DELETE = "DELETE";

  public static final String PROPERTIES_URL = "file:///etc/redhat-access/redhat-access-insights.properties";
  public static final String VERSION_RHEL6_SERVER = "6Server";
  public static final String VERSION_RHEL7_SERVER = "7Server";

  //redhat-access-insights.properties
  public static final String RPM_NAME_PROPERTY = "rpmname";
  public static final String ENABLED_PROPERTY = "enabled";
  public static final String PORTALURL_PROPERTY = "portalurl";
  public static final String DEBUG_PROPERTY = "debug";
  public static final String BOOLEAN_TYPE = "boolean";
  public static final String STRING_TYPE = "string";
  public static final String INSIGHTS_CLIENT_RPM_NAME = "redhat-access-insights";

  //path types
  public static final String PASSTHRU_PATH = "-1";
  public static final String SYSTEMS_PATH = "0";
  public static final String SYSTEM_REPORTS_PATH = "1";
  public static final String REPORTS_PATH = "2";
  public static final String ACKS_PATH = "3";
  public static final String RULES_PATH = "4";
  public static final String UPLOADS_PATH = "5";
  public static final String SYSTEMS_STATUS_PATH = "6";


  //telemetry api json keys
  public static final String MACHINE_ID_KEY = "machine_id";
  public static final String BRANCH_ID_KEY = "branch_id";
  public static final String LEAF_IDS_KEY = "leaf_ids";
  public static final String HASH_KEY = "hash";

  //System types
  public static final String SYSTEM_TYPE_HOST = "host";
  public static final String SYSTEM_TYPE_GUEST = "guest";
  public static final String SYSTEM_TYPE_PHYSICAL = "physical";

  public static final String SYSTEMID_HEADER = "x-rh-systemid";

  //gpg key
  public static final String GPG_KEY_URL = "https://www.redhat.com/security/f21541eb.txt";
  public static final String GPG_KEY_FINGERPRINT = "B08B 659E E86A F623 BC90 E8DB 938A 80CA F215 41EB";
  public static final String GPG_KEY_ID = "F21541EB";

  //satellite config
  public static final String RHN_CONF_LOC = "/etc/rhn/rhn.conf";
  public static final String RHN_CONF_HTTP_PROXY = "server.satellite.http_proxy";
  public static final String RHN_CONF_HTTP_PROXY_USERNAME = "server.satellite.http_proxy_username";
  public static final String RHN_CONF_HTTP_PROXY_PASSWORD = "server.satellite.http_proxy_password";
  public static final String RHN_CONF_HTTP_PROXY_CA_CHAIN = "server.satellite.ca_chain";

  //ssl key store
  public static final String SSL_KEY_STORE_LOC = "/etc/redhat-access/rhai.keystore";
  public static final String SSL_KEY_STORE_PW = "changeit";

  public static final String INTERNAL_SERVER_ERROR_MESSAGE =
    "Internal server error occurred. View rhai.log for more details.";

  public static final String INSTALL_SCHEDULED = "install";
  public static final String UNINSTALL_SCHEDULED = "uninstall";
  public static final String NOT_SCHEDULED = "no";

  public static final String SCHEDULE_CACHE_FILE_URL = "file:///etc/redhat-access/schedule-cache.properties";
}
