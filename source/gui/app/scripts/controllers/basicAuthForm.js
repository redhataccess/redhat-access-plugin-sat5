'use strict';

/**
 * @ngdoc function
 * @name sat5TelemetryApp.controller:BasicAuthForm
 * @description
 * # BasicAuthForm
 * Controller of the sat5TelemetryApp
 */
angular.module('sat5TelemetryApp')
.controller('BasicAuthForm', function (_, $scope, $http, Sat5TelemetryAdmin) {
  $scope.username = '';
  $scope.password = '';
  $scope.loading = true;

  $scope.disableUpdateButton = function() {
    var response = false;
    if (_.isEmpty($scope.username) || 
        _.isEmpty($scope.password) || 
        $scope.loading) {
      response = true;
    }
    return response;
  };

  $scope.postCreds = Sat5TelemetryAdmin.postCreds;

  $scope.doUpdate = function() {
    $scope.loading = true;
    Sat5TelemetryAdmin.postCreds($scope.username, $scope.password)
      .success(function(response) {
        $scope.loading = false;
        $scope.password = '';
      })
      .error(function(error) {
        console.log(error);
        $scope.loading = false;
      });
  };

  Sat5TelemetryAdmin.getCreds()
    .success(function(response) {
      $scope.username = response.username;
      $scope.loading = false;
    })
    .error(function(error) {
      console.log(error);
      $scope.loading = false;
    });
});
