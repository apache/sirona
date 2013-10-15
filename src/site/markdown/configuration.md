<!---
Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements.  See the NOTICE file
distributed with this work for additional information
regarding copyright ownership.  The ASF licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance
with the License.  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied.  See the License for the
specific language governing permissions and limitations
under the License.
-->
## Configuration features
### Location
Configuration relies on `commons-monitoring.properties` file which should be found in the commons-monitoring-core classloader.

Note: you can change the file name using org.apache.commons.monitoring.configuration system property.

### Utilities

`org.apache.commons.monitoring.configuration.Configuration` has several utility methods to get
int, boolean... from the properties file. You can reuse it in your monitoring extensions if you want.

Another interesting usage of Configuration class is to be a basic lifecycle handling of your objects.
Using `org.apache.commons.monitoring.configuration.Configuration.newInstance` method you can
decorate your class methods with `org.apache.commons.monitoring.configuration.Configuration.Created`
and `org.apache.commons.monitoring.configuration.Configuration.Destroying` to get lifecycle hooks.

`Destroying` is called when the monitoring is stopped. Generally since commons-monitoring-core is deployed in the
container or JVM classloader it is with the JVM but sometimes you can deploy it in your application. In this case
you'll need to either configure the `javax.servlet.ServletContextListener`
`org.apache.commons.monitoring.web.lifecycle.CommonsMonitoringLifecycle` from reporting module
or to call manually `Configuration.shutdown()` method.

### Main configuration keys (by module)

#### Core

* org.apache.commons.monitoring.configuration: the configuration file path if not using the default
* org.apache.commons.monitoring.shutdown.hook: boolean, true by default. Should be set to false when deploying commons-monitoring-core in an application (see Utilities part).
* org.apache.commons.monitoring.gauge.max-size: int, 100 by default. Number of gauge measures to keep in memory when not persistent.
* org.apache.commons.monitoring.gauge.memory.period: int, 4000 (ms) by default. Period for memory gauge.
* org.apache.commons.monitoring.gauge.cpu.period: int, 4000 (ms) by default. Period for CPU gauge.
* org.apache.commons.monitoring.store.DataStore: qualified class name, default `org.apache.commons.monitoring.store.DefaultDataStore`. DataStore to use.
* org.apache.commons.monitoring.repositories.Repository: qualified class name, default `org.apache.commons.monitoring.repositories.DefaultRepository`. Repository to use.
* org.apache.commons.monitoring.<name>.period: the period to use to flush counters for a batch data store (like graphite one)

#### Reporting

* org.apache.commons.proxy.ProxyFactory: qualified class name. ProxyFactory to use for client aop.
* [plugin name].activated: boolean, true by default. Should the plugin referenced by [plugin.name] be used.
* org.apache.commons.monitoring.jmx.method.allowed: boolean, true by default. Are JMX method invocation allowed.
* org.apache.commons.monitoring.gauge.csv.separator: char, ';' by default. CSV separator for CSV report.
* org.apache.commons.monitoring.gauge.jta.period: jta gauge period
* org.apache.commons.monitoring.gauge.memory.period: memory gauge period
* org.apache.commons.monitoring.gauge.cpu.period: cpu gauge period
* org.apache.commons.monitoring.reporting.activated: if auto deployment of reporting module is activated
* org.apache.commons.monitoring.reporting.mapping: the mapping of monitoring GUI

#### Web

* org.apache.commons.monitoring.web.activated: if auto deployment of web module is activated
* org.apache.commons.monitoring.web.monitored-urls: the mapping of monitored urls
* org.apache.commons.monitoring.gauge.sessions.period: the gauge period for sessions number monitoring

#### CDI

* org.apache.commons.monitoring.cdi.enabled: a boolean to activate/deactivate CDI interceptors config
