'use strict';

angular.module('sat5TelemetryApp')
.controller('SystemsOverview', function (_, $state, $scope, SystemOverviewService, $location) {

  $scope.isOnOverviewTab = function() {
    return SystemOverviewService.tab === 'overview';
  }

  var urlPieces = $location.path().split('/');
  if (urlPieces[1] === 'overview') {
    var category = urlPieces[2];
    SystemOverviewService.switchToOverviewTab({'category': category});
  } else if (urlPieces[1] === 'setup') {
    SystemOverviewService.switchToSetupTab();
  } else {
    SystemOverviewService.switchToOverviewTab();
  }
  $scope.SystemOverviewService = SystemOverviewService;
});
