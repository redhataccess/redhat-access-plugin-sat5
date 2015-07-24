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
  $scope.loadingSystems = true;
  $scope.filter = '';
  $scope.systems = [];
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
  $scope.getSystemStatus = Admin.getSystemStatus;
  $scope.updateSystemStatus = Admin.updateSystemStatus;
  $scope.getLoadingStatuses = Admin.getLoadingStatuses;

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
    _.forEach(Admin.getFilteredSystems(), function(system) {
        system.enabled = !allSelected;
        Admin.updateSystem(system);
        Admin.updateSystemStatus(system);
    });
  };

  $scope.allSelected = function() {
    return !_.some(Admin.getSystems(), {'enabled': false});
  };

  $scope.allPartiallySelected = function() {
    var response = false;
    var someSelected = _.some(Admin.getSystems(), {'enabled': true});
    if ($scope.allSelected()) {
      response = false;
    } else if (someSelected) {
      response = true;
    }
    return response;
  };

  $scope.systemPartiallySelected = function(system) {
    var response = false;
    if (system.indeterminate !== false && 
       ($scope.getInstallationStatus(Admin.getSystemStatus(system)) === IN_PROGRESS_INSTALL_STATUS)) {
      response = true;
    }
    return response;
  };

  $scope.getNumSelected = function() {
    return _.where(Admin.getSystems(), {'enabled': true}).length;
  };

  $scope.getSystemUrl = function(system) {
    return '/' + SAT5_ROOT_URLS.RHN + '/' + 
      SYSTEM_DETAILS_PAGE_URLS.OVERVIEW + '?sid=' + system.id;
  };

  $scope.doApply = function() {
    $scope.loadingSystems = true;
    Alert.info('Applying changes to systems...');
    Admin.postSystems(Admin.getSystemStatuses())
      .success(function(response) {
        Admin.updateStatuses().then(function() {
          $scope.loadingSystems = false;
          Alert.success('Successfully applied changes to systems.', true);
        });
      })
      .error(function(error) {
        Admin.updateStatuses().then(function() {
          $scope.loadingSystems = false;
          Alert.danger('Problem updating systems. Please try again.', true);
        });
      });
  };

  $scope.installStatusTooltip = function(system) {
    var message = '';
    var systemStatus = Admin.getSystemStatus(system);
    var installationStatus = $scope.getInstallationStatus(systemStatus);
    if (installationStatus === NO_CHANNEL_STATUS) {
      message = 'This system is not assigned a channel with the Access Insights RPM.';
    } else if (installationStatus === IN_PROGRESS_INSTALL_STATUS) {
      message = 'Access Insights RPM is scheduled to be installed';
    } else if (installationStatus === NO_INSTALL_STATUS) {
      message = 'Access Insights RPM is not installed';
    } else if (installationStatus === SUCCESSFUL_INSTALL_STATUS) {
      message = 'Access Insights RPM is installed';
    } else if (installationStatus === INVALID_TYPE_STATUS) {
      message = 'Unsupported system version';
    }

    return message;
  };

  /**
   * -1 - loading
   *  0 - failed install
   *  1 - in progress install
   *  2 - no install
   *  3 - successful install
   *  4 - invalid system type
   */
  var LOADING_STATUS = -1;
  var NO_CHANNEL_STATUS = 0;
  var IN_PROGRESS_INSTALL_STATUS = 1;
  var NO_INSTALL_STATUS = 2;
  var SUCCESSFUL_INSTALL_STATUS = 3;
  var INVALID_TYPE_STATUS = 4;
  $scope.getInstallationStatus = function(systemStatus) {
    var response = -1;
    if (!_.isEmpty(systemStatus)) {
      var installationStatus = systemStatus.installationStatus;
      if (!systemStatus.validType) {
        response = INVALID_TYPE_STATUS;
      } else if (!installationStatus.rpmInstalled &&
          !installationStatus.rpmScheduled &&
          !installationStatus.softwareChannelAssociated) {
        response = NO_CHANNEL_STATUS;
      } else if ((!installationStatus.rpmInstalled && 
          !installationStatus.rpmScheduled) &&
          installationStatus.softwareChannelAssociated) {
        response = NO_INSTALL_STATUS;
      } else if (installationStatus.rpmScheduled) {
        response = IN_PROGRESS_INSTALL_STATUS;
      } else {
        response = SUCCESSFUL_INSTALL_STATUS;
      }
    }
    return response;
  };

  $scope.noInstallation = function(systemStatus) {
    var status = $scope.getInstallationStatus(systemStatus);
    if (status === NO_INSTALL_STATUS) {
      return true;
    } else {
      return false;
    }
  };

  $scope.installationPending = function(systemStatus) {
    var status = $scope.getInstallationStatus(systemStatus);
    if (status === IN_PROGRESS_INSTALL_STATUS) {
      return true;
    } else {
      return false;
    }
  };

  $scope.installationSuccess = function(systemStatus) {
    var status = $scope.getInstallationStatus(systemStatus);
    if (status === SUCCESSFUL_INSTALL_STATUS) {
      return true;
    } else {
      return false;
    }
  };

  $scope.noChannel = function(systemStatus) {
    var status = $scope.getInstallationStatus(systemStatus);
    if (status === NO_CHANNEL_STATUS) {
      return true;
    } else {
      return false;
    }
  };

  $scope.installationPending = function(systemStatus) {
    var status = $scope.getInstallationStatus(systemStatus);
    if (status === IN_PROGRESS_INSTALL_STATUS) {
      return true;
    } else {
      return false;
    }
  };

  $scope.invalidType = function(systemStatus) {
    var status = $scope.getInstallationStatus(systemStatus);
    if (status === INVALID_TYPE_STATUS) {
      return true;
    } else {
      return false;
    }
  };

  $scope.loadingStatus = function(systemStatus) {
    var status = $scope.getInstallationStatus(systemStatus);
    if (status === LOADING_STATUS) {
      return true;
    } else {
      return false;
    }
  };

  $scope.setSelectionState = function() {
    _.forEach(Admin.getSystems(), function(system) {
      if ($scope.installationSuccess(system)) {
        system.enabled = true;
      } else if ($scope.noInstallation(system)) {
        system.enabled = false;
      } else {
        system.enabled = false;
      }
    });
  };

  $scope.isLoading = function() {
    var response = false;
    if ($scope.loadingSystems || $scope.getLoadingStatuses()) {
      response = true;
    }
    return response;
  };

  $scope.getLoadingMessage = function() {
    var message = 'Loading ';
    if ($scope.loadingSystems) {
      message = message + 'systems';
    } else if ($scope.getLoadingStatuses()) {
      message = message + 'statuses'; 
    }
    return message + '...';
  };

  $scope.populateSystems = function() {
    $scope.loadingSystems = true;
    var promise = Admin.getSystemsPromise()
      .success(function(response) {
        $scope.loadingSystems = false;
        Admin.setSystems(response);
      })
      .error(function(error) {
        $scope.loadingSystems = false;
        Alert.danger('Problem loading systems. Please try again.');
      });
    return promise;
  };

  $scope.populateSystems();
});
