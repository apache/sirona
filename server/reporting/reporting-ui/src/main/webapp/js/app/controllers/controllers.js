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

  jvmControllers.controller( 'JvmHomeCtrl', ['$scope','jvmCpu', function ( $scope,jvmCpu ){
    console.log("JvmHomeCtrl");

    var yesterday = new Date();
    yesterday.setTime(yesterday.getTime() - dayDuration);

    var now = new Date();

    jvmCpu.query({start:yesterday.getTime(),end:now.getTime()} ).$promise.then(function(cpuResults){
      var morrisDatas=toMorrisFormat(cpuResults.data);
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


  }]);

  /*
   format
   {"label":"CPU Usage","color":"#317eac"
   ,"data":{"1407322921555":3.71142578125,"1407322981555":2.63671875,"1407323041555":1.97216796875}}

   to morris format
   [
   { y: '2006', a: 100 },
   { y: '2007', a: 75 },
   { y: '2008', a: 50 },
   { y: '2009', a: 75 },
   { y: '2010', a: 50 },
   { y: '2011', a: 75 },
   { y: '2012', a: 100 }
   ]
   */
  var toMorrisFormat=function(reportResult){
    if (reportResult==null){
      console.log("reportResult==null");
      return [];
    }
    var values = [];
    angular.forEach(reportResult, function(key,value) {
      this.push({x:value,y: key});
    }, values);

    console.log("size:"+values.length+':'+values[0].x+","+values[0].y);
    console.log("size:"+values.length+':'+values[1].x+","+values[1].y);
    console.log("size:"+values.length+':'+values[10].x+","+values[10].y);

    return values;
  }

});





