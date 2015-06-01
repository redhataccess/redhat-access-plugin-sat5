'use strict';

angular.module('sat5TelemetryApp')
.service('SystemOverviewService', function () {
  this.hideSystemOverview = false;
  this.hideSystemSetup = true;

  this.switchToSetupTab = function() {
    this.hideSystemOverview = true;
    this.hideSystemSetup = false;
    $('#rha-insights-sat5-systems-setup-tab').addClass('active');
    $('#rha-insights-sat5-systems-overview-tab').removeClass('active');
  };

  this.switchToOverviewTab = function() {
    this.hideSystemOverview = false;
    this.hideSystemSetup = true;
    $('#rha-insights-sat5-systems-setup-tab').removeClass('active');
    $('#rha-insights-sat5-systems-overview-tab').addClass('active');
  };
});
