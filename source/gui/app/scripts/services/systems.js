'use strict';

/**
 * @ngdoc service
 * @name sat5TelemetryApp.Systems
 * @description
 * # Systems
 * Service in the sat5TelemetryApp.
 */
angular.module('sat5TelemetryApp')
.service('Systems', function ($http, $rootScope, EVENTS) {

  this.systems = [];

  this.populate = function() {
    var promise = $http({
      method: 'GET',
      url: '/insights/rs/telemetry/api/v1/systems?summary=true',
      headers: {
        'Accept': 'application/json'
      }     
    });
    promise.success(angular.bind(this, function(systems) {
      this.systems = systems;
      $rootScope.$broadcast(EVENTS.SYSTEMS_POPULATED);
    }));
    promise.error(function(error) {
      //TODO: real error handling
      console.log(error);
    });
    return promise;
  };
});
