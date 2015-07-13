package com.redhat.telemetry.integration.sat5.util;

public class Constants {
  public static final String PORTAL_URL = "https://access.redhat.com/r/insights/";

  public static final String SAT5_RPC_API_URL = "http://127.0.0.1/rpc/api";
  public static final String PEOPLE_REPO = 
    "http://access.redhat.com/insights/repo/";
  public static final String ENCRYPTION_PASSWORD = "MgSyMkgt32nhvMzZA6cQGSB4nx7rcEQl186gPjW9qh7sZ3GULOG8W3xUB1EWEsJGhMk6ZaVTNniTQ1kLpBOQ2tbGqc0CSJXrKjrl";

  public final static String API_URL =
    "v1/";
  public final static String SYSTEMS_URL =
    API_URL + "systems/";
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

  //rhel6 software channel
  public static final String CHANNEL_LABEL_RHEL6 = "rh-access-insights-server6-x86_64";
  public static final String CHANNEL_NAME_RHEL6 = "Access Insights for RHEL Server 6 - x86_64";
  public static final String CHANNEL_SUMMARY_RHEL6 = "Access Insights for RHEL Server 6 - x86_64";
  public static final String CHANNEL_ARCH_RHEL6 = "channel-x86_64";
  public static final String CHANNEL_PARENT_RHEL6 = "rhel-x86_64-server-6";
  public static final String REPO_LABEL_RHEL6 = "Red Hat Access Insights RHEL Server 6";
  public static final String REPO_URL_RHEL6 = PEOPLE_REPO + "6/";

  //rhel7 software channel
  public static final String CHANNEL_LABEL_RHEL7 = "rh-access-insights-server7-x86_64";
  public static final String CHANNEL_NAME_RHEL7 = "Access Insights for RHEL Server 7 - x86_64";
  public static final String CHANNEL_SUMMARY_RHEL7 = "Access Insights for RHEL Server 7 - x86_64";
  public static final String CHANNEL_ARCH_RHEL7 = "channel-x86_64";
  public static final String CHANNEL_PARENT_RHEL7 = "rhel-x86_64-server-7";
  public static final String REPO_LABEL_RHEL7 = "Red Hat Access Insights RHEL Server 7";
  public static final String REPO_URL_RHEL7 = PEOPLE_REPO + "7/";

  public static final String PROPERTIES_URL = "/etc/redhat-access/redhat-access-insights.properties";
  public static final String PACKAGE_NAME = "redhat-access-insights";
  public static final String CONFIG_CHANNEL_LABEL = "rh-access-insights-config";
  public static final String CONFIG_CHANNEL_NAME = 
    "Red Hat Access Insights client configuration";
  public static final String CONFIG_CHANNEL_DESCRIPTION = 
    "Red Hat Access Insights client configuration";
  public static final String CONFIG_PATH = 
    "/etc/redhat_access_proactive/redhat_access_proactive.conf";
  public static final String VERSION_RHEL6_SERVER = "6Server";
  public static final String VERSION_RHEL7_SERVER = "7Server";

  //redhat-access-insights.properties 
  public static final String USERNAME_PROPERTY = "username";
  public static final String PASSWORD_PROPERTY = "password";
  public static final String ENABLED_PROPERTY = "enabled";
  public static final String PORTALURL_PROPERTY = "portalurl";
  public static final String DEBUG_PROPERTY = "debug";
  public static final String BOOLEAN_TYPE = "boolean";
  public static final String STRING_TYPE = "string";

  //path types
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
}
