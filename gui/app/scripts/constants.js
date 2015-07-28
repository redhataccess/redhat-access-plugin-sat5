
angular.module('sat5TelemetryApp')
.constant('EVENTS', {
  'SYSTEMS_POPULATED': 'systems_populated',
  'GENERAL_CONFIG_LOADED': 'general_config_loaded'
})
.constant('ADMIN_PAGE_URLS', {
  'INSIGHTS': 'admin/Insights.do'
})
.constant('HTTP_CONST', {
  'ACCEPT': 'accept',
  'APPLICATION_JSON': 'application/json',
  'TEXT_PLAIN': 'text/plain',
  'CONTENT_TYPE': 'Content-Type',
  'GET': 'GET',
  'POST': 'POST'
})
.constant('CONFIG_KEYS', {
  'USERNAME': 'username',
  'PASSWORD': 'password',
  'SATELLITE_USER': 'satellite_user',
  'ENABLED': 'enabled',
  'DEBUG': 'debug',
  'SYSTEMS': 'systems',
  'TIMESTAMP': 'timestamp'
})
.constant('TELEMETRY_API_KEYS', {
  'REMOTE_LEAF': 'remote_leaf'
})
.constant('CONFIG_URLS', {
  'GENERAL': '/redhat_access/config/general',
  'SYSTEMS': '/redhat_access/config/systems',
  'STATUS': '/redhat_access/config/status',
  'SOFTWARE_CHANNELS': '/redhat_access/config/channels/software',
  'CONNECTION': '/redhat_access/config/connection',
  'LOG': '/redhat_access/config/log'
})
.constant('TELEMETRY_URLS', {
  'SYSTEMS_SUMMARY': '/redhat_access/r/insights/v1/systems?summary=true',
  'API_ROOT': '/r/insights/v1'
})
.constant('ADMIN_TABS', {
  'GENERAL': 'general',
  'SYSTEMS': 'systems',
  'RULES': 'rules'
})
.constant('ALERT_TYPES', {
  'DANGER': 'danger',
  'WARNING': 'warning',
  'INFO': 'info',
  'SUCCESS': 'success'
})
.constant('RPM_SCHEDULE_CONST', {
  'INSTALL': 'install',
  'UNINSTALL': 'uninstall',
  'NOT_SCHEDULED': 'no'
})
.constant('_', window._)
.constant('RHA_INSIGHTS', window.RHA_INSIGHTS)
.constant('SYSTEM_DETAILS_PAGE_URLS', window.SYSTEM_DETAILS_PAGE_URLS)
.constant('SAT5_ROOT_URLS', window.SAT5_ROOT_URLS)
.constant('SYSTEM_PAGE_URLS', window.SYSTEM_PAGE_URLS);
