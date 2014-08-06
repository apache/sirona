'use strict';


var angularVersion='1.3.0-beta.16';

require.config({

  baseUrl: "js",
  // FIXME only for development purpose!!
  // retrieve the version and if end with -SNAPSHOT do it only in this case
  urlArgs: "_timestamp=" +  (new Date()).getTime(),
  paths: {
    'jquery': 'jquery-1.11.0',
    'angular': 'angular-'+angularVersion,
    'angular-route': 'angular-route-'+angularVersion,
    'angular-resource': 'angular-resource-'+angularVersion,
    'bootstrap' : 'bootstrap.3.2.0.min',
    'controllers': 'app/controllers/controllers',
    'services': 'app/services/services',
    'sirona': 'sirona'
  },

  shim: {
    'angular': ['jquery'],
    'angular-route': ['angular']
  },

  deps: ['sirona']

});
