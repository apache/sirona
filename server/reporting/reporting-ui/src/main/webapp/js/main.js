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


var angularVersion='1.3.0';

var useAngularMin=false;

require.config({

  baseUrl: "js",
  // FIXME only for development purpose!!
  // retrieve the version and if end with -SNAPSHOT do it only in this case
  urlArgs: "_timestamp=" +  (new Date()).getTime(),
  paths: {
    'jquery': 'jquery-1.11.0.min',
    'angular': 'angular-'+angularVersion+(useAngularMin?".min":""),
    'angular-route': 'angular-route-'+angularVersion+(useAngularMin?".min":""),
    'angular-resource': 'angular-resource-'+angularVersion+(useAngularMin?".min":""),
    'angular-animate': 'angular-animate-'+angularVersion+".min",
    'bootstrap' : 'bootstrap.3.2.0.min',
    'controllers': 'app/controllers/controllers',
    'controllers-jvm': 'app/controllers/controllers-jvm',
    'controllers-threads': 'app/controllers/controllers-threads',
    'controllers-report': 'app/controllers/controllers-report',
    'controllers-gauges': 'app/controllers/controllers-gauges',
    'controllers-status': 'app/controllers/controllers-status',
    'controllers-jmx': 'app/controllers/controllers-jmx',
    'controllers-jta': 'app/controllers/controllers-jta',
    'controllers-sessions': 'app/controllers/controllers-sessions',
    'services': 'app/services/services',
    'sirona': 'sirona',
    'morris': 'plugins/morris/morris-0.5.0.min',
    'raphael': 'plugins/morris/raphael-2.1.2.min',
    'ui-bootstrap': 'ui-bootstrap-tpls-0.11.0',
    'datetimepicker': 'datetimepicker-0.2.4',
    'moment': 'moment-2.8.1.min',
    'nggrid': 'ng-grid-2.0.12.debug',
    'abn-tree' : 'abn_tree_directive'
  },

  shim: {
    'angular': ['jquery'],
    'angular-route': ['angular'],
    'angular-resource': ['angular'],
    'angular-animate': ['angular'],
    'morris': ['raphael'],
    'ui-bootstrap': ['angular','bootstrap'],
    'datetimepicker': ['angular','bootstrap','moment'],
    'nggrid': ['angular','bootstrap','ui-bootstrap'],
    'abn-tree': ['angular','bootstrap']
  },

  deps: ['sirona']

});
