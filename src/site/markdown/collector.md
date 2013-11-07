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

Default implementation only store data in memory but you can easily extend it to store data in MongoDB or whatever you want.

Note: this is on our roadmap

## For the impatient

Take a servlet container and deploy `sirona-collector-[version].war`. You'll get the reporting on `/collector` endpoint.

## Configuration

Just use the collector `DataStoreFactory`: `org.apache.sirona.collector.server.store.CollectorDataStoreFactory`.

For instance your `commons-monitoring.properties` can look like:

<pre class="prettyprint linenums"><![CDATA[
org.apache.sirona.store.DataStore = org.apache.sirona.collector.server.store.CollectorDataStoreFactory
]]></pre>

The `GaugeDataStore` can be configured through `org.apache.sirona.collector.gauge.store-class` property.
By default it uses the in memory implementation but you can set your own one if you want.

Note: if your `GaugeDataStore` has a constructor with a `String`, the marker of the store will be passed to the `GaugeDataStore`.

The `CounterDataStore` needs to be an instance of `org.apache.sirona.store.counter.CollectorCounterStore`.
By default it is in memory too but it is easily extensible to be persisted if needed.

## Installing the collector

To setup the collector you just need to configure the `DataStoreFactory` (see configuration part) and configure the
servlet `org.apache.sirona.collector.server.Collector`.

## Pushing data

The input is an array of event. Events are either gauges or counters.

Here is an array with a single gauge:

<pre class="prettyprint linenums"><![CDATA[
[
    {
        "type": "gauge",
        "time": "-",
        "data": {
            "unit": "u",
            "marker": "client1",
            "value": 0.0,
            "role": "mock"
        }
    }
]
]]></pre>

And here is an array with a single counter:

<pre class="prettyprint linenums"><![CDATA[
[
    {
        "type": "counter",
        "time": "2013-10-21T12:50:40Z",
        "data": {
            "min": 1.4,
            "unit": "ns",
            "hits": 4,
            "max": 2.9,
            "marker": "client1",
            "name": "test",
            "concurrency": 0,
            "m2": 1.4099999999999997,
            "sum": 8.2,
            "mean": 2.05,
            "role": "performances",
            "variance": 0.4699999999999999
        }
    }
]
]]></pre>
