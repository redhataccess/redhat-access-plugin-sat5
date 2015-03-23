'use strict';

/**
 * @ngdoc function
 * @name sat5TelemetryApp.controller:HealthIcon
 * @description
 * # HealthIcon
 * Controller of the sat5TelemetryApp
 */
angular.module('sat5TelemetryApp')
.controller('HealthIcon', function (
_, 
$scope, 
EVENTS, 
Sat5TelemetrySystems,
TELEMETRY_API_KEYS) {
  $scope.STATUSES = {
    'LOADING': 'loading',
    'GOOD': 'good',
    'BAD': 'bad',
    'UNKNOWN': 'unknown'
  };

  $scope.system_status = $scope.STATUSES.LOADING;
  $scope.$on(EVENTS.SYSTEMS_POPULATED, function() {
    var leafId = {};
    leafId[TELEMETRY_API_KEYS.REMOTE_LEAF] = $scope.sid.toString();
    var sys = _.find(Sat5TelemetrySystems.systems, leafId);
    if (_.isEmpty(sys)) {
      $scope.system_status = $scope.STATUSES.UNKNOWN;
    } else if (_.isEmpty(sys.reports)) {
      $scope.system_status = $scope.STATUSES.GOOD;
    } else {
      $scope.system_status = $scope.STATUSES.BAD;
    }
  });
});
