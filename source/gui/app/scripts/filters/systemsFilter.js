'use strict';
angular.module('sat5TelemetryApp').filter('systemsFilter', function ($filter, Admin) {
  return function (input, filter, orderBy, pageStart, pageSize) {
    input = $filter('filter')(input, filter);
    Admin.updateSystemLength(input.length);
    input = $filter('orderBy')(input, orderBy);
    pageStart = parseInt(pageStart);
    input = input.slice(pageStart);
    input = $filter('limitTo')(input, pageSize);
    return input;
    //ng-repeat='system in filteredSystems = (getSystems() | filter: filter) | orderBy: orderBy | pageStart: getPageStart() | limitTo:getPageSize()', 
  };
})
