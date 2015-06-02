'use strict';

angular.module('sat5TelemetryApp')
.service('SystemOverviewService', function ($state) {
  this.hideSystemOverview = false;
  this.hideSystemSetup = true;
  this.hideSystemRules = true;

  this.switchToSetupTab = function() {
    this.hideSystemOverview = true;
    this.hideSystemSetup = false;
    this.hideSystemRules = true;
    $('#rha-insights-sat5-systems-setup-tab').addClass('active');
    $('#rha-insights-sat5-systems-overview-tab').removeClass('active');
    $('#rha-insights-sat5-systems-rules-tab').removeClass('active');
  };

  this.switchToOverviewTab = function() {
    $state.go('app.overview');
    this.hideSystemOverview = false;
    this.hideSystemSetup = true;
    this.hideSystemRules = true;
    $('#rha-insights-sat5-systems-setup-tab').removeClass('active');
    $('#rha-insights-sat5-systems-overview-tab').addClass('active');
    $('#rha-insights-sat5-systems-rules-tab').removeClass('active');
  };

  this.switchToRulesTab = function() {
    $state.go('app.rules');
    this.hideSystemOverview = true;
    this.hideSystemSetup = true;
    this.hideSystemRules = false;
    $('#rha-insights-sat5-systems-setup-tab').removeClass('active');
    $('#rha-insights-sat5-systems-overview-tab').removeClass('active');
    $('#rha-insights-sat5-systems-rules-tab').addClass('active');
  };
});
