'use strict';

/**
 * @ngdoc function
 * @name sat5TelemetryApp.controller:BasicAuthForm
 * @description
 * # BasicAuthForm
 * Controller of the sat5TelemetryApp
 */
angular.module('sat5TelemetryApp')
.controller('BasicAuthForm', function (_, $scope, $http) {
  $scope.username = '';
  $scope.password = '';

  $scope.disableUpdateButton = function() {
    var response = false;
    if (_.isEmpty($scope.username) || _.isEmpty($scope.password)) {
      response = true;
    }
    return response;
  };

  $scope.doUpdate = function() {
    $http({
      method: 'POST',
      url: '/insights/config/credentials',
      headers: {
        'Accept': 'application/json',
        'Content-Type': 'application/json'
      },
      data: {'username': $scope.username, 'password': $scope.password}
    }).success(function(response) {
      console.log('updated username/password');
    }).error(function(error) {
      console.log(error);
    });
  };

  $http({
      method: 'GET',
      url: '/insights/config/credentials',
      headers: {
        'Accept': 'application/json'
      }
    }).success(function(response) {
      $scope.username = response.username;
    }).error(function(error) {
      console.log(error);
    });
});
