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
define(['jquery','angular','bootstrap','services','ui-bootstrap','nggrid'], function (){

  var dayDuration = 24 * 3600 * 1000;


  var reportControllers = angular.module('countersControllers', ['sironaJvmServices','ngGrid']);

  reportControllers.controller( 'countersHomeCtrl', ['$scope','$routeParams','$http','counters',
    function ( $scope,$routeParams,$http,counters){

      $scope.colDefs = [
        {field: 'name', displayName: 'Counter'},
        {field: 'roleName', displayName: 'Role', cellTemplate:"<div class='ngCellText'>{{row.getProperty(col.field)}} ({{row.getProperty('unitName')}})</div>"},
        {field: 'hits', displayName: 'Hits'},
        {field: 'max', displayName: 'Max'},
        {field: 'mean', displayName: 'Mean'},
        {field: 'min', displayName: 'Min'},
        {field: 'standardDeviation', displayName: 'StandardDeviation'},
        {field: 'sum', displayName: 'Sum'},
        {field: 'variance', displayName: 'Variance'},
        {field: 'concurrency', displayName: 'Concurrency'},
        {field: 'maxConcurrency', displayName: 'MaxConcurrency'},
      ];


      $scope.gridOptions={
        enablePaging: true,
        showFooter: false,
        showGroupPanel: true,
        jqueryUITheme: true,
        columnDefs: 'colDefs',
        plugins: [new ngGridFlexibleHeightPlugin()]
      };

      $http.get('restServices/sironaServices/counters')
          .success(function(data) {
                     $scope.counters = data;
                   });

      $scope.gridOptions.data='counters';



      $scope.getTableStyle= function() {
        var rowHeight=80;
        var headerHeight=50;
        return {
          height: ($scope.gridOptions.data.length * rowHeight + headerHeight) + "px"
        };
      };

  }]);


  function ngGridFlexibleHeightPlugin (opts) {
    var self = this;
    self.grid = null;
    self.scope = null;
    self.init = function (scope, grid, services) {
      self.domUtilityService = services.DomUtilityService;
      self.grid = grid;
      self.scope = scope;
      var recalcHeightForData = function () { setTimeout(innerRecalcForData, 1); };
      var innerRecalcForData = function () {
        var gridId = self.grid.gridId;
        var footerPanelSel = '.' + gridId + ' .ngFooterPanel';
        var extraHeight = self.grid.$topPanel.height() + jQuery(footerPanelSel).height();
        var naturalHeight = self.grid.$canvas.height() + 1;
        if (opts != null) {
          if (opts.minHeight != null && (naturalHeight + extraHeight) < opts.minHeight) {
            naturalHeight = opts.minHeight - extraHeight - 2;
          }
          if (opts.maxHeight != null && (naturalHeight + extraHeight) > opts.maxHeight) {
            naturalHeight = opts.maxHeight;
          }
        }

        var newViewportHeight = naturalHeight + 3;
        if (!self.scope.baseViewportHeight || self.scope.baseViewportHeight !== newViewportHeight) {
          self.grid.$viewport.css('height', newViewportHeight + 'px');
          self.grid.$root.css('height', (newViewportHeight + extraHeight) + 'px');
          self.scope.baseViewportHeight = newViewportHeight;
          self.domUtilityService.RebuildGrid(self.scope, self.grid);
        }
      };
      self.scope.catHashKeys = function () {
        var hash = '',
            idx;
        for (idx in self.scope.renderedRows) {
          hash += self.scope.renderedRows[idx].$$hashKey;
        }
        return hash;
      };
      self.scope.$watch('catHashKeys()', innerRecalcForData);
      self.scope.$watch(self.grid.config.data, recalcHeightForData);
    };
  };


});





