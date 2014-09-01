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


  var jvmControllers = angular.module('jvmControllers', ['sironaServices','ui.bootstrap','ui.bootstrap.datetimepicker']);

  jvmControllers.controller( 'JvmHomeCtrl', ['$scope','jvmCpu','jvmMemory','nonHeapMemory','activeThreads',
                                              'osInfo','memoryInfo',
    function ( $scope,jvmCpu,jvmMemory,nonHeapMemory,activeThreads,osInfo,memoryInfo){

      console.log("JvmHomeCtrl");
      $scope.data={};
      $scope.data.startDate = new Date();
      $scope.data.startDate.setTime($scope.data.startDate.getTime() - dayDuration);


      $scope.data.endDate = new Date();

      $scope.data.format = 'dd/MM/yyyy HH:mm:ss';

      jQuery("#dropdown-enddate").dropdown();
      jQuery("#dropdown-startdate").dropdown();

      var drawCpu = function(){
        jvmCpu.query({start: $scope.data.startDate.getTime(),end: $scope.data.endDate.getTime()} ).$promise.then( function ( results ){
          $scope.cpuResults = toMorrisFormat( results.data );
          jQuery("#cpu").empty();
          Morris.Line({
                        element: 'cpu',
                        data: $scope.cpuResults,
                        xkey: 'x',
                        ykeys: 'y',
                        labels: [results.label],
                        xLabelFormat: function ( ret ){
                          var date = new Date();
                          date.setTime($scope.cpuResults[ret.x].x);
                          return date.toLocaleString();
                        },
                        parseTime: false,
                        hideHover: 'auto'
                      });
          });
      };

      var drawHeapMemory = function(){
        jvmMemory.query({start:$scope.data.startDate.getTime(),end:$scope.data.endDate.getTime()} ).$promise.then(function(memoryResults){
          var morrisDatas=toMorrisFormat(memoryResults.data);
          jQuery("#memory" ).empty();
          Morris.Line({
                        element: 'memory',
                        data: morrisDatas,
                        xkey: 'x',
                        ykeys: 'y',
                        labels: [memoryResults.label],
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
      
      var drawNonHeapMemory = function(){

        nonHeapMemory.query({start:$scope.data.startDate.getTime(),end:$scope.data.endDate.getTime()} ).$promise.then(function(memoryResults){
          var morrisDatas=toMorrisFormat(memoryResults.data);
          jQuery("#nonheapmemory" ).empty();
          Morris.Line({
                        element: 'nonheapmemory',
                        data: morrisDatas,
                        xkey: 'x',
                        ykeys: 'y',
                        labels: [memoryResults.label],
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

      var drawActiveThreads = function(){
        activeThreads.query({start:$scope.data.startDate.getTime(),end:$scope.data.endDate.getTime()} ).$promise.then(function(results){
          var morrisDatas=toMorrisFormat(results.data);
          jQuery("#activethreads").empty();
          Morris.Line({
                        element: 'activethreads',
                        data: morrisDatas,
                        xkey: 'x',
                        ykeys: 'y',
                        labels: [results.label],
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

      osInfo.query().$promise.then(function(result){
        $scope.os=result;

      });

      memoryInfo.query().$promise.then(function(result){
        $scope.memory=result;
      });

      $scope.updateGraphs = function(){
        drawCpu();
        drawHeapMemory();
        drawNonHeapMemory();
        drawActiveThreads();
      };

      $scope.updateGraphs();

  }]);


  var toMorrisFormat=function(reportResult){
    if (reportResult==null){
      console.log("reportResult==null");
      return [];
    }
    var values = [];
    angular.forEach(reportResult, function(key,value) {
      this.push({x:value,y: key});
    }, values);


    return values;
  }

});





