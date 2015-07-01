'use strict';

/**
 * @ngdoc service
 * @name sat5TelemetryApp.Util
 * @description
 * # Util
 * Service in the sat5TelemetryApp.
 */
angular.module('sat5TelemetryApp')
.service('Util', function (SYSTEM_DETAILS_PAGE_URLS, SYSTEM_PAGE_URLS, SAT5_ROOT_URLS) {

  this.getSidFromUrl = function(url) {
    var query = new URI(url).query(true);
    return query.sid;
  };

  this.thereAreSystemsOnOverviewPage = function() {
    var response = true;
    if ($('.table > tbody > tr > td > div').text() === 'No systems.') {
      response = false;
    }
    return response;
  };

});
