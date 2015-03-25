'use strict';

/**
 * @ngdoc function
 * @name sat5TelemetryApp.controller:SystemTab
 * @description
 * # SystemtabCtrl
 * Controller of the sat5TelemetryApp
 */
angular.module('sat5TelemetryApp')
.controller('SystemTab', function ($scope, Admin) {
  Admin.getSystems()
    .success(function(response) {
      console.log(response);
    })
    .error(function(error) {
      console.log(error);
    });
});
