'use strict';

/**
 * @ngdoc directive
 * @name sat5TelemetryApp.directive:admin
 * @description
 * # admin
 */
angular.module('sat5TelemetryApp')
.directive('rhaInsightsSat5Admin', function () {
  return {
    controller: 'Admin',
    templateUrl: 'scripts/views/admin.html',
    restrict: 'E',
    link: function postLink(scope, element, attrs) {
    }
  };
});
