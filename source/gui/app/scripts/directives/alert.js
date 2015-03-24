'use strict';

/**
 * @ngdoc directive
 * @name sat5TelemetryApp.directive:alert
 * @description
 * # adminNav
 */
angular.module('sat5TelemetryApp')
.directive('rhaInsightsSat5Alert', function () {
  return {
    controller: 'Alert',
    templateUrl: 'scripts/views/alert.html',
    restrict: 'E',
    link: function postLink(scope, element, attrs) {
    }
  };
});
