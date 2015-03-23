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

  var postCreds = function(username, password) {
    var headers = {};
    headers[HTTP_CONST.ACCEPT] = HTTP_CONST.APPLICATION_JSON;
    headers[HTTP_CONST.CONTENT_TYPE] = HTTP_CONST.APPLICATION_JSON;
    var data = {};
    data[CONFIG_KEYS.USERNAME] = username;
    data[CONFIG_KEYS.PASSWORD] = password;
    var promise = $http({
      method: HTTP_CONST.POST,
      url: CONFIG_URLS.CREDENTIALS,
      headers: headers,
      data: data
    });
    return promise;
  };

  var getCreds = function() {
    var headers = {};
    headers[HTTP_CONST.ACCEPT] = HTTP_CONST.APPLICATION_JSON;
    var promise = $http({
      method: HTTP_CONST.GET,
      url: CONFIG_URLS.CREDENTIALS,
      headers: headers
    });
    return promise;
  };

  return {
    postCreds: postCreds,
    getCreds: getCreds
  };
});
