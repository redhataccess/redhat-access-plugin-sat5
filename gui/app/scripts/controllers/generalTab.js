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
  $scope.connectionStatus = 'unknown';
  $scope.loading = true;
  $scope.log = '';
  $scope.debug = false;
  $scope.initialDebug = false;
  $scope.initialEnabled = false;

  function fieldIsDirty(field) {
    var response = false;
    if ($scope.enabled && _.isEmpty(field)) {
      response = true;
    }
    return response;
  }

  $scope.disableUpdateButton = function() {
    var response = false;
    if ($scope.loading || ($scope.initialDebug === $scope.debug && $scope.initialEnabled === $scope.enabled)) {
      response = true;
    }
    return response;
  };

  $scope.doUpdate = function() {
    $scope.loading = true;
    Admin.postConfig($scope.enabled, $scope.debug)
      .success(function(response) {
        $scope.loading = false;
        $scope.initialDebug = $scope.debug;
        $scope.initialEnabled = $scope.enabled;
        Admin.setEnabled($scope.enabled);
        Alert.success(
          'Insights configuration was successfully updated.', true);
      })
      .error(function(error) {
        Alert.danger('Error while updating insights config. Please try again or contact support.', false);
        $scope.loading = false;
      });
  };

  $scope.setValues = function() {
    $scope.enabled = Admin.getEnabled();
    $scope.debug = Admin.getDebug();
    $scope.initialDebug = $scope.debug;
    $scope.initialEnabled = $scope.enabled;
    $scope.loading = false;
  };

  $scope.getLog = function(timestamp) {
    Admin.getLog(timestamp)
      .success(function(log) {
        $scope.log = log;
      })
      .error(function(error) {
        console.log(error);
        Alert.danger('Unable to retrieve rhai.log.');
      });
  };

  $scope.doTestConnection = function() {
    $scope.connectionStatus = 'loading';
    Admin.testConnection()
      .success(function(response) {
        if (response.connected) {
          $scope.connectionStatus = 'success';
        } else {
          $scope.connectionStatus = 'fail';
          Alert.danger('Unable to connect to Red Hat Servers. Please view the Test Connection Log below for more info.');
          $scope.getLog(response.timestamp);
        }
      })
      .error(function(error) {
        $scope.connectionStatus = 'unknown';
        Alert.danger('Unable to test connection to Red Hat Access Insights API.');
      });
  };

  $scope.getLoadingTooltip = function() {
    return 'Trying to connect to Red Hat Access Insights API...';
  };

  $scope.getSuccessTooltip = function() {
    return 'Connection to Red Hat Access Insights API was successful.';
  };

  $scope.getUnknownTooltip = function() {
    return 'Press the button to test connection between Satellite and Red Hat Access Insights API.';
  };

  $scope.getFailTooltip = function() {
    return 'Unable to connect to Red Hat Access Insights API.';
  };

  $scope.onAceLoad = function(editor) {
    editor.$blockScrolling = Infinity;
  };

  if (Admin.getConfigLoaded()) {
    $scope.setValues();
  } else {
    $scope.$on(EVENTS.GENERAL_CONFIG_LOADED, function() {
      $scope.setValues();
    });
  }
});
