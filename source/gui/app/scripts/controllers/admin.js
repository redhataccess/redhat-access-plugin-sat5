'use strict';

/**
 * @ngdoc function
 * @name sat5TelemetryApp.controller:Admin
 * @description
 * # AdmincontrollerCtrl
 * Controller of the sat5TelemetryApp
 */
angular.module('sat5TelemetryApp')
.controller('Admin', function ($scope, Admin) {
  $scope.generalTabSelected = Admin.generalTabSelected;
  $scope.systemsTabSelected = Admin.systemsTabSelected;

  $scope.toggleServiceIsEnabled = function() {
    Admin.setServiceIsEnabled(!Admin.getServiceIsEnabled());
  };

});
