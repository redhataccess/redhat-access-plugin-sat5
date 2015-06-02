'use strict';

angular.module('sat5TelemetryApp')
.controller('SystemsOverview', function ($state, $scope, SystemOverviewService) {

  $state.go('app.overview');
  $scope.SystemOverviewService = SystemOverviewService;
});
