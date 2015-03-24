'use strict';

/**
 * @ngdoc function
 * @name sat5TelemetryApp.controller:AdminnavCtrl
 * @description
 * # AdminnavCtrl
 * Controller of the sat5TelemetryApp
 */
angular.module('sat5TelemetryApp')
  .controller('AdminNav', function ($scope, Admin, ADMIN_TABS) {
    $scope.generalTabSelected = Admin.generalTabSelected;
    $scope.systemsTabSelected = Admin.systemsTabSelected;
    $scope.selectGeneralTab = function() {
      Admin.setTab(ADMIN_TABS.GENERAL);
    };

    $scope.selectSystemsTab = function() {
      Admin.setTab(ADMIN_TABS.SYSTEMS);
    };
  });
