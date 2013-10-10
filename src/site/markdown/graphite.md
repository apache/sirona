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
# Graphite

Graphite module allows to push counters and gauges to a graphite instance.

## Configuration

* `org.apache.commons.monitoring.graphite.GraphiteBuilder.address`: the graphite instance host/IP
* `org.apache.commons.monitoring.graphite.GraphiteBuilder.port`: the graphite instance port
* `org.apache.commons.monitoring.graphite.GraphiteBuilder.charset`: the charset to use with this Graphite instance

For instance your `commons-monitoring.properties` can look like:

```
org.apache.commons.monitoring.graphite.GraphiteBuilder.address = localhost
org.apache.commons.monitoring.graphite.GraphiteBuilder.port = 1234
```

## Counters

To push counters just add the `ServletContextListener` dedicated
to Graphite integration: `org.apache.commons.monitoring.graphite.lifecycle.GraphiteLifecycle`.

The available configuration is:

* `org.apache.commons.monitoring.graphite.period`: which period to use to push counters data to Graphite (default to 1mn).

Note: if you are not in a web application you'll need to use `Graphites.scheduleReport` methods.

## Gauges

To push gauges to a Graphite instance just add the Graphite `GaugeObserver`: `org.apache.commons.monitoring.graphite.GraphiteGaugeObserver`.

To do so just add in `commons-monitoring.properties`:

```
org.apache.commons.monitoring.gauges.GaugeObserver = org.apache.commons.monitoring.graphite.GraphiteGaugeObserver
```
