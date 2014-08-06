'use strict';

define(['jquery','controllers', 'angular-route', 'bootstrap'],
       function (jquery,controllers) {

  var sirona = angular.module('sirona', [
    'ngRoute',
    'homeControllers',
    'jvmControllers'
  ]);

  sirona.config(['$routeProvider',
    function($routeProvider) {
      $routeProvider.
        when('/home',
             {
                templateUrl: 'partials/home.html',
                controller: 'HomeCtrl'
             }
         ).
         when('/jvm',
              {
                templateUrl: 'partials/jvm.html',
                controller: 'JvmHomeCtrl'
              }
         ).
        otherwise({
          redirectTo: '/home'
        });
    }]);

  angular.bootstrap(document,['sirona']);

});