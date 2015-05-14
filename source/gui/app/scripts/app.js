'use strict';

/**
 * @ngdoc overview
 * @name sat5TelemetryApp
 * @description
 * # sat5TelemetryApp
 *
 * Main module of the application.
 */
angular.module('sat5TelemetryApp', ['telemetryRoutes', 'telemetryWidgets', 'telemetryConfig', 'ui.indeterminate'])
.config(function($urlRouterProvider, $locationProvider) {
  //$urlRouterProvider.otherwise('/');
  $locationProvider.html5Mode(false);
})
.run(function(
$rootScope,
_,
$state,
CONFIG,
Sat5TelemetrySystems,
Util,
Admin,
SYSTEM_PAGE_URLS, 
EVENTS,
SAT5_ROOT_URLS, 
ADMIN_PAGE_URLS, 
SYSTEM_DETAILS_PAGE_URLS,
TELEMETRY_URLS) {

  CONFIG.API_ROOT = '/' + SAT5_ROOT_URLS.PROXY + TELEMETRY_URLS.API_ROOT + '/';
  CONFIG.authenticate = false;
  CONFIG.preloadData = false;

  var appendToSideNav = function(url, isState, content, hide) {
    var classString = '';
    if (hide) {
      classString = 'class="ng-hide"';
    }
    $('#sidenav > ul').append(
      '<li id="rha-insights-sidenav" ' + classString + '><a href="/' + SAT5_ROOT_URLS.RHN + '/' + url + '">Insights</a></li>');
    //highlight Insights nav>li when selected
    if (Util.isOnPage(url)) {
      var currentSelection = $('#sidenav > ul > li .active')[0];
      if (currentSelection && currentSelection.parentElement && 
          currentSelection.parentElement.parentElement &&
          currentSelection.parentElement.parentElement.tagName === 'LI') {
        currentSelection.parentElement.remove();
      }

      currentSelection = $('#sidenav > ul > li.active')[0];
      if (currentSelection &&
          currentSelection.nextElementSibling &&
          currentSelection.nextElementSibling.firstElementChild &&
          currentSelection.nextElementSibling.firstElementChild.tagName === 'UL') {
        currentSelection.nextElementSibling.remove();
      }

      $('#sidenav > ul > li').removeClass('active');
      $('#sidenav > ul > li:last').addClass('active');

      if (isState) {
        $state.go(content);
      } else {
        $('#spacewalk-content').append(content);
      }
    }
  };

  //Check which page we're on then make appropriate changes to dom
  if (Util.isOnPage(SAT5_ROOT_URLS.SYSTEMS) || 
      Util.isOnPage(SAT5_ROOT_URLS.SSM) || 
      Util.isOnPage(SAT5_ROOT_URLS.ACTIVATIONKEYS) ||
      Util.isOnPage(SAT5_ROOT_URLS.PROFILES) ||
      Util.isOnPage(SAT5_ROOT_URLS.KICKSTART) ||
      Util.isOnPage(SAT5_ROOT_URLS.KEYS)) {
    //Add Insights to side nav
    appendToSideNav(SYSTEM_PAGE_URLS.INSIGHTS, true, 'overview', true);
  }

  if (Util.isOnSystemListPage()) {

    var HEALTH_TABLE_POS = 1;

    $('<th id="rha-insights-system-health-col-head" class="ng-hide">Insights</th>').insertAfter(
      $('.table > thead > tr > th:eq(' + HEALTH_TABLE_POS + ')'));

    var count = $('.table > tbody > tr').length;
    if (Util.thereAreSystemsOnOverviewPage()) {
      for(var i = 0; i < count; i++) {
        var systemUrl = $('.table > tbody > tr:eq(' + i + ') > td:eq(1) > a')[0].href;
        var sid = Util.getSidFromUrl(systemUrl);

        $('<th class="rha-insights-system-health-col ng-hide"><health-icon sid="' + sid + '"/></th>').insertAfter(
          $('.table > tbody > tr:eq(' + i + ') > td:eq(' + HEALTH_TABLE_POS + ')'));
      }
    }
  } else if (Util.isOnSystemDetailsPage()) {
    $('<li id="rha-insights-system-details" class="ng-hide"><a href="/rhn/systems/details/Insights.do?' + 
      'sid=' + Util.getSidFromUrl(window.location.search) + '">Insights</a></li>').insertAfter(
        $('#spacewalk-content > div.spacewalk-content-nav > ul.nav-tabs-pf > li:last')); 
    if (Util.isOnPage(SYSTEM_DETAILS_PAGE_URLS.INSIGHTS)) {
      $('#spacewalk-content > div.spacewalk-content-nav > ul.nav-tabs-pf > li').
        removeClass('active');
      $('#spacewalk-content > div.spacewalk-content-nav > ul.nav-tabs-pf > li:last').
        addClass('active');
      
      $('#spacewalk-content').
        append('<rule-summary machine-id="' + Util.getSidFromUrl(window.location.search) + '" rule="" system="{}"/>');
      $('#spacewalk-content').addClass('main-content insights-main-content');
    }
  } else if (Util.isOnAdminPage()) {
    appendToSideNav(ADMIN_PAGE_URLS.INSIGHTS, false, '<rha-insights-sat5-admin/>', false);
  }

  if (Util.isOnInsightsEnabledPage()) {
    Admin.getConfig()
    .success(function(config) {
      Admin.setEnabled(config.enabled);
      Admin.setUsername(config.username);
      $rootScope.$broadcast(EVENTS.GENERAL_CONFIG_LOADED);
      if (config.enabled) {
        $('#rha-insights-sidenav').removeClass('ng-hide');
        $('#rha-insights-system-details').removeClass('ng-hide');
        $('.rha-insights-system-health-col').removeClass('ng-hide');
        $('#rha-insights-system-health-col-head').removeClass('ng-hide');
        if (Util.isOnSystemListPage() && Util.thereAreSystemsOnOverviewPage()) {
          Sat5TelemetrySystems.populate();
        }
      }
    })
    .error(function(response) {
      console.log('Unable to load Red Hat Access Insights config');
    });
  }
});

angular.element(document).ready(function() {
  angular.bootstrap(document, ['sat5TelemetryApp']);
});
