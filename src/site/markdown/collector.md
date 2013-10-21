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
# Collector

Collector modules aims to aggregate data from multiple instances.

## Features

Default implementation only store data in memory.

## Configuration

Just use the collector `DataStoreFactory`: `org.apache.commons.monitoring.collector.server.store.CollectorDataStoreFactory`.

For instance your `commons-monitoring.properties` can look like:

```
org.apache.commons.monitoring.store.DataStore = org.apache.commons.monitoring.collector.server.store.CollectorDataStoreFactory
```

The `GaugeDataStore` can be configured through `org.apache.commons.monitoring.collector.gauge.store-class` property.
By default it uses the in memory implementation but you can set your own one if you want.

Note: if your `GaugeDataStore` has a constructor with a `String`, the marker of the store will be passed to the `GaugeDataStore`.

The `CounterDataStore` needs to be an instance of `org.apache.commons.monitoring.collector.server.store.counter.CollectorCounterStore`.
By default it is in memory too but it is easily extensible to be persisted if needed.

## Installing the collector

To setup the collector you just need to configure the `DataStoreFactory` (see configuration part) and configure the
servlet `org.apache.commons.monitoring.collector.server.Collector`.

## Pushing data

The input is an array of event. Events are either gauges or counters.

Here is an array with a single gauge:

```json
[{"type": "gauge","time": "-","data": {"unit":"u","marker":"client1","value":0.0,"role":"mock"}}]
```

And here is an array with a single counter:

```json
[{"type": "counter","time": "2013-10-21T12:50:40Z","data": {"min":1.4,"unit":"ns","hits":4,"max":2.9,"marker":"client1","name":"test","concurrency":0,"m2":1.4099999999999997,"sum":8.2,"mean":2.05,"role":"performances","variance":0.4699999999999999}}]
```
