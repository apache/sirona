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


  var jtaControllers = angular.module('jtaControllers', ['sironaServices','ui.bootstrap','ui.bootstrap.datetimepicker']);

  jtaControllers.controller( 'jtaHomeCtrl', ['$scope','jtaCommits','jtaRollbacks','jtaActives',
    function ( $scope,jtaCommits,jtaRollbacks,jtaActives){

      console.log("jtaHomeCtrl");
      $scope.data={};
      $scope.data.startDate = new Date();
      $scope.data.startDate.setTime($scope.data.startDate.getTime() - dayDuration);


      $scope.data.endDate = new Date();

      $scope.data.format = 'dd/MM/yyyy HH:mm:ss';

      jQuery("#dropdown-enddate").dropdown();
      jQuery("#dropdown-startdate").dropdown();

      var drawCommits = function(){
        jtaCommits.query({start: $scope.data.startDate.getTime(),end: $scope.data.endDate.getTime()} ).$promise.then( function ( results ){
          $scope.commitsResults = toMorrisFormat( results.data );
          jQuery("#commits").empty();
          Morris.Line({
                        element: 'commits',
                        data: $scope.commitsResults,
                        xkey: 'x',
                        ykeys: 'y',
                        labels: [results.label],
                        xLabelFormat: function ( ret ){
                          var date = new Date();
                          date.setTime($scope.commitsResults[ret.x].x);
                          return date.toLocaleString();
                        },
                        parseTime: false,
                        hideHover: 'auto'
                      });
          });
      };


      var drawRollbacks = function(){
        jtaRollbacks.query({start: $scope.data.startDate.getTime(),end: $scope.data.endDate.getTime()} ).$promise.then( function ( results ){
          $scope.rollbacksResults = toMorrisFormat( results.data );
          jQuery("#rollbacks").empty();
          Morris.Line({
                        element: 'rollbacks',
                        data: $scope.rollbacksResults,
                        xkey: 'x',
                        ykeys: 'y',
                        labels: [results.label],
                        xLabelFormat: function ( ret ){
                          var date = new Date();
                          date.setTime($scope.rollbacksResults[ret.x].x);
                          return date.toLocaleString();
                        },
                        parseTime: false,
                        hideHover: 'auto'
                      });
        });
      };


      var drawActives = function(){
        jtaActives.query({start: $scope.data.startDate.getTime(),end: $scope.data.endDate.getTime()} ).$promise.then( function ( results ){
          $scope.activesResults = toMorrisFormat( results.data );
          jQuery("#actives").empty();
          Morris.Line({
                        element: 'actives',
                        data: $scope.activesResults,
                        xkey: 'x',
                        ykeys: 'y',
                        labels: [results.label],
                        xLabelFormat: function ( ret ){
                          var date = new Date();
                          date.setTime($scope.activesResults[ret.x].x);
                          return date.toLocaleString();
                        },
                        parseTime: false,
                        hideHover: 'auto'
                      });
        });
      };

      $scope.updateGraphs = function(){
        drawCommits();
        drawRollbacks();
        drawActives();
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





