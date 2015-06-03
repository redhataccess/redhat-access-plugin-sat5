'use strict';

/**
 * @ngdoc function
 * @name sat5TelemetryApp.controller:AdminnavCtrl
 * @description
 * # AdminnavCtrl
 * Controller of the sat5TelemetryApp
 */
angular.module('sat5TelemetryApp')
  .controller('AdminNav', function ($scope, Admin, ADMIN_TABS, $state, $location) {
    $scope.generalTabSelected = Admin.generalTabSelected;
    $scope.systemsTabSelected = Admin.systemsTabSelected;
    $scope.rulesTabSelected = Admin.rulesTabSelected;
    $scope.selectGeneralTab = function() {
      Admin.setTab(ADMIN_TABS.GENERAL);
      $state.go('rhaInsightsSat5General');
    };

    $scope.selectSystemsTab = function() {
      Admin.setTab(ADMIN_TABS.SYSTEMS);
      $state.go('rhaInsightsSat5Setup');
    };

    $scope.selectRulesTab = function() {
      if (Admin.getEnabled()) {
        $state.go('app.rules', {}, {'reload': true});
      }
      Admin.setTab(ADMIN_TABS.RULES);
    };

    var urlPieces = $location.path().split('/');
    if (urlPieces[1] === 'general') {
      $scope.selectGeneralTab();
    } else if (urlPieces[1] === 'setup') {
      $scope.selectSystemsTab();
    } else if (urlPieces[1] === 'rules') {
      $scope.selectRulesTab();
    } else {
      $scope.selectGeneralTab();
    }
  });
