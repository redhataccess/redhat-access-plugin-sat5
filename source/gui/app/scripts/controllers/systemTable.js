'use strict';

/**
 * @ngdoc function
 * @name sat5TelemetryApp.controller:SystemTable
 * @description
 * # Systemtable
 * Controller of the sat5TelemetryApp
 */
angular.module('sat5TelemetryApp')
.controller('SystemTable', function (
$scope, 
Admin, 
Alert, 
SAT5_ROOT_URLS, 
SYSTEM_DETAILS_PAGE_URLS) {
  $scope.loading = true;
  $scope.filter = '';
  $scope.getSystems = Admin.getSystems;

  $scope.getSystemUrl = function(system) {
    return '/' + SAT5_ROOT_URLS.RHN + '/' + 
      SYSTEM_DETAILS_PAGE_URLS.OVERVIEW + '?sid=' + system.id;
  };

  $scope.doApply = function() {
    Admin.postSystems(Admin.getSystems())
      .success(function(response) {
        console.log(response);
      })
      .error(function(error) {
        Alert.danger('Problem updating systems. Please try again.');
      });
  };

  Admin.getSystemsPromise()
    .success(function(response) {
      $scope.loading = false;
      Admin.setSystems(response);
    })
    .error(function(error) {
      $scope.loading = false;
      Alert.danger('Problem loading systems. Please try again.');
    });
});
