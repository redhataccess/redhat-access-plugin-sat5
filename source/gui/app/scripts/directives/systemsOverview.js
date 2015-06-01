'use strict';

angular.module('sat5TelemetryApp')
  .directive('rhaInsightsSat5SystemsOverview', function () {
    return {
      controller: 'SystemsOverview',
      templateUrl: 'scripts/views/systemsOverview.html',
      restrict: 'A',
      link: function postLink(scope, element, attrs) {
      }
    };
  });
