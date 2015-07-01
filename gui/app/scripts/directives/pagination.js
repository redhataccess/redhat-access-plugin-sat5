'use strict';

/**
 * @ngdoc directive
 * @name sat5TelemetryApp.directive:pagination
 * @description
 * # healthIcon
 */
angular.module('sat5TelemetryApp')
  .directive('rhaInsightsSat5Pagination', function () {
    return {
      controller: 'Pagination',
      templateUrl: 'scripts/views/pagination.html',
      restrict: 'A'
    };
  });
