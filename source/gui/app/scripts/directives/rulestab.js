'use strict';

angular.module('sat5TelemetryApp')
  .directive('rhaInsightsSat5RulesTab', function () {
    return {
      controller: 'RulesTab',
      templateUrl: 'scripts/views/rulesTab.html',
      restrict: 'A',
      link: function postLink(scope, element, attrs) {
      }
    };
  });
