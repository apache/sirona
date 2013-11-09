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
#Goal

Cassandra modules aims to provide an Apache Cassandra persistence for collector.

The benefit are:

* your data are persisted whatever happen to the instances
* your can split collector in two parts: the collector (instance getting data) and reporting instance (the gui)


# Usage

To use it simply set the `org.apache.sirona.cassandra.CassandraCollectorDataStoreFactory`:

<pre class="prettyprint linenums"><![CDATA[
org.apache.sirona.store.DataStoreFactory = org.apache.sirona.cassandra.CassandraCollectorDataStoreFactory
]]></pre>

# Configuration

Configuration is done through `org.apache.sirona.cassandra.CassandraBuilder`. here is the default configuration:

<pre class="prettyprint linenums"><![CDATA[
org.apache.sirona.cassandra.CassandraBuilder.hosts = localhost:9171
org.apache.sirona.cassandra.CassandraBuilder.cluster = sirona-cluster
org.apache.sirona.cassandra.CassandraBuilder.keyspace = sirona
org.apache.sirona.cassandra.CassandraBuilder.counterColumnFamily = counters
org.apache.sirona.cassandra.CassandraBuilder.gaugeValuesColumnFamily = gauges_values
org.apache.sirona.cassandra.CassandraBuilder.statusColumnFamily = statuses
org.apache.sirona.cassandra.CassandraBuilder.markerGaugesColumFamily = markers_gauges
org.apache.sirona.cassandra.CassandraBuilder.replicationFactor = 1
]]></pre>

# Model

## counters

```
nodeId => (counterId1 => (role, key, maxConcurrency, variance, n, max, min, sum, m2, mean)), (counterId2 => (role, key, maxConcurrency, variance, n, max, min, sum, m2, mean))
```

CounterId is a string following this convention: `counterRoleName->counterRoleUnit->counterName->marker`.

## gauges

```
gaugeId => time1=valueAtTime1, time2=valueAtTime2, ...
```

GaugeId is a string following this convention: `gaugeRoleName->gaugeRoleUnitName->marker`

## markers_gauges

```
nodeId => gaugeId1, gaugeId2, gaugeId3....
```

## statuses

```
nodeId => (validationName1 => (validationDescription1, validationStatus1)), (validationName2 => (validationDescription2, validationStatus2)), ...
```
