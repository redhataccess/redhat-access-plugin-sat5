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
SAT5_ROOT_URLS,
SYSTEM_DETAILS_PAGE_URLS,
TELEMETRY_API_KEYS) {
  $scope.STATUSES = {
    'LOADING': 'loading',
    'GOOD': 'good',
    'BAD': 'bad',
    'UNKNOWN': 'unknown'
  };

  $scope.system = null;

  $scope.system_status = $scope.STATUSES.LOADING;
  $scope.$on(EVENTS.SYSTEMS_POPULATED, function() {
    var leafId = {};
    leafId[TELEMETRY_API_KEYS.REMOTE_LEAF] = $scope.sid.toString();
    $scope.system = _.find(Sat5TelemetrySystems.systems, leafId);
    if (_.isEmpty($scope.system)) {
      $scope.system_status = $scope.STATUSES.UNKNOWN;
    } else if (_.isEmpty($scope.system.reports)) {
      $scope.system_status = $scope.STATUSES.GOOD;
    } else {
      $scope.system_status = $scope.STATUSES.BAD;
    }
  });

  $scope.getTooltip = function() {
    var tooltop = "";
    if ($scope.system_status === $scope.STATUSES.UNKNOWN) {
      tooltip = "System is not registered with Insights.";
    } else if ($scope.system_status === $scope.STATUSES.GOOD) {
      tooltip = "0 Actions";
    } else if ($scope.system_status === $scope.STATUSES.BAD) {
      var text = " Action";
      if ($scope.system.reports.length > 1) {
        text = text + "s";
      }
      tooltip = $scope.system.reports.length + text;
    } else if ($scope.system_status === $scope.STATUSES.LOADING) {
      tooltip = "Loading...";
    }
    return tooltip;
  };

  $scope.getSystemUrl = function() {
    return '/' + SAT5_ROOT_URLS.RHN + '/' + 
      SYSTEM_DETAILS_PAGE_URLS.INSIGHTS + '?sid=' + $scope.system.remote_leaf;
  };
});
