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

/* Services */
define(['angular','angular-resource'], function (){
  var sironaServices = angular.module('sironaServices', ['ngResource']);

  sironaServices.factory('jvmCpu', ['$resource',
    function($resource){
      return $resource('restServices/sironaServices/jvmreports/cpu/:start/:end',
                       {start:'@start',end:'@end'},
                       {query: {method:'GET', params:{}}});
    }
  ]);

  sironaServices.factory('jvmMemory', ['$resource',
    function($resource){
      return $resource('restServices/sironaServices/jvmreports/memory/:start/:end',
                       {start:'@start',end:'@end'},
                       {query: {method:'GET', params:{}}});
    }
  ]);

  sironaServices.factory('nonHeapMemory', ['$resource',
    function($resource){
      return $resource('restServices/sironaServices/jvmreports/nonheapmemory/:start/:end',
                       {start:'@start',end:'@end'},
                       {query: {method:'GET', params:{}}});
    }
  ]);

  sironaServices.factory('activeThreads', ['$resource',
    function($resource){
      return $resource('restServices/sironaServices/jvmreports/activethreads/:start/:end',
                       {start:'@start',end:'@end'},
                       {query: {method:'GET', params:{}}});
    }
  ]);

  sironaServices.factory('osInfo', ['$resource',
    function($resource){
      return $resource('restServices/sironaServices/environment/os',
                       {},
                       {query: {method:'GET', params:{}}});
    }
  ]);

  sironaServices.factory('memoryInfo', ['$resource',
    function($resource){
      return $resource('restServices/sironaServices/environment/memory',
                       {},
                       {query: {method:'GET', params:{}}});
    }
  ]);


  sironaServices.factory('threads', ['$resource',
    function($resource){
      return $resource('restServices/sironaServices/threads/:threadName',
                       {},
                       {query: {method:'GET', params:{threadName:'@threadName'},isArray:true}});
    }
  ]);

  sironaServices.factory('counters', ['$resource',
    function($resource){
      return $resource('restServices/sironaServices/counters/:reportName/:unitName',
                       {},
                       {query: {method:'GET', params:{reportName:'@reportName',unitName:'@unitName'},isArray:true}});
    }
  ]);

  sironaServices.factory('gauges', ['$resource',
    function($resource){
      return $resource('restServices/sironaServices/gauges/:gaugeName/:start/:end',
                       {},
                       {
                         all: {method:'GET', params:{},isArray:true},
                         query: {method:'GET', params:{gaugeName:'@gaugeName',start:'@start',end:'@end'},isArray:false}
                       });
    }
  ]);

  sironaServices.factory('status', ['$resource',
    function($resource){
      return $resource('restServices/sironaServices/status/:nodeName',
                       {},
                       {
                         query: {method:'GET', params:{nodeName:'@nodeName'},isArray:false},
                         all: {method:'GET', params:{},isArray:true}
                       });
    }
  ]);

  sironaServices.factory('jmx', ['$resource',
    function($resource){
      return $resource('restServices/sironaServices/jmx/:mbean',
                       {},
                       {
                         query: {method:'GET', params:{mbean:'@mbean'},isArray:false},
                         all: {method:'GET', params:{},isArray:true},
                         invoke: {method:'POST', isArray:false}
                       });
    }
  ]);


  sironaServices.factory('jtaCommits', ['$resource',
    function($resource){
      return $resource('restServices/sironaServices/jtareports/commits/:start/:end',
                       {start:'@start',end:'@end'},
                       {query: {method:'GET', params:{}}});
    }
  ]);


  sironaServices.factory('jtaRollbacks', ['$resource',
    function($resource){
      return $resource('restServices/sironaServices/jtareports/rollbacks/:start/:end',
                       {start:'@start',end:'@end'},
                       {query: {method:'GET', params:{}}});
    }
  ]);

  sironaServices.factory('jtaActives', ['$resource',
    function($resource){
      return $resource('restServices/sironaServices/jtareports/actives/:start/:end',
                       {start:'@start',end:'@end'},
                       {query: {method:'GET', params:{}}});
    }
  ]);

  sironaServices.factory('sessions', ['$resource',
    function($resource){
      return $resource('restServices/sironaServices/sessions/:start/:end',
                       {start:'@start',end:'@end'},
                       {query: {method:'GET', params:{},isArray:true}});
    }
  ]);

});