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


  var reportControllers = angular.module('countersControllers', ['sironaServices','ngGrid']);

  reportControllers.controller( 'countersHomeCtrl', ['$scope','$routeParams','$http','counters',
    function ( $scope,$routeParams,$http,counters){

      $scope.colDefs = [
        {field: 'name', displayName: 'Counter',enableFiltering: true},
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

      var csvOpts = {fileName: "counters.csv"};

      $scope.pagingOptions= {
        pageSizes: [25, 50, 1000],
        pageSize: 25,
        currentPage: 1
      };

      $scope.gridOptions={
        enableFiltering: true,
        enablePaging: true,
        pagingOptions: $scope.pagingOptions,
        showFooter: true,
        showHeader: true,
        jqueryUITheme: false,
        columnDefs: 'colDefs',
        plugins: [new ngGridFlexibleHeightPlugin(), new ngGridCsvExportPlugin(csvOpts)]
      };

      $http.get('restServices/sironaServices/counters?unit=ms') //
          .success(function(data) {
                     $scope.counters = data;
                   });

      $scope.gridOptions.data='counters';

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

  function ngGridCsvExportPlugin (opts) {
    var self = this;
    self.grid = null;
    self.scope = null;
    self.services = null;

    opts = opts || {};
    opts.containerPanel = opts.containerPanel || '.ngFooterPanel';
    opts.linkClass = opts.linkCss || 'csv-data-link-span';
    opts.linkLabel = opts.linkLabel || 'CSV Export';
    opts.fileName = opts.fileName || 'Export.csv';

    self.init = function(scope, grid, services) {
      self.grid = grid;
      self.scope = scope;
      self.services = services;

      function showDs() {
        function csvStringify(str) {
          if (str == null) { // we want to catch anything null-ish, hence just == not ===
            return '';
          }
          if (typeof(str) === 'number') {
            return '' + str;
          }
          if (typeof(str) === 'boolean') {
            return (str ? 'TRUE' : 'FALSE') ;
          }
          if (typeof(str) === 'string') {
            return str.replace(/"/g,'""');
          }

          return JSON.stringify(str).replace(/"/g,'""');
        }

        var keys = [];
        var csvData = '';
        for (var f in grid.config.columnDefs) {
          if (grid.config.columnDefs.hasOwnProperty(f))
          {
            keys.push(grid.config.columnDefs[f].field);
            csvData += '"' ;
            if(typeof grid.config.columnDefs[f].displayName !== 'undefined'){/** moved to reduce looping and capture the display name if it exists**/
            csvData += csvStringify(grid.config.columnDefs[f].displayName);
            }
            else{
              csvData += csvStringify(grid.config.columnDefs[f].field);
            }
            csvData +=  '",';
          }
        }

        function swapLastCommaForNewline(str) {
          var newStr = str.substr(0,str.length - 1);
          return newStr + "\n";
        }

        csvData = swapLastCommaForNewline(csvData);
        var gridData = grid.data;
        for (var gridRow in gridData) {
          var rowData = '';
          for ( var k in keys) {
            var curCellRaw;

            if (opts != null && opts.columnOverrides != null && opts.columnOverrides[keys[k]] != null) {
              curCellRaw = opts.columnOverrides[keys[k]](
                  self.services.UtilityService.evalProperty(gridData[gridRow], keys[k]));
            } else {
              curCellRaw = self.services.UtilityService.evalProperty(gridData[gridRow], keys[k]);
            }

            rowData += '"' + csvStringify(curCellRaw) + '",';
          }
          csvData += swapLastCommaForNewline(rowData);
        }
        var fp = grid.$root.find(opts.containerPanel);
        var csvDataLinkPrevious = grid.$root.find(opts.containerPanel + ' .' + opts.linkClass);
        if (csvDataLinkPrevious != null) {csvDataLinkPrevious.remove() ; }
        var csvDataLinkHtml = '<span class="' + opts.linkClass + '">';
        csvDataLinkHtml += '<br><a href="data:text/csv;charset=UTF-8,';
        csvDataLinkHtml += encodeURIComponent(csvData);
        csvDataLinkHtml += '" download="' + opts.fileName + '">' + opts.linkLabel + '</a></br></span>' ;
        fp.append(csvDataLinkHtml);


      }
      setTimeout(showDs, 0);
      scope.catHashKeys = function() {
        var hash = '';
        for (var idx in scope.renderedRows) {
          hash += scope.renderedRows[idx].$$hashKey;
        }
        return hash;
      };
      if (opts && opts.customDataWatcher) {
        scope.$watch(opts.customDataWatcher, showDs);
      } else {
        scope.$watch(scope.catHashKeys, showDs);
      }
    };
  };

});





