'use strict';

/**
 * @ngdoc overview
 * @name sat5TelemetryApp
 * @description
 * # sat5TelemetryApp
 *
 * Main module of the application.
 */
angular.module('sat5TelemetryApp', ['insights', 'ui.indeterminate', 'ui.ace'])
.config(function(
          $urlRouterProvider, 
          $locationProvider, 
          $stateProvider,
          SAT5_ROOT_URLS, 
          TELEMETRY_URLS,
          InsightsConfigProvider,
          SYSTEM_DETAILS_PAGE_URLS) {

  InsightsConfigProvider.setFetchRelatedSolution(false);         
  InsightsConfigProvider.setGetSystemStatus(true);
  InsightsConfigProvider.setApiRoot('/' + SAT5_ROOT_URLS.PROXY + TELEMETRY_URLS.API_ROOT + '/');
  InsightsConfigProvider.setOverviewShowSystem(
    function(system) {
      window.location = '/' + SAT5_ROOT_URLS.RHN + '/' + 
        SYSTEM_DETAILS_PAGE_URLS.INSIGHTS + '?sid=' + system.remote_leaf;
    });
  InsightsConfigProvider.setGetReportsErrorCallback(
    function(data, status, headers, config) {
      if (status === 404) {
        return '<p>This system is not registered with Red Hat Access Insights. First install the redhat-access-insights RPM using the <a href="/rhn/systems/Insights.do#/setup">Setup page</a>. Then register this system with Red Hat Access Insights by following <a href="https://access.redhat.com/insights/getting-started/satellite/5/#register">these instructions</a>.</p>';
      }
    });

  $urlRouterProvider.otherwise(function() {
  });
  $locationProvider.html5Mode({
    enabled: false,
    requireBase: false,
    rewriteLinks: false
  });

  $stateProvider
    .state('rhaInsightsSat5Setup', {
      url: '/setup',
      templateUrl: 'scripts/views/setupState.html'
    });

  $stateProvider
    .state('rhaInsightsSat5General', {
      url: '/general',
      templateUrl: 'scripts/views/generalState.html'
    });

})
.run(function(
      $rootScope,
      $state,
      Sat5TelemetrySystems,
      SystemOverviewService,
      Util,
      Admin,
      SYSTEM_PAGE_URLS, 
      EVENTS,
      SAT5_ROOT_URLS, 
      ADMIN_PAGE_URLS, 
      SYSTEM_DETAILS_PAGE_URLS,
      RHA_INSIGHTS,
      TELEMETRY_URLS,
      _) {


  var appendToSideNav = function(url, isState, content, hide) {
    var classString = '';
    if (hide) {
      classString = classString + 'ng-hide';
    }
    $('#sidenav > ul').append(
      '<li id="rha-insights-sidenav" class="' + classString + '"><a href="/' + SAT5_ROOT_URLS.RHN + '/' + url + '">Insights</a></li>');
    //highlight Insights nav>li when selected
    if (RHA_INSIGHTS.UTILS.isOnPage(url)) {
      var currentSelection = $('#sidenav > ul > li .active')[0];
      if (currentSelection && currentSelection.parentElement && 
          currentSelection.parentElement.parentElement &&
          currentSelection.parentElement.parentElement.tagName === 'LI') {
        currentSelection.parentElement.remove();
      }

      currentSelection = $('#sidenav > ul > li.active')[0];
      if (currentSelection &&
          currentSelection.nextElementSibling &&
          currentSelection.nextElementSibling.firstElementChild &&
          currentSelection.nextElementSibling.firstElementChild.tagName === 'UL') {
        currentSelection.nextElementSibling.remove();
      }

      $('#sidenav > ul > li').removeClass('active');
      $('#sidenav > ul > li:last').addClass('active');

      if (!isState) {
        $('#spacewalk-content').append(content);
      }
    }
  };

  //Check which page we're on then make appropriate changes to dom
  if (RHA_INSIGHTS.UTILS.isOnSystemTabPage()) {
    //Add Insights to side nav
    appendToSideNav(SYSTEM_PAGE_URLS.INSIGHTS, true, 'app.overview', true);
  }

  if (RHA_INSIGHTS.UTILS.isOnSystemListPage()) {

    var HEALTH_TABLE_POS = 1;

    $('<th id="rha-insights-system-health-col-head" class="ng-hide">Insights</th>').insertAfter(
      $('.table > thead > tr > th:eq(' + HEALTH_TABLE_POS + ')'));

    //for (i < pageSize)
    //  if at end of table
    //    break
    var pageSize = $('.list-sizeselector > select').val();
    if (Util.thereAreSystemsOnOverviewPage()) {
      for(var i = 0; i < pageSize; i++) {
        var row = $('.table > tbody > tr:eq(' + i + ')');
        if (row.length === 0) {
          break;
        }
        var systemUrl = $('.table > tbody > tr:eq(' + i + ') > td:eq(1) > a')[0].href;
        var sid = Util.getSidFromUrl(systemUrl);

        $('<td class="rha-insights-system-health-col ng-hide"><health-icon sid="' + sid + '"/></td>').insertAfter(
          $('.table > tbody > tr:eq(' + i + ') > td:eq(' + HEALTH_TABLE_POS + ')'));
      }
    }
    
  } else if (RHA_INSIGHTS.UTILS.isOnSystemDetailsPage()) {

    $('<li id="rha-insights-system-details" class="ng-hide"><a href="/rhn/systems/details/Insights.do?' + 
      'sid=' + Util.getSidFromUrl(window.location.search) + '">Insights</a></li>').insertAfter(
        $('#spacewalk-content > div.spacewalk-content-nav > ul.nav-tabs-pf > li:last')); 
    if (RHA_INSIGHTS.UTILS.isOnPage(SYSTEM_DETAILS_PAGE_URLS.INSIGHTS)) {
      $('#spacewalk-content > div.spacewalk-content-nav > ul.nav-tabs-pf > li').
        removeClass('active');
      $('#spacewalk-content > div.spacewalk-content-nav > ul.nav-tabs-pf > li:last').
        addClass('active');


      //Add header
      $('#spacewalk-content').
        append('<h2>Red Hat Access Insights</h2>');
      
      //Add content
      $('#spacewalk-content').
        append('<div class="main-content insights-main-content">' + 
                 '<div ' + 
                   'class="rule-summaries" ' + 
                   'loading="{isLoading: true}" ' + 
                   'machine-id="' + Util.getSidFromUrl(window.location.search) + '" ' + 
                   'rule="" ' + 
                   'system="{}"/>' + 
               '</div>');

      Admin.getSystemDetails(Util.getSidFromUrl())
        .then(function(response) {
          //add the system name, proper icon, system ID to Delete System and Add to SSM link urls
          var iconClass = 'spacewalk-icon-virtual-host';
          if (response.data.type === 'guest') {
            iconClass = 'spacewalk-icon-virtual-guest';
          }
          $('#spacewalk-content > .spacewalk-toolbar-h1 > h1').html('<i class="fa ' + iconClass + '" />');
          $('#spacewalk-content > .spacewalk-toolbar-h1 > h1').append(response.data.name);
          var firstLink = $('#spacewalk-content > .spacewalk-toolbar-h1 > .spacewalk-toolbar > a:eq(0)');
          firstLink.attr('href', firstLink.attr('href') + response.data.id);
          var secondLink = $('#spacewalk-content > .spacewalk-toolbar-h1 > .spacewalk-toolbar > a:eq(1)');
          secondLink.attr('href', secondLink.attr('href') + response.data.id);

          $('#spacewalk-content > .spacewalk-toolbar > a:eq(0)').text(response.data.name);
        });

    }
  } else if (RHA_INSIGHTS.UTILS.isOnAdminPage()) {
    appendToSideNav(ADMIN_PAGE_URLS.INSIGHTS, false, '<rha-insights-sat5-admin/>', false);
  } else if (RHA_INSIGHTS.UTILS.isOnInsightsOverviewPage()) {
    $('#rha-insights-sidenav').after(
      '<li class="rha-insights-sidenav-sub">' + 
        '<ul class="nav nav-pills nav-stacked">' + 
          '<li id="rha-insights-sat5-systems-overview-tab" class="active">' + 
            '<a ng-click="SystemOverviewService.switchToOverviewTab()">Overview</a>' + 
          '</li>' +
          '<li id="rha-insights-sat5-systems-setup-tab">' + 
            '<a ng-click="SystemOverviewService.switchToSetupTab()">Setup</a>' + 
          '</li>' + 
        '</ul>' + 
      '</li>');
  } else if (RHA_INSIGHTS.UTILS.isOnHelpPage()) {
    $('#help-url-list').append('<li><a style="font-size:12pt" href="https://access.redhat.com/insights/info">Red Hat Access Insights Info</a><br/>Information about Red Hat Access Insights</li>');
    $('#help-url-list').append('<li><a style="font-size:12pt" href="https://access.redhat.com/insights/getting-started/satellite/5/">Red Hat Access Insights Getting Started Guide</a><br/>Using and administering Red Hat Access Insights</li>');
  }

  Admin.setEnabled(window.RHA_INSIGHTS.config.enabled);
  Admin.setDebug(window.RHA_INSIGHTS.config.debug);
  Admin.setConfigLoaded(true);
  $rootScope.$broadcast(EVENTS.GENERAL_CONFIG_LOADED);
  if (window.RHA_INSIGHTS.config.enabled) {
    $('#rha-insights-sidenav').removeClass('ng-hide');
    $('#rha-insights-system-details').removeClass('ng-hide');
    $('.rha-insights-system-health-col').removeClass('ng-hide');
    $('#rha-insights-system-health-col-head').removeClass('ng-hide');
    if (RHA_INSIGHTS.UTILS.isOnSystemListPage() && Util.thereAreSystemsOnOverviewPage()) {
      Sat5TelemetrySystems.populate();
    }
  }
});
