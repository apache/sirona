'use strict';

/* Services */

var sironaServices = angular.module('sironaServices', ['ngResource']);

sironaServices.factory('jvm', ['$resource',
  function($resource){
    return $resource('jvm', {}, {
      query: {method:'GET', params:{}, isArray:true}
    });
  }]);
