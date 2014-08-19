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
define(['angular','services','morris','ui-bootstrap'], function (){

  var dayDuration = 24 * 3600 * 1000;


  var jvmControllers = angular.module('jvmControllers', ['sironaJvmServices','ui.bootstrap']);

  jvmControllers.controller( 'JvmHomeCtrl', ['$scope','jvmCpu','jvmMemory','nonHeapMemory','activeThreads',
                                              'osInfo','memoryInfo',
    function ( $scope,jvmCpu,jvmMemory,nonHeapMemory,activeThreads,osInfo,memoryInfo){

      console.log("JvmHomeCtrl");

      $scope.startDate = new Date();
      $scope.startDate.setTime($scope.startDate.getTime() - dayDuration);


      $scope.endDate = new Date();


      $scope.format = 'dd/MM/yyyy HH:mm:ss';

      $scope.startDateOpen = function($event) {
        $event.preventDefault();
        $event.stopPropagation();
        $scope.startDateOpened=!$scope.startDateOpened;

      };

      $scope.endDateOpen = function($event) {
        $event.preventDefault();
        $event.stopPropagation();
        $scope.endDateOpened=!$scope.endDateOpened;

      };

      var drawCpu = function()
      {
        console.log("$scope.endDate:"+$scope.endDate);
        jvmCpu.query( {
                        start: $scope.startDate.getTime(),
                        end: $scope.endDate.getTime()
                      } ).$promise.then( function ( results ){
                                           $scope.cpuResults = toMorrisFormat( results.data );
                                           $("#cpu").empty();
                                           Morris.Line( {
                                                          element: 'cpu',
                                                          data: $scope.cpuResults,
                                                          xkey: 'x',
                                                          ykeys: 'y',
                                                          labels: [results.label],
                                                          xLabelFormat: function ( ret )
                                                          {
                                                            var date = new Date();
                                                            date.setTime($scope.cpuResults[ret.x].x);
                                                            return date.toLocaleString();
                                                          },
                                                          parseTime: false,
                                                          hideHover: 'auto'
                                                        } );

                                         } );
      };

      drawCpu();

      jvmMemory.query({start:$scope.startDate.getTime(),end:$scope.endDate.getTime()} ).$promise.then(function(memoryResults){
        var morrisDatas=toMorrisFormat(memoryResults.data);
        $("#memory" ).empty();
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

      nonHeapMemory.query({start:$scope.startDate.getTime(),end:$scope.endDate.getTime()} ).$promise.then(function(memoryResults){
        var morrisDatas=toMorrisFormat(memoryResults.data);
        $("#nonheapmemory" ).empty();
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

      activeThreads.query({start:$scope.startDate.getTime(),end:$scope.endDate.getTime()} ).$promise.then(function(results){
        var morrisDatas=toMorrisFormat(results.data);
        $("#activethreads" ).empty();
        Morris.Line({
                      element: 'activethreads',
                      data: morrisDatas,
                      xkey: 'x',
                      ykeys: 'y',
                      labels: [results.label],
                      xLabelFormat:function(ret){
                        return "";
                      },
                      parseTime: false,
                      hideHover: 'auto'
                    });

      });

      osInfo.query().$promise.then(function(result){
        $scope.os=result;

      });

      memoryInfo.query().$promise.then(function(result){
        $scope.memory=result;
      });

      $scope.updateGraphs = function(){
        console.log("updateGraphs");
        drawCpu();
      };

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





