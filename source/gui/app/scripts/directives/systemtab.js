'use strict';

/**
 * @ngdoc directive
 * @name sat5TelemetryApp.directive:systemTab
 * @description
 * # systemTab
 */
angular.module('sat5TelemetryApp')
  .directive('rhaInsightsSat5SystemTab', function () {
    return {
      controller: 'SystemTab',
      templateUrl: 'scripts/views/systemTab.html',
      restrict: 'E',
      link: function postLink(scope, element, attrs) {
      }
    };
  });
