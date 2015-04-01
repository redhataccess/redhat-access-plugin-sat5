'use strict';

/**
 * @ngdoc function
 * @name sat5TelemetryApp.controller:GeneralTab
 * @description
 * # BasicAuthForm
 * Controller of the sat5TelemetryApp
 */
angular.module('sat5TelemetryApp')
.controller('GeneralTab', function (
_,
$scope,
$http,
Admin,
Alert,
EVENTS) {

  $scope.softwareChannels = [];
  $scope.enabled = false;
  $scope.username = '';
  $scope.password = '';
  $scope.loading = true;

  function fieldIsDirty(field) {
    var response = false;
    if ($scope.enabled && _.isEmpty(field)) {
      response = true;
    }
    return response;
  }

  $scope.disableUpdateButton = function() {
    var response = false;
    if ($scope.usernameDirty() || $scope.passwordDirty() || $scope.loading) {
      response = true;
    }
    return response;
  };

  $scope.usernameDirty = function() {
    return fieldIsDirty($scope.username);
  };

  $scope.passwordDirty = function() {
    return fieldIsDirty($scope.password);
  };

  $scope.doUpdate = function() {
    $scope.loading = true;
    Admin.postConfig($scope.enabled, $scope.username, $scope.password)
      .success(function(response) {
        $scope.loading = false;
        $scope.password = '';
        Alert.success(
          'Insights configuration was successfully updated.', true);
      })
      .error(function(error) {
        Alert.danger('Error while updating insights config. Please try again or contact support.', false);
        $scope.loading = false;
      });
  };

  $scope.$on(EVENTS.GENERAL_CONFIG_LOADED, function() {
    $scope.username = Admin.getUsername();
    $scope.enabled = Admin.getEnabled();
    $scope.loading = false;
  });
});
