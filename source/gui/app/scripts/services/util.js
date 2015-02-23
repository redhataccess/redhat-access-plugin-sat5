'use strict';

/**
 * @ngdoc service
 * @name sat5TelemetryApp.Util
 * @description
 * # Util
 * Service in the sat5TelemetryApp.
 */
angular.module('sat5TelemetryApp')
.service('Util', function (SYSTEM_DETAILS_PAGE_URLS) {

  this.isOnPage = function(page) {
    var response = false;
    if (window.location.pathname.indexOf('/rhn/' + page) === 0) {
      response = true;
    }
    return response;
  };

  this.isOnSystemDetailsPage = function() {
    var response = false;
    if (this.isOnPage(SYSTEM_DETAILS_PAGE_URLS.OVERVIEW) ||
        this.isOnPage(SYSTEM_DETAILS_PAGE_URLS.PROPERTIES) ||
        this.isOnPage(SYSTEM_DETAILS_PAGE_URLS.HARDWARE) ||
        this.isOnPage(SYSTEM_DETAILS_PAGE_URLS.MIGRATE) ||
        this.isOnPage(SYSTEM_DETAILS_PAGE_URLS.NOTES) ||
        this.isOnPage(SYSTEM_DETAILS_PAGE_URLS.INSIGHTS)) {
      response = true;
    }
    return response;
  };

  this.getSidFromUrl = function(url) {
    var query = new URI(window.location.href).query(true);
    return query.sid;
  };

});
