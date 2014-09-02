/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
'use strict';

/* Controllers */
define(['jquery','angular','bootstrap','services','morris','ui-bootstrap','datetimepicker'], function (){

  var dayDuration = 24 * 3600 * 1000;

  var gaugesControllers = angular.module('gaugesControllers', ['sironaServices','ui.bootstrap','ui.bootstrap.datetimepicker']);

  gaugesControllers.controller( 'GaugesHomeCtrl', ['$scope','$routeParams','gauges',
    function ($scope,$routeParams,gauges){

      $scope.data={};

      gauges.all().$promise.then(function(result){
        $scope.data.gaugesInfo=result;

      });

  }]);

  gaugesControllers.controller( 'gaugeDetailCtrl', ['$scope','$routeParams','gauges',
    function ($scope,$routeParams,gauges){

      $scope.data={};
      $scope.data.startDate = new Date();
      $scope.data.startDate.setTime($scope.data.startDate.getTime() - dayDuration);


      $scope.data.endDate = new Date();

      $scope.data.format = 'dd/MM/yyyy HH:mm:ss';

      jQuery("#dropdown-enddate").dropdown();
      jQuery("#dropdown-startdate").dropdown();

      var gaugeName=$routeParams.gaugeName;

      console.log("gaugeName:"+gaugeName);

      var drawGauge = function(){

        gauges.query({gaugeName: gaugeName,start:$scope.data.startDate.getTime(),end:$scope.data.endDate.getTime()})
            .$promise.then(function(result){
                             $scope.data.gaugeResult=result;
                             var morrisDatas=toMorrisFormat(result.gaugeValues);
                             console.log("morrisDatas.length:"+morrisDatas.length);
                             jQuery("#gaugeresult").empty();
                             Morris.Line({
                                           element: 'gaugeresult',
                                           data: morrisDatas,
                                           xkey: 'x',
                                           ykeys: 'y',
                                           labels: [result.gaugeName],
                                           xLabelFormat:function(ret){
                                             var date = new Date();
                                             date.setTime(morrisDatas[ret.x].x);
                                             return date.toLocaleString();
                                           },
                                           parseTime: false,
                                           hideHover: 'auto'
                                         });
                           });

      };

      $scope.updateGraph = function(){
        drawGauge();
      };

      $scope.updateGraph();



  }]);

  var toMorrisFormat=function(reportResult){
    if (reportResult==null){
      console.log("reportResult==null");
      return [];
    }
    var values = [];
    angular.forEach(reportResult, function(key,value) {
      this.push({x:key.timestamp,y: key.value});
    }, values);


    return values;
  }



});





