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
Sat5TelemetrySystems,
Util,
SYSTEM_PAGE_URLS, 
SAT5_ROOT_URLS, 
ADMIN_PAGE_URLS, 
SYSTEM_DETAILS_PAGE_URLS,
TELEMETRY_URLS) {

  CONFIG.API_ROOT = '/' + SAT5_ROOT_URLS.PROXY + TELEMETRY_URLS.API_ROOT + '/';
  CONFIG.authenticate = false;
  CONFIG.preloadData = false;

  function appendToSideNav(url, isState, content) {
    $('#sidenav > ul').append(
      '<li><a href="/' + SAT5_ROOT_URLS.RHN + '/' + url + '">Insights</a></li>');

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
        $('#spacewalk-content').append('<ul><rha-telemetry-overview-side-nav/></ul>');
        $state.go(content);
      } else {
        $('#spacewalk-content').append(content);
      }
    }
  }

  //Check which page we're on then make appropriate changes to dom
  if (Util.isOnPage(SAT5_ROOT_URLS.SYSTEMS) || 
      Util.isOnPage(SAT5_ROOT_URLS.SSM) || 
      Util.isOnPage(SAT5_ROOT_URLS.ACTIVATIONKEYS) ||
      Util.isOnPage(SAT5_ROOT_URLS.PROFILES) ||
      Util.isOnPage(SAT5_ROOT_URLS.KICKSTART) ||
      Util.isOnPage(SAT5_ROOT_URLS.KEYS)) {
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

    Sat5TelemetrySystems.populate();
  } else if (Util.isOnPage(SAT5_ROOT_URLS.ADMIN)) {
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
