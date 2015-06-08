'use strict';

angular.module('sat5TelemetryApp')
.service('SystemOverviewService', function ($state) {
  this.tab = 'overview';

  this.switchToSetupTab = function() {
    $state.go('rhaInsightsSat5Setup');
    this.tab = 'setup';
    $('#rha-insights-sat5-systems-setup-tab').addClass('active');
    $('#rha-insights-sat5-systems-overview-tab').removeClass('active');
    $('#rha-insights-sat5-systems-rules-tab').removeClass('active');
  };

  this.switchToOverviewTab = function(params) {
    $state.go('app.overview', params);
    this.tab = 'overview';
    $('#rha-insights-sat5-systems-setup-tab').removeClass('active');
    $('#rha-insights-sat5-systems-overview-tab').addClass('active');
    $('#rha-insights-sat5-systems-rules-tab').removeClass('active');
  };
});
