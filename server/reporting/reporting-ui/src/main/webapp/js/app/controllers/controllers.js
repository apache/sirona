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
define(['angular','services','morris'], function (){

  var dayDuration = 24 * 3600 * 1000;

  var homeControllers = angular.module('homeControllers', ['sironaJvmServices']);

  homeControllers.controller( 'HomeCtrl', ['$scope', function ( $scope ){
    console.log("HomeCtrl");
  }]);


  var jvmControllers = angular.module('jvmControllers', ['sironaJvmServices']);

  jvmControllers.controller( 'JvmHomeCtrl', ['$scope','jvmCpu','jvmMemory','nonHeapMemory','activeThreads',
    function ( $scope,jvmCpu,jvmMemory,nonHeapMemory,activeThreads ){

      console.log("JvmHomeCtrl");

      var yesterday = new Date();
      yesterday.setTime(yesterday.getTime() - dayDuration);

      var now = new Date();

      jvmCpu.query({start:yesterday.getTime(),end:now.getTime()} ).$promise.then(function(cpuResults){
        var morrisDatas=toMorrisFormat(cpuResults.data);
        $("#cpu" ).empty();
        Morris.Line({
                      element: 'cpu',
                      data: morrisDatas,
                      xkey: 'x',
                      ykeys: 'y',
                      labels: [cpuResults.label],
                      xLabelFormat:function(ret){
                        return new Date(morrisDatas[ret.x].x).toString();
                      },
                      parseTime: false,
                      hideHover: 'auto'
                    });

      });

      jvmMemory.query({start:yesterday.getTime(),end:now.getTime()} ).$promise.then(function(memoryResults){
        var morrisDatas=toMorrisFormat(memoryResults.data);
        $("#memory" ).empty();
        Morris.Line({
                      element: 'memory',
                      data: morrisDatas,
                      xkey: 'x',
                      ykeys: 'y',
                      labels: [memoryResults.label],
                      xLabelFormat:function(ret){
                        return new Date(morrisDatas[ret.x].x).toString();
                      },
                      parseTime: false,
                      hideHover: 'auto'
                    });

      });

      nonHeapMemory.query({start:yesterday.getTime(),end:now.getTime()} ).$promise.then(function(memoryResults){
        var morrisDatas=toMorrisFormat(memoryResults.data);
        $("#nonheapmemory" ).empty();
        Morris.Line({
                      element: 'nonheapmemory',
                      data: morrisDatas,
                      xkey: 'x',
                      ykeys: 'y',
                      labels: [memoryResults.label],
                      xLabelFormat:function(ret){
                        return new Date(morrisDatas[ret.x].x).toString();
                      },
                      parseTime: false,
                      hideHover: 'auto'
                    });

      });

      activeThreads.query({start:yesterday.getTime(),end:now.getTime()} ).$promise.then(function(results){
        var morrisDatas=toMorrisFormat(results.data);
        $("#activethreads" ).empty();
        Morris.Line({
                      element: 'activethreads',
                      data: morrisDatas,
                      xkey: 'x',
                      ykeys: 'y',
                      labels: [results.label],
                      xLabelFormat:function(ret){
                        return new Date(morrisDatas[ret.x].x).toString();
                      },
                      parseTime: false,
                      hideHover: 'auto'
                    });

      });

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





