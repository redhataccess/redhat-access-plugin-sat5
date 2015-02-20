'use strict';

/**
 * @ngdoc service
 * @name sat5TelemetryApp.Util
 * @description
 * # Util
 * Service in the sat5TelemetryApp.
 */
angular.module('sat5TelemetryApp')
.service('Util', function () {

  this.isOnPage = function(page) {
    var response = false;
    if (window.location.pathname.indexOf('/rhn/' + page) === 0) {
      response = true;
    }
    return response;
  };

  this.isOnSystemDetailsPage = function() {
    var response = false;
    if (isOnPage(SYSTEM_DETAILS_PAGE_URLS.OVERVIEW) ||
        isOnPage(SYSTEM_DETAILS_PAGE_URLS.PROPERTIES) ||
        isOnPage(SYSTEM_DETAILS_PAGE_URLS.HARDWARE) ||
        isOnPage(SYSTEM_DETAILS_PAGE_URLS.MIGRATE) ||
        isOnPage(SYSTEM_DETAILS_PAGE_URLS.NOTES) ||
        isOnPage(SYSTEM_DETAILS_PAGE_URLS.INSIGHTS)) {
      response = true;
    }
    return response;
  };

  this.getSidFromUrl = function(url) {
    var sidIndex = url.indexOf('sid=');
    var nextParamIndex = url.indexOf('&', sidIndex);
    var sid = null;
    if (nextParamIndex === -1) {
      sid = url.slice(sidIndex + 4);
    } else {
      sid = url.slice(sidIndex + 4, nextParamIndex);
    }
    return sid;
  };

});
