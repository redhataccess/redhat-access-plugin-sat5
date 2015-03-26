'use strict';

/**
 * @ngdoc directive
 * @name sat5TelemetryApp.directive:systemTable
 * @description
 * # systemTab
 */
angular.module('sat5TelemetryApp')
  .directive('rhaInsightsSat5SystemTable', function () {
    return {
      controller: 'SystemTable',
      templateUrl: 'scripts/views/systemTable.html',
      restrict: 'E',
      link: function postLink(scope, element, attrs) {
      }
    };
  });
