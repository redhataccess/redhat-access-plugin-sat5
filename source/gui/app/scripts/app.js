'use strict';

/**
 * @ngdoc overview
 * @name sat5TelemetryApp
 * @description
 * # sat5TelemetryApp
 *
 * Main module of the application.
 */
angular.module('sat5TelemetryApp', ['telemetryWidgets', 'telemetryConfig'])
.constant('EVENTS', {
  'SYSTEMS_POPULATED': 'systems_populated'
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
.constant('ROOT_URLS', {
  'ADMIN': 'admin',
  'SYSTEMS': 'systems',
  'SYSTEM_DETAILS': 'systems/details',
  'SSM': 'ssm',
  'ACTIVATIONKEYS': 'activationkeys',
  'PROFILES': 'profiles',
  'KICKSTART': 'kickstart',
  'KEYS': 'keys',
  'RHN': 'rhn'
})
.constant('_', window._)
.config(function($urlRouterProvider, $locationProvider) {
  //$urlRouterProvider.otherwise('/');
  $locationProvider.html5Mode(false);
})
.run(function(
          _,
          $http,
          $location,
          $state,
          CONFIG,
          Systems,
          Util,
          SYSTEM_PAGE_URLS, 
          ROOT_URLS, 
          ADMIN_PAGE_URLS, 
          SYSTEM_DETAILS_PAGE_URLS) {

  CONFIG.API_ROOT = '/insights/rs/telemetry/api/';
  CONFIG.authenticate = false;
  CONFIG.preloadData = false;

  function appendToSideNav(url, isState, content) {
    $('#sidenav > ul').append(
      '<li><a href="/' + ROOT_URLS.RHN + '/' + url + '">Insights</a></li>');

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

      //***REMOVE BELOW COMMENT
      if (isState) {
        //$('#spacewalk-content').append('<div ui-view="" class="wrapper ng-cloak" style="padding-top: 25px;"></div>');
        $state.go(content);
      } else {
        $('#spacewalk-content').append(content);
      }
    }
  }

  //Check which page we're on then make appropriate changes to dom
  if (Util.isOnPage(ROOT_URLS.SYSTEMS) || 
      Util.isOnPage(ROOT_URLS.SSM) || 
      Util.isOnPage(ROOT_URLS.ACTIVATIONKEYS) ||
      Util.isOnPage(ROOT_URLS.PROFILES) ||
      Util.isOnPage(ROOT_URLS.KICKSTART) ||
      Util.isOnPage(ROOT_URLS.KEYS)) {
    //Add Insights to side nav
    appendToSideNav(SYSTEM_PAGE_URLS.INSIGHTS, true, 'overview');
  }

  if (Util.isOnPage(SYSTEM_PAGE_URLS.SYSTEMS) ||
      Util.isOnPage(SYSTEM_PAGE_URLS.PHYSICAL) ||
      Util.isOnPage(SYSTEM_PAGE_URLS.OUT_OF_DATE) ||
      Util.isOnPage(SYSTEM_PAGE_URLS.REQUIRING_REBOOT) ||
      Util.isOnPage(SYSTEM_PAGE_URLS.UNGROUPED) ||
      Util.isOnPage(SYSTEM_PAGE_URLS.INACTIVE) ||
      Util.isOnPage(SYSTEM_PAGE_URLS.RECENTLY_REGISTERED) ||
      Util.isOnSystemOverviewPage()) {

    var HEALTH_TABLE_POS = 1;

    $('<th>Insight</th>').insertAfter(
      $('.table > thead > tr > th:eq(' + HEALTH_TABLE_POS + ')'));

    var count = $('.table > tbody > tr').length;
    for(var i = 0; i < count; i++) {
      var systemUrl = $('.table > tbody > tr:eq(' + i + ') > td:eq(1) > a')[0].href;
      var sid = Util.getSidFromUrl(systemUrl);

      $('<th><health-icon sid="' + sid + '"/></th>').insertAfter(
        $('.table > tbody > tr:eq(' + i + ') > td:eq(' + HEALTH_TABLE_POS + ')'));
    }

    Systems.populate();
  } else if (Util.isOnPage(ROOT_URLS.ADMIN)) {
    appendToSideNav(ADMIN_PAGE_URLS.INSIGHTS, false, '<basic-auth-form/>');
  } else if (Util.isOnSystemDetailsPage()) {
    $('<li><a href="/rhn/systems/details/Insights.do?' + 
      'sid=' + Util.getSidFromUrl(window.location.search) + '">Insights</a></li>').insertAfter(
        $('#spacewalk-content > div.spacewalk-content-nav > ul.nav-tabs-pf > li:last')); 
    if (Util.isOnPage(SYSTEM_DETAILS_PAGE_URLS.INSIGHTS)) {
      $('#spacewalk-content > div.spacewalk-content-nav > ul.nav-tabs-pf > li').
        removeClass('active');
      $('#spacewalk-content > div.spacewalk-content-nav > ul.nav-tabs-pf > li:last').
        addClass('active');
      
      $('#spacewalk-content').
        append('<error-info-summary machine-id="' + Util.getSidFromUrl(window.location.search) + '" error-info=""/>');
    }
  }
});

angular.element(document).ready(function() {
  angular.bootstrap(document, ['sat5TelemetryApp']);
});
