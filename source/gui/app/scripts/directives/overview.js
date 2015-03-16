'use strict';

/**
 * @ngdoc directive
 * @name sat5TelemetryApp.directive:rhaInsightsOverview
 * @description
 * # healthIcon
 */
angular.module('sat5TelemetryApp')
  .directive('rhaInsightsOverview', function () {
    return {
      controller: 'Overview',
      templateUrl: 'scripts/views/overview.html',
      restrict: 'E',
      link: function postLink(scope, element, attrs) {
      }
    };
  });
