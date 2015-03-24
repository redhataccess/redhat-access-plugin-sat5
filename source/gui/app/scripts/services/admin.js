'use strict';

/**
 * @ngdoc service
 * @name sat5TelemetryApp.Sat5TelemetryAdmin
 * @description
 * # Systems
 * Service in the sat5TelemetryApp.
 */
angular.module('sat5TelemetryApp')
.factory('Sat5TelemetryAdmin', function (
$http, 
$rootScope, 
EVENTS, 
CONFIG_URLS, 
HTTP_CONST,
CONFIG_KEYS) {

  /**
   * Scrape the username off the page. 
   * Need this for calls to Sat5 API in the proxy.
   */
  var getSatelliteUser = function() {
    return $('nav.navbar-pf > div.navbar-collapse > ul.navbar-nav > li > a[href="/rhn/account/UserDetails.do"]').text().trim();
  };

  var postCreds = function(username, password) {
    var headers = {};
    headers[HTTP_CONST.ACCEPT] = HTTP_CONST.APPLICATION_JSON;
    headers[HTTP_CONST.CONTENT_TYPE] = HTTP_CONST.APPLICATION_JSON;
    var params = {};
    params[CONFIG_KEYS.SATELLITE_USER] = getSatelliteUser();
    var data = {};
    data[CONFIG_KEYS.USERNAME] = username;
    data[CONFIG_KEYS.PASSWORD] = password;
    var promise = $http({
      method: HTTP_CONST.POST,
      url: CONFIG_URLS.CREDENTIALS,
      headers: headers,
      data: data,
      params: params
    });
    return promise;
  };

  var getCreds = function() {
    var headers = {};
    headers[HTTP_CONST.ACCEPT] = HTTP_CONST.APPLICATION_JSON;
    var params = {};
    params[CONFIG_KEYS.SATELLITE_USER] = getSatelliteUser();
    var promise = $http({
      method: HTTP_CONST.GET,
      url: CONFIG_URLS.CREDENTIALS,
      headers: headers,
      params: params
    });
    return promise;
  };

  return {
    postCreds: postCreds,
    getCreds: getCreds
  };
});
