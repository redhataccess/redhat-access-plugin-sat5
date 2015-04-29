'use strict';

/**
 * @ngdoc function
 * @name sat5TelemetryApp.controller:Pagination
 * @description
 * Controller of the sat5TelemetryApp
 */
angular.module('sat5TelemetryApp')
.controller('Pagination', function (_, $scope, Admin) {
  $scope.getNumSystems = Admin.getNumSystems;
  $scope.setPage = Admin.setPage;
  $scope.getPage = Admin.getPage;
  $scope.getPageStart = Admin.getPageStart;
  $scope.getPageSize = Admin.getPageSize;
  $scope.getNumSystems = Admin.getNumSystems;

  $scope.getPageEnd = function() {
    var pageEnd = Admin.getPageStart() + Admin.getPageSize();
    if (pageEnd > Admin.getNumSystems()) {
      pageEnd = Admin.getNumSystems();
    }
    return pageEnd;
  };

  $scope.pageBack = function() {
    Admin.setPage(Admin.getPage() - 1);
  };

  $scope.pageForward = function() {
    Admin.setPage(Admin.getPage() + 1);
  };

  $scope.pageEnd = function() {
    Admin.setPage(Math.floor(Admin.getNumSystems() / Admin.getPageSize()) - 1);
  };

  $scope.pageStart = function() {
    Admin.setPage(0);
  };

  $scope.disablePageBack = function() {
    return Admin.getPage() === 0;
  };

  $scope.disablePageForward = function() {
    return Admin.getPageStart() + Admin.getPageSize() >= Admin.getNumSystems();
  };
});
