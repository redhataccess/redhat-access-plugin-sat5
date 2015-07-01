'use strict';

/**
 * @ngdoc directive
 * @name sat5TelemetryApp.directive:rhaInsightsSystemDetails
 * @description
 * # healthIcon
 */
angular.module('sat5TelemetryApp')
  .directive('rhaInsightsSystemDetails', function () {
    return {
      controller: 'SystemDetails',
      templateUrl: 'scripts/views/systemDetails.html',
      restrict: 'E',
      link: function postLink(scope, element, attrs) {
      }
    };
  });
