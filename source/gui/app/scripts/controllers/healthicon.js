'use strict';

/**
 * @ngdoc function
 * @name sat5TelemetryApp.controller:HealthIcon
 * @description
 * # HealthIcon
 * Controller of the sat5TelemetryApp
 */
angular.module('sat5TelemetryApp')
  .controller('HealthIcon', function (_, $scope, EVENTS, Systems) {
    $scope.system_status = 'loading';
    $scope.$on(EVENTS.SYSTEMS_POPULATED, function() {
      var sys = _.find(Systems.systems, {'remote_leaf': $scope.sid.toString()});
      if (!_.isEmpty(sys)) {
        $scope.system_status = 'good';
      } else {
        $scope.system_status = 'unknown';
      }
    });
  });
