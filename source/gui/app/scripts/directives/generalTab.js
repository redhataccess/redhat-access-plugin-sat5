'use strict';

/**
 * @ngdoc directive
 * @name sat5TelemetryApp.directive:generalTab
 * @description
 * # healthIcon
 */
angular.module('sat5TelemetryApp')
.directive('rhaInsightsSat5GeneralTab', function () {
  return {
    controller: 'GeneralTab',
    templateUrl: 'scripts/views/generalTab.html',
    restrict: 'EA',
    link: function postLink(scope, element, attrs) {
    }
  };
});
