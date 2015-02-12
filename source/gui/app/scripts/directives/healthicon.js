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
      template: '<i class="fa fa-check-circle fa-1-5x text-success" title="No problems"></i>',
      restrict: 'E',
      link: function postLink(scope, element, attrs) {
      }
    };
  });
