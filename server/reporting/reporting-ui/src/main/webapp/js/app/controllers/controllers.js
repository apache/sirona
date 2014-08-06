'use strict';

/* Controllers */
define(['angular'], function (){

  var homeControllers = angular.module('homeControllers', []);

  homeControllers.controller( 'HomeCtrl', ['$scope', function ( $scope ){
    console.log("HomeCtrl");
  }]);

});





