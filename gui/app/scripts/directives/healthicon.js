'use strict';

/**
 * @ngdoc directive
 * @name sat5TelemetryApp.directive:healthIcon
 * @description
 * # healthIcon
 */
angular.module('sat5TelemetryApp')
  .directive('healthIcon', function () {
    return {
      controller: 'HealthIcon',
      templateUrl: 'scripts/views/healthIcon.html',
      restrict: 'E',
      scope: {
        sid: '='
      },
      link: function postLink(scope, element, attrs) {
      }
    };
  });
