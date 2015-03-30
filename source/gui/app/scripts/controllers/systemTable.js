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
_,
$scope, 
Admin, 
Alert, 
SAT5_ROOT_URLS, 
SYSTEM_DETAILS_PAGE_URLS) {
  $scope.loading = true;
  $scope.filter = '';
  $scope.getSystems = Admin.getSystems;
  $scope.systems = [];
  $scope.allSelected = false;
  $scope.orderBy = 'name';

  $scope.toggleStatusSort = function() {
    if ($scope.orderBy === 'status') {
      $scope.orderBy = '-status';
    } else {
      $scope.orderBy = 'status';
    }
  };

  $scope.toggleNameSort = function() {
    if ($scope.orderBy === 'name') {
      $scope.orderBy = '-name';
    } else {
      $scope.orderBy = 'name';
    }
  };

  $scope.toggleAll = function() {
    _.forEach($scope.systems, function(system) {
      system.selected = $scope.allSelected;
    });
  };

  $scope.selectAll = function() {
    $scope.allSelected = true;
    $scope.toggleAll();
  };

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
      $scope.systems = response;
    })
    .error(function(error) {
      $scope.loading = false;
      Alert.danger('Problem loading systems. Please try again.');
    });
});
