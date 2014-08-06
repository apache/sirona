'use strict';

/* Controllers */
define(['angular','services'], function (){

  var homeControllers = angular.module('homeControllers', []);

  homeControllers.controller( 'HomeCtrl', ['$scope', function ( $scope ){
    console.log("HomeCtrl");
  }]);


  var jvmControllers = angular.module('jvmControllers', ['sironaJvmServices']);

  jvmControllers.controller( 'JvmHomeCtrl', ['$scope', function ( $scope ){
    console.log("JvmHomeCtrl");
  }]);

});





