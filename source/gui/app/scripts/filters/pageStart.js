'use strict';
angular.module('sat5TelemetryApp').filter('pageStart', function () {
  return function (input, start) {
    start = parseInt(start);
    return input.slice(start);
  };
})
