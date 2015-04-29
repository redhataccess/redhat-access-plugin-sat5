'use strict';

/**
 * @ngdoc function
 * @name sat5TelemetryApp.controller:SystemTable
 * @description
 * # Systemtable
 * Controller of the sat5TelemetryApp
 */
angular.module('sat5TelemetryApp')
.controller('SystemTable', function (
_,
$scope, 
Admin, 
Alert, 
SAT5_ROOT_URLS, 
SYSTEM_DETAILS_PAGE_URLS) {
  $scope.loading = true;
  $scope.filter = '';
  $scope.systems = [];
  $scope.validSystems = [];
  $scope.orderBy = 'name';
  $scope.pageSize = 10;
  $scope.pageSizes = [5, 10, 20, 50];
  $scope.alphas = 
    ['0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 
     'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N',
     'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'];
  $scope.alpha = '';

  $scope.filteredSystems = [];
  $scope.getSystems = Admin.getSystems;
  $scope.getPageSize = Admin.getPageSize;
  $scope.getPage = Admin.getPage;
  $scope.getPageStart = Admin.getPageStart;

  $scope.disableAlphabarElement = function(alpha) {
    return !_.some(Admin.getSystems(), function(sys) {
      return _.startsWith(sys.name.toLowerCase(), alpha.toLowerCase());
    });
  };

  $scope.alphabarFilter = function(alpha) {
    Admin.setPage(0);
    if ($scope.alpha === alpha) {
      $scope.alpha = '';
    } else {
      $scope.alpha = alpha;
    }
  };

  $scope.doFilter = function() {
    Admin.setPage(0);
  };

  $scope.setPageSize = function() {
    Admin.setPage(0);
    Admin.setPageSize($scope.pageSize);
  };

  $scope.toggleStatusSort = function() {
    if ($scope.orderBy === 'status') {
      $scope.orderBy = '-status';
    } else {
      $scope.orderBy = 'status';
    }
  };

  $scope.toggleNameSort = function() {
    if ($scope.orderBy === 'name') {
      $scope.orderBy = '-name';
    } else {
      $scope.orderBy = 'name';
    }
  };

  $scope.toggleAll = function() {
    var allSelected = $scope.allSelected();
    _.forEach($scope.validSystems, function(system) {
      system.enabled = !allSelected;
    });
  };

  $scope.selectAll = function() {
    $scope.allSelected = true;
    $scope.toggleAll();
    $scope.allPartiallySelected = true;
  };

  $scope.allSelected = function() {
    return !_.some($scope.validSystems, {'enabled': false});
  };

  $scope.allPartiallySelected = function() {
    var response = false;
    var someSelected = _.some($scope.validSystems, {'enabled': true});
    if ($scope.allSelected()) {
      response = false;
    } else if (someSelected) {
      response = true;
    }
    return response;
  };

  $scope.systemPartiallySelected = function(system) {
    var response = false;
    if ($scope.getInstallationStatus(system) === 0 ||
        $scope.getInstallationStatus(system) === 1) {
      response = true;
    }
    return response;
  };

  $scope.getNumSelected = function() {
    return _.where($scope.validSystems, {'enabled': true}).length;
  };

  $scope.getSystemUrl = function(system) {
    return '/' + SAT5_ROOT_URLS.RHN + '/' + 
      SYSTEM_DETAILS_PAGE_URLS.OVERVIEW + '?sid=' + system.id;
  };

  $scope.doApply = function() {
    Admin.postSystems($scope.validSystems)
      .success(function(response) {
        console.log(response);
      })
      .error(function(error) {
        Alert.danger('Problem updating systems. Please try again.');
      });
  };

  //if status===true return systems with installationStatus===true
  $scope.makeStatusMessage = function(system, status) {
    var messages = [];
    var groupedStatus = 
      _.groupBy(Object.keys(system.installationStatus), function(value, key, statuses) {
        if (system.installationStatus[value]) {
          return true;
        } else {
          return false;
        }
      });

    if (!status) {
      if (!_.isEmpty(groupedStatus['false']) && 
          !_.isEmpty(groupedStatus['false']['rpmInstalled'])) {
        messages.push('RPM is not installed');
      } 
      if (!_.isEmpty(groupedStatus['false']) && 
          !_.isEmpty(groupedStatus['false']['softwareChannelAssociated'])) {
        messages.push('Software channel not associated');
      }
      if (!_.isEmpty(groupedStatus['false']) && 
          !_.isEmpty(groupedStatus['false']['configChannelAssociated'])) {
        messages.push('Config channel not associated');
      }
      if (!_.isEmpty(groupedStatus['false']) && 
          !_.isEmpty(groupedStatus['false']['configDeployed'])) {
        messages.push('Config out of sync with latest revision');
      }
    } else {
      if (!_.isEmpty(groupedStatus['true']) && 
          !_.isEmpty(groupedStatus['true']['rpmInstalled'])) {
        messages.push('RPM installed');
      } 
      if (!_.isEmpty(groupedStatus['true']) && 
          !_.isEmpty(groupedStatus['true']['softwareChannelAssociated'])) {
        messages.push('Software channel associated');
      }
      if (!_.isEmpty(groupedStatus['true']) && 
          !_.isEmpty(groupedStatus['true']['configChannelAssociated'])) {
        messages.push('Config channel associated');
      }
      if (!_.isEmpty(groupedStatus['true']) && 
          !_.isEmpty(groupedStatus['true']['configDeployed'])) {
        messages.push('Config in sync with latest revision');
      }
    }

    var response = '';
    _.forEach(messages, function(message, index) {
      if (index === messages.length - 1) {
        response = response + message;
      } else {
        response = response + message + ', ';
      }
    });
    
    return response;
  };

  /**
   * -1 - what?
   *  0 - failed install
   *  1 - in progress install
   *  2 - no install
   *  3 - successful install
   *  4 - invalid system type
   */
  var FAILED_INSTALL_STATUS = 0;
  var IN_PROGRESS_INSTALL_STATUS = 1;
  var NO_INSTALL_STATUS = 2;
  var SUCCESSFUL_INSTALL_STATUS = 3;
  var INVALID_TYPE_STATUS = 4;
  $scope.getInstallationStatus = function(system) {
    var response = -1;
    var installationStatus = system.installationStatus;
    if (!system.validType) {
      response = INVALID_TYPE_STATUS;
    } else if (!installationStatus.rpmInstalled &&
        !installationStatus.configDeployed &&
        !installationStatus.configChannelAssociated &&
        !installationStatus.softwareChannelAssociated) {
      response = NO_INSTALL_STATUS;
    } else if (!installationStatus.rpmInstalled || 
        !installationStatus.configDeployed || 
        !installationStatus.configChannelAssociated || 
        !installationStatus.softwareChannelAssociated) {
      response = FAILED_INSTALL_STATUS;
    } else {
      response = SUCCESSFUL_INSTALL_STATUS;
    }

    return response;
  };

  $scope.noInstallation = function(system) {
    var status = $scope.getInstallationStatus(system);
    if (status === NO_INSTALL_STATUS) {
      return true;
    } else {
      return false;
    }
  };

  $scope.installationPending = function(system) {
    var status = $scope.getInstallationStatus(system);
    if (status === IN_PROGRESS_INSTALL_STATUS) {
      return true;
    } else {
      return false;
    }
  };

  $scope.installationSuccess = function(system) {
    var status = $scope.getInstallationStatus(system);
    if (status === SUCCESSFUL_INSTALL_STATUS) {
      return true;
    } else {
      return false;
    }
  };

  $scope.installationFail = function(system) {
    var status = $scope.getInstallationStatus(system);
    if (status === FAILED_INSTALL_STATUS) {
      return true;
    } else {
      return false;
    }
  };

  $scope.invalidType = function(system) {
    var status = $scope.getInstallationStatus(system);
    if (status == INVALID_TYPE_STATUS) {
      return true;
    } else {
      return false;
    }
  };

  $scope.setSelectionState = function() {
    _.forEach($scope.validSystems, function(system) {
      if ($scope.installationSuccess(system)) {
        system.enabled = true;
      } else if ($scope.noInstallation(system)) {
        system.enabled = false;
      } else {
        system.enabled = false;
      }
    });
  };

  Admin.getSystemsPromise()
    .success(function(response) {
      $scope.loading = false;
      Admin.setSystems(response);
      //$scope.validSystems = 
        //_.filter(
          //response, 
          //{'validType': true});
      //$scope.setSelectionState();
    })
    .error(function(error) {
      $scope.loading = false;
      Alert.danger('Problem loading systems. Please try again.');
    });
});
