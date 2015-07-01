'use strict';

/**
 * @ngdoc directive
 * @name sat5TelemetryApp.directive:adminNav
 * @description
 * # adminNav
 */
angular.module('sat5TelemetryApp')
  .directive('rhaInsightsSat5AdminNav', function () {
    return {
      controller: 'AdminNav',
      templateUrl: 'scripts/views/adminNav.html',
      restrict: 'E',
      link: function postLink(scope, element, attrs) {
      }
    };
  });
