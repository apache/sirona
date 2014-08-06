'use strict';

/* Services */
define(['angular'], function (){
  var sironaServices = angular.module('sironaJvmServices', ['ngResource']);

  sironaServices.factory('jvmCpu', ['$resource',
    function($resource){
      return $resource('restServices/sironaServices/jvmreports/cpu', {}, {
        query: {method:'GET', params:{}, isArray:true}
      });
    }]);

});