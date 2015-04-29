
angular.module('sat5TelemetryApp')
.constant('EVENTS', {
  'SYSTEMS_POPULATED': 'systems_populated',
  'GENERAL_CONFIG_LOADED': 'general_config_loaded'
})
.constant('SYSTEM_PAGE_URLS', {
  'SYSTEM_OVERVIEW': 'systems/Overview.do',
  'SYSTEMS': 'systems/SystemList.do',
  'PHYSICAL': 'systems/PhysicalList.do',
  'VIRTUAL': 'systems/VirtualSystemsList.do',
  'OUT_OF_DATE': 'systems/OutOfDate.do',
  'REQUIRING_REBOOT': 'systems/RequiringReboot.do',
  'EXTRA_PACKAGES': 'systems/ExtraPackagesSystems.do',
  'UNENTITLED': 'systems/Unentitled.do',
  'UNGROUPED': 'systems/Ungrouped.do',
  'INACTIVE': 'systems/Inactive.do',
  'RECENTLY_REGISTERED': 'systems/Registered.do',
  'PROXY': 'systems/ProxyList.do',
  'DUPLICATE': 'systems/DuplicateIPList.do',
  'CURRENCY': 'systems/SystemCurrency.do',
  'DETAILS_OVERVIEW': 'systems/details/Overview.do',
  'INSIGHTS': 'systems/Insights.do'
})
.constant('ADMIN_PAGE_URLS', {
  'INSIGHTS': 'admin/Insights.do'
})
.constant('SYSTEM_DETAILS_PAGE_URLS', {
  'OVERVIEW': 'systems/details/Overview.do',
  'PROPERTIES': 'systems/details/Edit.do',
  'HARDWARE': 'systems/details/SystemHardware.do',
  'MIGRATE': 'systems/details/SystemMigrate.do',
  'NOTES': 'systems/details/Notes.do',
  'INSIGHTS': 'systems/details/Insights.do'
})
.constant('SAT5_ROOT_URLS', {
  'ADMIN': 'admin',
  'SYSTEMS': 'systems',
  'SYSTEM_DETAILS': 'systems/details',
  'SSM': 'ssm',
  'ACTIVATIONKEYS': 'activationkeys',
  'PROFILES': 'profiles',
  'KICKSTART': 'kickstart',
  'KEYS': 'keys',
  'RHN': 'rhn',
  'PROXY': 'redhat_access'
})
.constant('HTTP_CONST', {
  'ACCEPT': 'accept',
  'APPLICATION_JSON': 'application/json',
  'CONTENT_TYPE': 'Content-Type',
  'GET': 'GET',
  'POST': 'POST'
})
.constant('CONFIG_KEYS', {
  'USERNAME': 'username',
  'PASSWORD': 'password',
  'SATELLITE_USER': 'satellite_user',
  'ENABLED': 'enabled'
})
.constant('TELEMETRY_API_KEYS', {
  'REMOTE_LEAF': 'remote_leaf'
})
.constant('CONFIG_URLS', {
  'GENERAL': '/redhat_access/config/general',
  'SYSTEMS': '/redhat_access/config/systems',
  'STATUS': '/redhat_access/config/status',
  'SOFTWARE_CHANNELS': '/redhat_access/config/channels/software'
})
.constant('TELEMETRY_URLS', {
  'SYSTEMS_SUMMARY': '/redhat_access/rs/telemetry/api/v1/systems?summary=true',
  'API_ROOT': '/rs/telemetry/api/v1'
})
.constant('ADMIN_TABS', {
  'GENERAL': 'general',
  'SYSTEMS': 'systems'
})
.constant('ALERT_TYPES', {
  'DANGER': 'danger',
  'WARNING': 'warning',
  'INFO': 'info',
  'SUCCESS': 'success'
})
.constant('_', window._);
