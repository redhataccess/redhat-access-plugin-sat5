(function() {

//functions shared between insights.js and insights.app.js
var RHA_INSIGHTS = {
  'UTILS': {}
};

RHA_INSIGHTS.UTILS.isOnInsightsEnabledPage = function () {
  var response = false;
  if (RHA_INSIGHTS.UTILS.isOnSystemListPage() || 
      RHA_INSIGHTS.UTILS.isOnSystemDetailsPage() || 
      RHA_INSIGHTS.UTILS.isOnSystemOverviewPage() ||
      RHA_INSIGHTS.UTILS.isOnAdminPage() ||
      RHA_INSIGHTS.UTILS.isOnInsightsOverviewPage() ||
      RHA_INSIGHTS.UTILS.isOnSystemTabPage() ||
      RHA_INSIGHTS.UTILS.isOnHelpPage()) {
    response = true;
  }
  return response;
};

RHA_INSIGHTS.UTILS.isOnPage = function(page) {
  var response = false;
  if (window.location.pathname.indexOf('/' + SAT5_ROOT_URLS.RHN + '/' + page) === 0) {
    response = true;
  }
  return response;
};

RHA_INSIGHTS.UTILS.isOnAdminPage = function() {
  return RHA_INSIGHTS.UTILS.isOnPage(SAT5_ROOT_URLS.ADMIN);
};

RHA_INSIGHTS.UTILS.isOnSystemTabPage = function() {
  var response = false;
  if (RHA_INSIGHTS.UTILS.isOnPage(SAT5_ROOT_URLS.SYSTEMS) || 
      RHA_INSIGHTS.UTILS.isOnPage(SAT5_ROOT_URLS.SSM) || 
      RHA_INSIGHTS.UTILS.isOnPage(SAT5_ROOT_URLS.ACTIVATIONKEYS) ||
      RHA_INSIGHTS.UTILS.isOnPage(SAT5_ROOT_URLS.PROFILES) ||
      RHA_INSIGHTS.UTILS.isOnPage(SAT5_ROOT_URLS.KICKSTART) ||
      RHA_INSIGHTS.UTILS.isOnPage(SAT5_ROOT_URLS.KEYS)) {
    response = true;
  } 
  return response;
};

RHA_INSIGHTS.UTILS.isOnSystemDetailsPage = function() {
  var response = false;
  if (RHA_INSIGHTS.UTILS.isOnPage(SYSTEM_DETAILS_PAGE_URLS.OVERVIEW) ||
      RHA_INSIGHTS.UTILS.isOnPage(SYSTEM_DETAILS_PAGE_URLS.PROPERTIES) ||
      RHA_INSIGHTS.UTILS.isOnPage(SYSTEM_DETAILS_PAGE_URLS.HARDWARE) ||
      RHA_INSIGHTS.UTILS.isOnPage(SYSTEM_DETAILS_PAGE_URLS.MIGRATE) ||
      RHA_INSIGHTS.UTILS.isOnPage(SYSTEM_DETAILS_PAGE_URLS.NOTES) ||
      RHA_INSIGHTS.UTILS.isOnPage(SYSTEM_DETAILS_PAGE_URLS.CUSTOM_INFO) ||
      RHA_INSIGHTS.UTILS.isOnPage(SYSTEM_DETAILS_PAGE_URLS.INSIGHTS)) {
    response = true;
  }
  return response;
};

RHA_INSIGHTS.UTILS.isOnInsightsOverviewPage = function() {
  var response = false;
  if (RHA_INSIGHTS.UTILS.isOnPage(SYSTEM_PAGE_URLS.INSIGHTS)) {
    response = true;
  }
  return response;
};

RHA_INSIGHTS.UTILS.isOnSystemOverviewPage = function() {
  var response = false;
  var showGroups = RHA_INSIGHTS.UTILS.showGroups();

  if (RHA_INSIGHTS.UTILS.isOnPage(SYSTEM_PAGE_URLS.SYSTEM_OVERVIEW) &&
      !showGroups) {
    response = true;
  }
  return response;
};

RHA_INSIGHTS.UTILS.showGroups = function() {
  var query = window.location.search;
  var showGroupsIndex = query.indexOf('showgroups');
  var showGroups = false;
  if (showGroupsIndex !== -1) {
    var value = '';
    value = query.substring(showGroupsIndex, 16); //16 is length of showgroups=false
    if (value.indexOf('true') > -1) {
      showGroups = true;
    } else {
      showGroups = false;
    }
  }
  return showGroups;
};

RHA_INSIGHTS.UTILS.isOnSystemListPage = function() {
  if ((RHA_INSIGHTS.UTILS.isOnPage(SYSTEM_PAGE_URLS.SYSTEMS) ||
      RHA_INSIGHTS.UTILS.isOnPage(SYSTEM_PAGE_URLS.PHYSICAL) ||
      RHA_INSIGHTS.UTILS.isOnPage(SYSTEM_PAGE_URLS.OUT_OF_DATE) ||
      RHA_INSIGHTS.UTILS.isOnPage(SYSTEM_PAGE_URLS.REQUIRING_REBOOT) ||
      RHA_INSIGHTS.UTILS.isOnPage(SYSTEM_PAGE_URLS.UNGROUPED) ||
      RHA_INSIGHTS.UTILS.isOnPage(SYSTEM_PAGE_URLS.INACTIVE) ||
      RHA_INSIGHTS.UTILS.isOnSystemOverviewPage()) &&
      !RHA_INSIGHTS.UTILS.showGroups()) {
    return true;
  } else {
    return false;
  }
};

RHA_INSIGHTS.UTILS.isOnHelpPage = function () {
  if (RHA_INSIGHTS.UTILS.isOnPage(HELP_PAGE_URLS.INDEX)) {
    return true;
  } else {
    return false;
  }
};

var SAT5_ROOT_URLS = {
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
};

var HELP_PAGE_URLS = {
  'INDEX': 'help/index.do'
};


var SYSTEM_DETAILS_PAGE_URLS = {
  'OVERVIEW': 'systems/details/Overview.do',
  'PROPERTIES': 'systems/details/Edit.do',
  'HARDWARE': 'systems/details/SystemHardware.do',
  'MIGRATE': 'systems/details/SystemMigrate.do',
  'NOTES': 'systems/details/Notes.do',
  'CUSTOM_INFO': 'systems/details/ListCustomData.do',
  'INSIGHTS': 'systems/details/Insights.do'
};

var SYSTEM_PAGE_URLS = {
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
  'INSIGHTS': 'systems/Insights.do',
  'SOFTWARE_CRASHES_OVERVIEW': 'systems/SoftwareCrashesOverview.do'
};

window.RHA_INSIGHTS = RHA_INSIGHTS;
window.SYSTEM_PAGE_URLS = SYSTEM_PAGE_URLS;
window.SAT5_ROOT_URLS = SAT5_ROOT_URLS;
window.SYSTEM_DETAILS_PAGE_URLS = SYSTEM_DETAILS_PAGE_URLS;

if (RHA_INSIGHTS.UTILS.isOnInsightsEnabledPage()) {
  $.get('/redhat_access/config/general')
    .done(function(insightsConfig) {
      window.RHA_INSIGHTS.config = insightsConfig;

      if (insightsConfig.enabled || RHA_INSIGHTS.UTILS.isOnAdminPage()) {
        $.ajax({
          dataType: 'script',
          cache: true,
          url: '/javascript/insights.app.js'
        }).done(function(script, status) {
          angular.bootstrap(document, ['sat5TelemetryApp']);
        });
      }
    })
    .fail(function(response) {
      console.log('Unable to GET Red Hat Access Insights config.');
    });
}
})();

