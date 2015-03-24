'use strict';

/**
 * @ngdoc service
 * @name sat5TelemetryApp.Alert
 * @description
 * # Systems
 * Service in the sat5TelemetryApp.
 */
angular.module('sat5TelemetryApp')
.factory('Alert', function (
ALERT_TYPES,
$timeout) {

  var _type = null;
  var _message = null;

  var _showAlert = function(type, message) {
    _message = message;
    type = type;
  };

  var info = function(message, timeout) {
    _type = ALERT_TYPES.INFO;
    _message = message;

    if (timeout) {
      $timeout(function() {
        clear();
      }, 15000);
    }
  };

  var warning = function(message, timeout) {
    _type = ALERT_TYPES.WARNING;
    _message = message;

    if (timeout) {
      $timeout(function() {
        clear();
      }, 15000);
    }
  };

  var danger = function(message, timeout) {
    _type = ALERT_TYPES.DANGER;
    _message = message;

    if (timeout) {
      $timeout(function() {
        clear();
      }, 15000);
    }
  };

  var success = function(message, timeout) {
    _type = ALERT_TYPES.SUCCESS;
    _message = message;

    if (timeout) {
      $timeout(function() {
        clear();
      }, 15000);
    }
  };

  var getType = function() {
    return _type;
  };

  var getMessage = function() {
    return _message;
  };

  var clear = function() {
    _message = null;
    _type = null;
  };

  return {
    info: info,
    success: success,
    danger: danger,
    warning: warning,
    getType: getType,
    getMessage: getMessage,
    clear: clear
  };

});
