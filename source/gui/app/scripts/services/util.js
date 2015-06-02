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

  this.isOnInsightsEnabledPage = function() {
    var response = false;
    if (this.isOnSystemListPage() || 
        this.isOnSystemDetailsPage() || 
        this.isOnSystemOverviewPage() ||
        this.isOnAdminPage() ||
        this.isOnInsightsOverviewPage() ||
        this.isOnSystemTabPage()) {
      response = true;
    }
    return response;
  };

  this.isOnPage = function(page) {
    var response = false;
    if (window.location.pathname.indexOf('/' + SAT5_ROOT_URLS.RHN + '/' + page) === 0) {
      response = true;
    }
    return response;
  };

  this.isOnAdminPage = function() {
    return this.isOnPage(SAT5_ROOT_URLS.ADMIN);
  };

  this.isOnSystemTabPage = function() {
    var response = false;
    if (this.isOnPage(SAT5_ROOT_URLS.SYSTEMS) || 
        this.isOnPage(SAT5_ROOT_URLS.SSM) || 
        this.isOnPage(SAT5_ROOT_URLS.ACTIVATIONKEYS) ||
        this.isOnPage(SAT5_ROOT_URLS.PROFILES) ||
        this.isOnPage(SAT5_ROOT_URLS.KICKSTART) ||
        this.isOnPage(SAT5_ROOT_URLS.KEYS)) {
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
        this.isOnPage(SYSTEM_DETAILS_PAGE_URLS.CUSTOM_INFO) ||
        this.isOnPage(SYSTEM_DETAILS_PAGE_URLS.INSIGHTS)) {
      response = true;
    }
    return response;
  };

  this.isOnInsightsOverviewPage = function() {
    var response = false;
    if (this.isOnPage(SYSTEM_PAGE_URLS.INSIGHTS)) {
      response = true;
    }
    return response;
  };

  this.isOnSystemOverviewPage = function() {
    var response = false;
    var query = new URI(window.location.href).query(true);
    if (this.isOnPage(SYSTEM_PAGE_URLS.SYSTEM_OVERVIEW) &&
        (query === undefined ||
        query.showgroups === undefined ||
        !$.parseJSON(query.showgroups))) {
      response = true;
    }
    return response;
  };

  this.isOnSystemListPage = function() {
    if (this.isOnPage(SYSTEM_PAGE_URLS.SYSTEMS) ||
        this.isOnPage(SYSTEM_PAGE_URLS.PHYSICAL) ||
        this.isOnPage(SYSTEM_PAGE_URLS.OUT_OF_DATE) ||
        this.isOnPage(SYSTEM_PAGE_URLS.REQUIRING_REBOOT) ||
        this.isOnPage(SYSTEM_PAGE_URLS.UNGROUPED) ||
        this.isOnPage(SYSTEM_PAGE_URLS.INACTIVE) ||
        this.isOnPage(SYSTEM_PAGE_URLS.RECENTLY_REGISTERED) ||
        this.isOnSystemOverviewPage()) {
      return true;
    } else {
      return false;
    }
  };

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
