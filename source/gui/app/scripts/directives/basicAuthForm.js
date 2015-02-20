'use strict';

/**
 * @ngdoc directive
 * @name sat5TelemetryApp.directive:basicAuthForm
 * @description
 * # healthIcon
 */
angular.module('sat5TelemetryApp')
  .directive('basicAuthForm', function () {
    return {
      controller: 'BasicAuthForm',
      templateUrl: 'scripts/views/basicAuthForm.html',
      restrict: 'E',
      link: function postLink(scope, element, attrs) {
      }
    };
  });
