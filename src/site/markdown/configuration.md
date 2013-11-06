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
Configuration relies on `sirona.properties` file which should be found in the commons-monitoring-core classloader.

Note: you can change the file name using `org.apache.sirona.configuration` system property.

### Utilities

`org.apache.sirona.configuration.Configuration` has several utility methods to get
int, boolean... from the properties file. You can reuse it in your monitoring extensions if you want.

Another interesting usage of Configuration class is to be a basic lifecycle handling of your objects.
Using `org.apache.sirona.configuration.Configuration.newInstance` method you can
decorate your class methods with `org.apache.sirona.configuration.Configuration.Created`
and `org.apache.sirona.configuration.Configuration.Destroying` to get lifecycle hooks.

`Destroying` is called when the monitoring is stopped. Generally since commons-monitoring-core is deployed in the
container or JVM classloader it is with the JVM but sometimes you can deploy it in your application. In this case
you'll need to either configure the `javax.servlet.ServletContextListener`
`org.apache.sirona.web.lifecycle.SironaLifecycle` from reporting module
or to call manually `Configuration.shutdown()` method.

### Main configuration keys (by module)

#### Core

* org.apache.sirona.configuration: the configuration file path if not using the default
* org.apache.sirona.shutdown.hook: boolean, true by default. Should be set to false when deploying commons-monitoring-core in an application (see Utilities part).
* org.apache.sirona.gauge.max-size: int, 100 by default. Number of gauge measures to keep in memory when not persistent.
* org.apache.sirona.gauge.memory.period: int, 4000 (ms) by default. Period for memory gauge.
* org.apache.sirona.gauge.cpu.period: int, 4000 (ms) by default. Period for CPU gauge.
* org.apache.sirona.store.DataStoreFactory: qualified class name, default `org.apache.sirona.store.DefaultDataStoreFactory`. DataStoreFactory to use.
* org.apache.sirona.repositories.Repository: qualified class name, default `org.apache.sirona.repositories.DefaultRepository`. Repository to use.
* org.apache.sirona.core.gauge.activated: a boolean to deactivate cpu/memory gauges
* org.apache.sirona.\<name>.period: the period to use to flush counters for a batch data store (like graphite one)
* org.apache.sirona.periodic.status.period: the period to use for status reporting. Note: when using another reporter (cube typically) you'll need to replace `periodic` by the specific name of the reporter (`cube`). Note too that `period` is optional to allow to share the same period between all stores.
* org.apache.sirona.gauges.GaugeManager: the gauge manager to use, by default for agents it uses timers to store/push metrics but you can set `org.apache.sirona.gauges.NoopGaugeManager` to prevent it.

#### Reporting

* org.apache.commons.proxy.ProxyFactory: qualified class name. ProxyFactory to use for client aop.
* [plugin name].activated: boolean, true by default. Should the plugin referenced by [plugin.name] be used.
* org.apache.sirona.jmx.method.allowed: boolean, true by default. Are JMX method invocation allowed.
* org.apache.sirona.gauge.csv.separator: char, ';' by default. CSV separator for CSV report.
* org.apache.sirona.gauge.jta.period: jta gauge period
* org.apache.sirona.gauge.memory.period: memory gauge period
* org.apache.sirona.gauge.cpu.period: cpu gauge period
* org.apache.sirona.reporting.activated: if auto deployment of reporting module is activated
* org.apache.sirona.reporting.mapping: the mapping of monitoring GUI

#### Web

* org.apache.sirona.web.activated: if auto deployment of web module is activated
* org.apache.sirona.web.monitored-urls: the mapping of monitored urls
* org.apache.sirona.web.gauge.sessions.period: the gauge period for sessions number monitoring
* org.apache.sirona.web.gauge.status.period: when status monitoring is activated the period for status gauges
* org.apache.sirona.web.monitored-statuses: the comma separated list of monitored statuses (if not a default list is used)

#### CDI

* org.apache.sirona.cdi.enabled: a boolean to activate/deactivate CDI interceptors config
* org.apache.sirona.cdi.performance: list of intercepted beans for performances (prefix:org.superbiz, regex:.*Service...)
* org.apache.sirona.cdi.jta: list of intercepted beans for JTA

# TomEE

* org.apache.sirona.tomee.gauges.activated: a boolean to deactivate tomee guages (stateless pool stat)
* org.apache.sirona.tomee.validations.activated: a boolean to deactivate tomee validations (datasource validation by validation query)

# Pull

* org.apache.sirona.agent.pull.mapping: the servlet mapping, default to `/sirona/pull`
