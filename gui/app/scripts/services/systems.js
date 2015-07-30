'use strict';

/**
 * @ngdoc service
 * @name sat5TelemetryApp.Systems
 * @description
 * # Systems
 * Service in the sat5TelemetryApp.
 */
angular.module('sat5TelemetryApp')
.service('Sat5TelemetrySystems', function (
$http, 
$rootScope, 
EVENTS,
HTTP_CONST,
TELEMETRY_URLS) {

  this.systems = [];

  this.populate = function() {
    var headers = {};
    headers[HTTP_CONST.ACCEPT] = HTTP_CONST.APPLICATION_JSON;
    var promise = $http({
      method: HTTP_CONST.GET,
      url: TELEMETRY_URLS.SYSTEMS_SUMMARY,
      headers: headers
    });
    promise.success(angular.bind(this, function(systems) {
      var alertDiv = $('#rha-insights-sat5-loading-alert');
      if (alertDiv) {
        alertDiv.remove();
      }
      this.systems = systems.systems;
      $rootScope.$broadcast(EVENTS.SYSTEMS_POPULATED);
    }));
    promise.error(function(error) {
      //TODO: real error handling
      console.log(error);
    });
    return promise;
  };
});
