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
.constant('SYSTEM_PAGES', {
  'SYSTEM_OVERVIEW': 'System Overview',
  'SYSTEMS': 'Systems',
  'PHYSICAL': 'Physical Systems',
  'VIRTUAL': 'Virtual Systems',
  'OUT_OF_DATE': 'Out of Date Systems',
  'REQUIRING_REBOOT': 'Systems Requiring Reboot',
  'EXTRA_PACKAGES': 'Systems with Extra Packages',
  'UNENTITLED': 'Unentitled Systems',
  'UNGROUPED': 'Ungrouped Systems',
  'INACTIVE': 'Inactive Systems',
  'RECENTLY_REGISTERED': 'Recently Registered Systems',
  'PROXY': 'Proxy Servers',
  'DUPLICATE': 'Duplicate System Profiles',
  'CURRENCY': 'System Currency Report'
})
.config(function(SYSTEM_PAGES) {
  //Check which page we're on then make appropriate changes to dom
  if ($('.spacewalk-toolbar-h1 > h1')[0].innerText.search(SYSTEM_PAGES.SYSTEM_OVERVIEW) !== -1) {
    var HEALTH_TABLE_POS = 1;

    $('<th>Insight</th>').insertAfter(
      $('.table > thead > tr > th:eq(' + HEALTH_TABLE_POS + ')'));
    
    var count = $('.table > tbody > tr').length;
    for(var i = 0; i < count; i++) {
      $('<th><health-icon/></th>').insertAfter(
        $('.table > tbody > tr:eq(' + i + ') > td:eq(' + HEALTH_TABLE_POS + ')'));
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
