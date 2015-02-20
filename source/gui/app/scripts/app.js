'use strict';

/**
 * @ngdoc overview
 * @name sat5TelemetryApp
 * @description
 * # sat5TelemetryApp
 *
 * Main module of the application.
 */
angular.module('sat5TelemetryApp', [])
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
  'DETAILS_OVERVIEW': 'systems/details/Overview.do'
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
  'SYSTEM_DETAILS': 'systems/details'
})
.constant('_', window._)
.config(function(
SYSTEM_PAGE_URLS, 
ROOT_URLS, 
ADMIN_PAGE_URLS, 
SYSTEM_DETAILS_PAGE_URLS) {

  function isOnPage(page) {
    var response = false;
    if (window.location.pathname.indexOf('/rhn/' + page) === 0) {
      response = true;
    }
    return response;
  }

  function isOnSystemDetailsPage() {
    var response = false;
    if (isOnPage(SYSTEM_DETAILS_PAGE_URLS.OVERVIEW) ||
        isOnPage(SYSTEM_DETAILS_PAGE_URLS.PROPERTIES) ||
        isOnPage(SYSTEM_DETAILS_PAGE_URLS.HARDWARE) ||
        isOnPage(SYSTEM_DETAILS_PAGE_URLS.MIGRATE) ||
        isOnPage(SYSTEM_DETAILS_PAGE_URLS.NOTES) ||
        isOnPage(SYSTEM_DETAILS_PAGE_URLS.INSIGHTS)) {
      response = true;
    }
    return response;
  }

  function getSidFromQueryParams() {
    var sidIndex = window.location.search.indexOf('sid=');
    var nextParamIndex = window.location.search.indexOf('&', sidIndex);
    var sid = null;
    if (nextParamIndex === -1) {
      sid = window.location.search.slice(sidIndex + 4);
    } else {
      sid = window.location.search.slice(sidIndex + 4, nextParamIndex);
    }
    return sid;
  }

  //Check which page we're on then make appropriate changes to dom
  if (isOnPage(SYSTEM_PAGE_URLS.SYSTEM_OVERVIEW)) {
    var HEALTH_TABLE_POS = 1;

    $('<th>Insight</th>').insertAfter(
      $('.table > thead > tr > th:eq(' + HEALTH_TABLE_POS + ')'));
    
    var count = $('.table > tbody > tr').length;
    for(var i = 0; i < count; i++) {
      $('<th><health-icon/></th>').insertAfter(
        $('.table > tbody > tr:eq(' + i + ') > td:eq(' + HEALTH_TABLE_POS + ')'));
    }
  } else if (isOnPage(ROOT_URLS.ADMIN)) {
    $('<li><a href="/rhn/admin/Insights.do">Insights</a></li>').
      insertAfter($('#sidenav > ul > li:last'));
    if (isOnPage(ADMIN_PAGE_URLS.INSIGHTS)) {
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

      $('#spacewalk-content').append('<basic-auth-form/>');
    }
  } else if (isOnSystemDetailsPage()) {
    $('<li><a href="/rhn/systems/details/Insights.do?' + 
      'sid=' + getSidFromQueryParams() + '">Insights</a></li>').insertAfter(
        $('#spacewalk-content > div.spacewalk-content-nav > ul.nav-tabs-pf > li:last')); 
    if (isOnPage(SYSTEM_DETAILS_PAGE_URLS.INSIGHTS)) {
      $('#spacewalk-content > div.spacewalk-content-nav > ul.nav-tabs-pf > li').
        removeClass('active');
      $('#spacewalk-content > div.spacewalk-content-nav > ul.nav-tabs-pf > li:last').
        addClass('active');
    }
  }
})
.run(function($http) {
  $http.get('/insights/rs/telemetry/api/groups').
    success(function(response) {
      console.log('success get api/groups');
    }).
    error(function(error) {
      console.log('error get api/groups');
    });
});

angular.element(document).ready(function() {
  angular.bootstrap(document, ['sat5TelemetryApp']);
}) 
