'use strict';

angular.module('sat5TelemetryApp')
.controller('RulesTab', function ($scope, Admin) {
  $scope.getEnabled = Admin.getEnabled;
});
