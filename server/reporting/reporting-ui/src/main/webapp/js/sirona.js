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

define(['jquery','controllers','controllers-jvm','controllers-threads','controllers-gauges','angular-route',
         'bootstrap','datetimepicker','controllers-report','controllers-status','controllers-jmx','controllers-jta',
         'controllers-sessions'],
       function (jquery,controllers) {

  var sirona = angular.module('sirona', [
    'ngRoute',
    'homeControllers',
    'jvmControllers',
    'threadsControllers',
    'countersControllers',
    'gaugesControllers',
    'statusControllers',
    'jmxControllers',
    'jtaControllers',
    'sessionsControllers'
  ]);

  sirona.config(['$routeProvider','$logProvider',
    function($routeProvider,$logProvider) {
      $logProvider.debugEnabled(true);
      $routeProvider.
        when('/home',
             {
                templateUrl: 'partials/home.html',
                controller: 'HomeCtrl'
             }
        ).
        when('/jvm',
              {
                templateUrl: 'partials/jvm.html',
                controller: 'JvmHomeCtrl'
              }
        ).
        when('/threads',
             {
               templateUrl: 'partials/threads.html',
               controller: 'ThreadsHomeCtrl'
             }
        ).
        when('/threads/:threadName',
             {
               templateUrl: 'partials/threads.html',
               controller: 'ThreadsHomeCtrl'
             }
        ).
        when('/report',
             {
               templateUrl: 'partials/report.html',
               controller: 'countersHomeCtrl'
             }
        ).
        when('/gauges',
             {
               templateUrl: 'partials/gauges.html',
               controller: 'GaugesHomeCtrl'
             }
        ).
        when('/gauges/:gaugeName',
             {
               templateUrl: 'partials/gauge-detail.html',
               controller: 'gaugeDetailCtrl'
             }
        ).
        when('/status',
             {
               templateUrl: 'partials/status.html',
               controller: 'StatusHomeCtrl'
             }
        ).
        when('/status/:nodeName',
             {
               templateUrl: 'partials/status-detail.html',
               controller: 'StatusDetailCtrl'
             }
        ).
        when('/jmx',
             {
               templateUrl: 'partials/jmx.html',
               controller: 'jmxHomeCtrl'
             }
        ).
        when('/jta',
             {
               templateUrl: 'partials/jta.html',
               controller: 'jtaHomeCtrl'
             }
        ).
        when('/jmx/:mbeanName',
             {
               templateUrl: 'partials/jmx-detail.html',
               controller: 'jmxDetailCtrl'
             }
        ).
        when('/sessions',
             {
               templateUrl: 'partials/sessions.html',
               controller: 'sessionsHomeCtrl'
             }
        ).
        otherwise({
          redirectTo: '/home'
        });
    }]);

  angular.bootstrap(document,['sirona']);

});