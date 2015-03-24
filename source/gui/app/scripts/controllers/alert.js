'use strict';

/**
 * @ngdoc function
 * @name sat5TelemetryApp.controller:Alert
 * @description
 * # AdminnavCtrl
 * Controller of the sat5TelemetryApp
 */
angular.module('sat5TelemetryApp')
.controller('Alert', function ($scope, Alert, ALERT_TYPES) {
  $scope.getMessage = Alert.getMessage;
  $scope.isDanger = function() {
    return Alert.getType() === ALERT_TYPES.DANGER;
  };
  $scope.isSuccess = function() {
    return Alert.getType() === ALERT_TYPES.SUCCESS;
  };
  $scope.isInfo = function() {
    return Alert.getType() === ALERT_TYPES.INFO;
  };
  $scope.isWarning = function() {
    return Alert.getType() === ALERT_TYPES.WARNING;
  };
});
