'use strict';

define(['jquery','controllers', 'angular-route', 'bootstrap'],
       function (jquery,controllers) {

  console.log("load sirona.js");

  var sirona = angular.module('sirona', [
    'ngRoute',
    'homeControllers'
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
        otherwise({
          redirectTo: '/home'
        });
    }]);

  angular.bootstrap(document,['sirona']);
});