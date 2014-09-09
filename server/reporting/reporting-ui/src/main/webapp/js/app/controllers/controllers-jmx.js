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
define(['jquery','angular','bootstrap','services','ui-bootstrap','abn-tree','angular-animate'], function (){


  var jmxControllers = angular.module('jmxControllers', ['jmxControllers','ui.bootstrap','angularBootstrapNavTree'
                                      ,'ngAnimate']);

  jmxControllers.controller( 'jmxHomeCtrl', ['$scope','$routeParams','jmx','$location',
    function ($scope,$routeParams,jmx,$location){

      $scope.treeData=[{label:"Loading"}];


      console.log("jmxHomeCtrl:");

      jmx.query().$promise.then(function(result){
        $scope.treeData=[result];
      });

      $scope.selectionHandler=function(branch){ //
        if (branch.leaf){
          console.log("selectionHandler:'"+branch.base64+"'");
          //$location.url("jmx/"+branch.base64);
          jmx.query({mbean:branch.base64}).$promise.then(function(result){
            $scope.mbean=result;
          });
        }
      };

      $scope.invoke=function(name,base64){
        console.log("name:"+name+","+base64);

        var parameters = [];

        jQuery("#"+name + " input").each(function( index ) {
          parameters.push(jQuery( this ).val());
        });

        var request={ mbeanEncodedName: base64, operationName: name, parameters: parameters };

        jmx.invoke({ mbeanEncodedName: base64, operationName: name, parameters: parameters })
            .$promise.then(function(result){
          $scope.invokeResult=result;
        });


      };

  }]);

  jmxControllers.controller( 'jmxDetailCtrl', ['$scope','$routeParams','jmx','$location',
    function ($scope,$routeParams,jmx,$location){

      $scope.treeData=[{label:"Loading"}];


      console.log("jmxDetailCtrl:"+$routeParams.mbeanName);

      jmx.query().$promise.then(function(result){
        $scope.treeData=[result];

        jmx.query({mbean:$routeParams.mbeanName}).$promise.then(function(result){
          $scope.mbean=result;
        });

      });



      $scope.selectionHandler=function(branch){ //
        if (branch.leaf){
          console.log("selectionHandler:'"+branch.base64+"'");
          $location.url("jmx/"+branch.base64);
        }
      }

    }]);


});





