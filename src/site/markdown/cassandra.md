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

## CQL samples

### Select your keyspace

```
cqlsh> use sirona;
```

### Show gauges by node

```
cqlsh:sirona> select * from markers_gauges;

 key          | column1                  | value
--------------+--------------------------+-------
 0x6e6f646531 |            CPU->u->node1 |    0x
 0x6e6f646531 |    Used Memory->u->node1 |    0x
```

### Show gauges values

```
 cqlsh:sirona> select * from gauges_values;

  key                        | column1       | value
 ----------------------------+---------------+--------------------
0x4350552d3e752d3e6e6f646531 | 1384245545000 | 0x3fef5c28f5c28f5c
0x4350552d3e752d3e6e6f646531 | 1384245549000 | 0x3ff23d70a3d70a3d
```

### Show counters

```
cqlsh:sirona> select * from counters;

 key          | column1                                                                             | column2        | value
--------------+-------------------------------------------------------------------------------------+----------------+----------------------------------------------------------------------------------------------------------------------------------------
 0x6e6f646531 |                      jdbc->ns->select 1 from INFORMATION_SCHEMA.SYSTEM_USERS->node1 |            key |                                           0x73656c65637420312066726f6d20494e464f524d4154494f4e5f534348454d412e53595354454d5f5553455253
 0x6e6f646531 |                      jdbc->ns->select 1 from INFORMATION_SCHEMA.SYSTEM_USERS->node1 |             m2 |                                                                                                                     0x427486ed44071b60
 0x6e6f646531 |                      jdbc->ns->select 1 from INFORMATION_SCHEMA.SYSTEM_USERS->node1 |            max |                                                                                                                     0x4133b39100000000
 0x6e6f646531 |                      jdbc->ns->select 1 from INFORMATION_SCHEMA.SYSTEM_USERS->node1 | maxConcurrency |                                                                                                                             0x00000000
 0x6e6f646531 |                      jdbc->ns->select 1 from INFORMATION_SCHEMA.SYSTEM_USERS->node1 |           mean |                                                                                                                     0x4100520e86bca1b0
 0x6e6f646531 |                      jdbc->ns->select 1 from INFORMATION_SCHEMA.SYSTEM_USERS->node1 |            min |                                                                                                                     0x40e6c38000000000
 0x6e6f646531 |                      jdbc->ns->select 1 from INFORMATION_SCHEMA.SYSTEM_USERS->node1 |              n |                                                                                                                     0x0000000000000026
 0x6e6f646531 |                      jdbc->ns->select 1 from INFORMATION_SCHEMA.SYSTEM_USERS->node1 |           role |                                                                                                                             0x6a646263
 0x6e6f646531 |                      jdbc->ns->select 1 from INFORMATION_SCHEMA.SYSTEM_USERS->node1 |            sum |                                                                                                                     0x4153617140000000
 0x6e6f646531 |                      jdbc->ns->select 1 from INFORMATION_SCHEMA.SYSTEM_USERS->node1 |       variance |                                                                                                                     0x4221c0cd33ea788a
 0x6e6f646531 |             performances->ns->org.superbiz.demo.sirona.AnEjb.scheduledMethod->node1 |            key |                                         0x6f72672e737570657262697a2e64656d6f2e7369726f6e612e416e456a622e7363686564756c65644d6574686f64
 0x6e6f646531 |             performances->ns->org.superbiz.demo.sirona.AnEjb.scheduledMethod->node1 |             m2 |                                                                                                                     0x4273955f57d89e6f
 0x6e6f646531 |             performances->ns->org.superbiz.demo.sirona.AnEjb.scheduledMethod->node1 |            max |                                                                                                                     0x41320d5400000000
 0x6e6f646531 |             performances->ns->org.superbiz.demo.sirona.AnEjb.scheduledMethod->node1 | maxConcurrency |                                                                                                                             0x00000000
 0x6e6f646531 |             performances->ns->org.superbiz.demo.sirona.AnEjb.scheduledMethod->node1 |           mean |                                                                                                                     0x40f43b50c7ce0c7c
 0x6e6f646531 |             performances->ns->org.superbiz.demo.sirona.AnEjb.scheduledMethod->node1 |            min |                                                                                                                     0x40dae5c000000000
 0x6e6f646531 |             performances->ns->org.superbiz.demo.sirona.AnEjb.scheduledMethod->node1 |              n |                                                                                                                     0x0000000000000029
 0x6e6f646531 |             performances->ns->org.superbiz.demo.sirona.AnEjb.scheduledMethod->node1 |           role |                                                                                                             0x706572666f726d616e636573
 0x6e6f646531 |             performances->ns->org.superbiz.demo.sirona.AnEjb.scheduledMethod->node1 |            sum |                                                                                                                     0x4149ebff80000000
 0x6e6f646531 |             performances->ns->org.superbiz.demo.sirona.AnEjb.scheduledMethod->node1 |       variance |                                                                                                                     0x421f5565595a9718
 0x6e6f646531 |                                   session-durations->ns->session-durations-/->node1 |            key |                                                                                               0x73657373696f6e2d6475726174696f6e732d2f
 0x6e6f646531 |                                   session-durations->ns->session-durations-/->node1 |             m2 |                                                                                                                     0x7ff8000000000000
 0x6e6f646531 |                                   session-durations->ns->session-durations-/->node1 |            max |                                                                                                                     0x7ff8000000000000
 0x6e6f646531 |                                   session-durations->ns->session-durations-/->node1 | maxConcurrency |                                                                                                                             0x00000000
 0x6e6f646531 |                                   session-durations->ns->session-durations-/->node1 |           mean |                                                                                                                     0x7ff8000000000000
 0x6e6f646531 |                                   session-durations->ns->session-durations-/->node1 |            min |                                                                                                                     0x7ff8000000000000
 0x6e6f646531 |                                   session-durations->ns->session-durations-/->node1 |              n |                                                                                                                     0x0000000000000000
 0x6e6f646531 |                                   session-durations->ns->session-durations-/->node1 |           role |                                                                                                   0x73657373696f6e2d6475726174696f6e73
 0x6e6f646531 |                                   session-durations->ns->session-durations-/->node1 |            sum |                                                                                                                     0x0000000000000000
 0x6e6f646531 |                                   session-durations->ns->session-durations-/->node1 |       variance |                                                                                                                     0x7ff8000000000000
```

### Show statuses

```
cqlsh:sirona> select * from statuses;

 key          | column1                         | column2     | value
--------------+---------------------------------+-------------+--------------------------------------------------------------------------
 0x6e6f646531 | tomee-datasource-ROOT/jdbc/demo | description | 0x76616c69646174696f6e20717565727920657865637574656420636f72726563746c79
 0x6e6f646531 | tomee-datasource-ROOT/jdbc/demo |      status |                                                                   0x4f4b
 0x6e6f646531 |             tomee-datasource-db | description | 0x76616c69646174696f6e20717565727920657865637574656420636f72726563746c79
 0x6e6f646531 |             tomee-datasource-db |      status |                                                                   0x4f4b
 0x6e6f646531 |                   validation #1 | description |                                                 0x616c6c2069732066696e65
 0x6e6f646531 |                   validation #1 |      status |                                                                   0x4f4b

```