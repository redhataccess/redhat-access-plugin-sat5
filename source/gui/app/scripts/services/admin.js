'use strict';

/**
 * @ngdoc service
 * @name sat5TelemetryApp.Admin
 * @description
 * # Systems
 * Service in the sat5TelemetryApp.
 */
angular.module('sat5TelemetryApp')
.factory('Admin', function (
$http, 
$rootScope, 
EVENTS, 
CONFIG_URLS, 
HTTP_CONST,
CONFIG_KEYS,
ADMIN_TABS) {

  var _tab = ADMIN_TABS.GENERAL;
  var _enabled = false;
  var _username = "";

  var setTab = function(tab) {
    _tab = tab;
  };

  var getTab = function() {
    return _tab;
  };

  var generalTabSelected = function() {
    return _tab === ADMIN_TABS.GENERAL;
  };

  var systemsTabSelected = function() {
    return _tab === ADMIN_TABS.SYSTEMS;
  };

  var getEnabled = function() {
    return _enabled;
  };

  var setEnabled = function(enabled) {
    _enabled = enabled;
  };

  var getUsername = function() {
    return _username;
  };

  var setUsername = function(username) {
    _username = username;
  };

  /**
   * Scrape the username off the page. 
   * Need this for calls to Sat5 API in the proxy.
   */
  var getSatelliteUser = function() {
    return $('nav.navbar-pf > div.navbar-collapse > ul.navbar-nav > li > a[href="/rhn/account/UserDetails.do"]').text().trim();
  };

  var postConfig = function(enabled, username, password) {
    var headers = {};
    headers[HTTP_CONST.ACCEPT] = HTTP_CONST.APPLICATION_JSON;
    headers[HTTP_CONST.CONTENT_TYPE] = HTTP_CONST.APPLICATION_JSON;
    var params = {};
    params[CONFIG_KEYS.SATELLITE_USER] = getSatelliteUser();
    var data = {};
    data[CONFIG_KEYS.ENABLED] = enabled;
    data[CONFIG_KEYS.USERNAME] = username;
    if (password !== null) {
      data[CONFIG_KEYS.PASSWORD] = password;
    }
    var promise = $http({
      method: HTTP_CONST.POST,
      url: CONFIG_URLS.GENERAL,
      headers: headers,
      data: data,
      params: params
    });
    return promise;
  };

  var getConfig = function() {
    var headers = {};
    headers[HTTP_CONST.ACCEPT] = HTTP_CONST.APPLICATION_JSON;
    var params = {};
    params[CONFIG_KEYS.SATELLITE_USER] = getSatelliteUser();
    var promise = $http({
      method: HTTP_CONST.GET,
      url: CONFIG_URLS.GENERAL,
      headers: headers,
      params: params
    });
    return promise;
  };

  var postSystems = function(systems) {
    var headers = {};
    headers[HTTP_CONST.ACCEPT] = HTTP_CONST.APPLICATION_JSON;
    headers[HTTP_CONST.CONTENT_TYPE] = HTTP_CONST.APPLICATION_JSON;
    var params = {};
    params[CONFIG_KEYS.SATELLITE_USER] = getSatelliteUser();
    var data = systems;
    var promise = $http({
      method: HTTP_CONST.POST,
      url: CONFIG_URLS.SYSTEMS,
      headers: headers,
      params: params,
      data: data
    });
    return promise;
  };

  var getSystemsPromise = function() {
    var headers = {};
    headers[HTTP_CONST.ACCEPT] = HTTP_CONST.APPLICATION_JSON;
    var params = {};
    params[CONFIG_KEYS.SATELLITE_USER] = getSatelliteUser();
    var promise = $http({
      method: HTTP_CONST.GET,
      url: CONFIG_URLS.SYSTEMS,
      headers: headers,
      params: params
    });
    return promise;
  };

  return {
    postConfig: postConfig,
    getConfig: getConfig,
    setTab: setTab,
    getTab: getTab,
    generalTabSelected: generalTabSelected,
    systemsTabSelected: systemsTabSelected,
    getSystemsPromise: getSystemsPromise,
    postSystems: postSystems,
    getEnabled: getEnabled,
    setEnabled: setEnabled,
    setUsername: setUsername,
    getUsername: getUsername
  };
});
