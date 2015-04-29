'use strict';
angular.module('sat5TelemetryApp').filter('systemsFilter', function (_, $filter, Admin) {
  return function (input, filter, orderBy, pageStart, pageSize, alpha) {
    input = _.filter(input, function(sys) {
      return _.startsWith(sys.name, alpha);
    });
    input = $filter('filter')(input, filter);
    Admin.updateSystemLength(input.length);
    input = $filter('orderBy')(input, orderBy);
    pageStart = parseInt(pageStart);
    input = input.slice(pageStart);
    input = $filter('limitTo')(input, pageSize);
    return input;
  };
})
