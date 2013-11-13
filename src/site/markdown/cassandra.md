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
org.apache.sirona.cassandra.CassandraBuilder.hosts = localhost:9160
org.apache.sirona.cassandra.CassandraBuilder.cluster = sirona-cluster
org.apache.sirona.cassandra.CassandraBuilder.keyspace = sirona
org.apache.sirona.cassandra.CassandraBuilder.counterColumnFamily = counters
org.apache.sirona.cassandra.CassandraBuilder.gaugeValuesColumnFamily = gauges_values
org.apache.sirona.cassandra.CassandraBuilder.statusColumnFamily = statuses
org.apache.sirona.cassandra.CassandraBuilder.markerGaugesColumFamily = markers_gauges
org.apache.sirona.cassandra.CassandraBuilder.markerCountersColumFamily = markers_counters
org.apache.sirona.cassandra.CassandraBuilder.markerStatusesColumFamily = markers_statuses
org.apache.sirona.cassandra.CassandraBuilder.replicationFactor = 1
org.apache.sirona.cassandra.CassandraBuilder.maxActive = 50
org.apache.sirona.cassandra.CassandraBuilder.writeConsistencyLevel = QUORUM
org.apache.sirona.cassandra.CassandraBuilder.readConsistencyLevel = QUORUM
]]></pre>

# Model

## counters

```
counterId1 => (role, key, maxConcurrency, variance, n, max, min, sum, m2, mean)
```

CounterId is a string following this convention: `counterRoleName->counterRoleUnit->counterName->marker`.

## markers_counters

```
nodeId => counterId1, counterId2, counterId3....
```

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
validationId => (validationDescription1, validationStatus1)
```

Note: validationId follows this convention: `validationName->marker`

## markers_statuses

```
nodeId => validationId1, validationId2, validationId3....
```

## CQL samples

### Select your keyspace

```
cqlsh> use sirona;
```

### Show gauges by node

```
[default@sirona] list counters_values;
Using default limit of 100
Using default cell limit of 100
-------------------
RowKey: 73657373696f6e2d6475726174696f6e732d3e6e732d3e73657373696f6e2d6475726174696f6e732d2f2d3e7562756e7475
=> (name=key, value=73657373696f6e2d6475726174696f6e732d2f, timestamp=1384371568101001)
=> (name=m2, value=7ff8000000000000, timestamp=1384371568101008)
=> (name=max, value=7ff8000000000000, timestamp=1384371568101005)
=> (name=maxConcurrency, value=00000000, timestamp=1384371568101002)
=> (name=mean, value=7ff8000000000000, timestamp=1384371568101009)
=> (name=min, value=7ff8000000000000, timestamp=1384371568101006)
=> (name=n, value=0000000000000000, timestamp=1384371568101004)
=> (name=role, value=73657373696f6e2d6475726174696f6e73, timestamp=1384371568101000)
=> (name=sum, value=0000000000000000, timestamp=1384371568101007)
=> (name=variance, value=7ff8000000000000, timestamp=1384371568101003)
-------------------
RowKey: 73657373696f6e2d6475726174696f6e732d3e6e732d3e73657373696f6e2d6475726174696f6e732d2f2d3e6e6f646531
=> (name=key, value=73657373696f6e2d6475726174696f6e732d2f, timestamp=1384371580602001)
=> (name=m2, value=7ff8000000000000, timestamp=1384371580603004)
=> (name=max, value=7ff8000000000000, timestamp=1384371580603001)
=> (name=maxConcurrency, value=00000000, timestamp=1384371580602002)
=> (name=mean, value=7ff8000000000000, timestamp=1384371580603005)
=> (name=min, value=7ff8000000000000, timestamp=1384371580603002)
=> (name=n, value=0000000000000000, timestamp=1384371580603000)
=> (name=role, value=73657373696f6e2d6475726174696f6e73, timestamp=1384371580602000)
=> (name=sum, value=0000000000000000, timestamp=1384371580603003)
=> (name=variance, value=7ff8000000000000, timestamp=1384371580602003)
-------------------
RowKey: 706572666f726d616e6365732d3e6e732d3e6f72672e737570657262697a2e64656d6f2e7369726f6e612e416e456a622e7363686564756c65644d6574686f642d3e7562756e7475
=> (name=key, value=6f72672e737570657262697a2e64656d6f2e7369726f6e612e416e456a622e7363686564756c65644d6574686f64, timestamp=1384371568098001)
=> (name=m2, value=42623703388470f1, timestamp=1384371568099000)
=> (name=max, value=41294b4400000000, timestamp=1384371568098005)
=> (name=maxConcurrency, value=00000000, timestamp=1384371568098002)
=> (name=mean, value=40f13b14b4b4b4b5, timestamp=1384371568099001)
=> (name=min, value=40c0b38000000000, timestamp=1384371568098006)
=> (name=n, value=0000000000000011, timestamp=1384371568098004)
=> (name=role, value=706572666f726d616e636573, timestamp=1384371568098000)
=> (name=sum, value=41324ec600000000, timestamp=1384371568098007)
=> (name=variance, value=42223703388470f1, timestamp=1384371568098003)
-------------------
RowKey: 6a6462632d3e6e732d3e6a6462633a7369726f6e613a6873716c64623a6d656d3a64623f64656c65676174654472697665723d6f72672e6873716c64622e6a6462634472697665722d3e7562756e7475
=> (name=key, value=6a6462633a7369726f6e613a6873716c64623a6d656d3a64623f64656c65676174654472697665723d6f72672e6873716c64622e6a646263447269766572, timestamp=1384371568095001)
=> (name=m2, value=7ff8000000000000, timestamp=1384371568096001)
=> (name=max, value=7ff8000000000000, timestamp=1384371568095005)
=> (name=maxConcurrency, value=00000001, timestamp=1384371568095002)
=> (name=mean, value=7ff8000000000000, timestamp=1384371568096002)
=> (name=min, value=7ff8000000000000, timestamp=1384371568095006)
=> (name=n, value=0000000000000000, timestamp=1384371568095004)
=> (name=role, value=6a646263, timestamp=1384371568095000)
=> (name=sum, value=0000000000000000, timestamp=1384371568096000)
=> (name=variance, value=7ff8000000000000, timestamp=1384371568095003)
-------------------
RowKey: 6a6462632d3e6e732d3e6a6462633a7369726f6e613a6873716c64623a6d656d3a64623f64656c65676174654472697665723d6f72672e6873716c64622e6a6462634472697665722d3e6e6f646531
=> (name=key, value=6a6462633a7369726f6e613a6873716c64623a6d656d3a64623f64656c65676174654472697665723d6f72672e6873716c64622e6a646263447269766572, timestamp=1384371580599001)
=> (name=m2, value=7ff8000000000000, timestamp=1384371580599008)
=> (name=max, value=7ff8000000000000, timestamp=1384371580599005)
=> (name=maxConcurrency, value=00000001, timestamp=1384371580599002)
=> (name=mean, value=7ff8000000000000, timestamp=1384371580599009)
=> (name=min, value=7ff8000000000000, timestamp=1384371580599006)
=> (name=n, value=0000000000000000, timestamp=1384371580599004)
=> (name=role, value=6a646263, timestamp=1384371580599000)
=> (name=sum, value=0000000000000000, timestamp=1384371580599007)
=> (name=variance, value=7ff8000000000000, timestamp=1384371580599003)
-------------------
RowKey: 6a6462632d3e6e732d3e73656c65637420312066726f6d20494e464f524d4154494f4e5f534348454d412e53595354454d5f55534552532d3e6e6f646531
=> (name=key, value=73656c65637420312066726f6d20494e464f524d4154494f4e5f534348454d412e53595354454d5f5553455253, timestamp=1384371580596001)
=> (name=m2, value=42516f85f2e1e36d, timestamp=1384371580597005)
=> (name=max, value=41227d8600000000, timestamp=1384371580597002)
=> (name=maxConcurrency, value=00000000, timestamp=1384371580596002)
=> (name=mean, value=40f70ce292492491, timestamp=1384371580597006)
=> (name=min, value=40e696c000000000, timestamp=1384371580597003)
=> (name=n, value=0000000000000038, timestamp=1384371580597001)
=> (name=role, value=6a646263, timestamp=1384371580596000)
=> (name=sum, value=41542b4640000000, timestamp=1384371580597004)
=> (name=variance, value=41f449eafeb311f3, timestamp=1384371580597000)
-------------------
RowKey: 6a6462632d3e6e732d3e73656c65637420312066726f6d20494e464f524d4154494f4e5f534348454d412e53595354454d5f55534552532d3e7562756e7475
=> (name=key, value=73656c65637420312066726f6d20494e464f524d4154494f4e5f534348454d412e53595354454d5f5553455253, timestamp=1384371568092001)
=> (name=m2, value=42c2b745705bd777, timestamp=1384371568092008)
=> (name=max, value=4159dcba80000000, timestamp=1384371568092005)
=> (name=maxConcurrency, value=00000000, timestamp=1384371568092002)
=> (name=mean, value=4121c69088888889, timestamp=1384371568092009)
=> (name=min, value=40f6fbe000000000, timestamp=1384371568092006)
=> (name=n, value=000000000000000f, timestamp=1384371568092004)
=> (name=role, value=6a646263, timestamp=1384371568092000)
=> (name=sum, value=4160aa2780000000, timestamp=1384371568092007)
=> (name=variance, value=428563bd12b21ad1, timestamp=1384371568092003)
-------------------
RowKey: 706572666f726d616e6365732d3e6e732d3e6f72672e737570657262697a2e64656d6f2e7369726f6e612e416e456a622e7363686564756c65644d6574686f642d3e6e6f646531
=> (name=key, value=6f72672e737570657262697a2e64656d6f2e7369726f6e612e416e456a622e7363686564756c65644d6574686f64, timestamp=1384371580600001)
=> (name=m2, value=426b5e97084323d9, timestamp=1384371580601004)
=> (name=max, value=412e7ec600000000, timestamp=1384371580601001)
=> (name=maxConcurrency, value=00000000, timestamp=1384371580600002)
=> (name=mean, value=40efa537b9611a7b, timestamp=1384371580601005)
=> (name=min, value=40d8278000000000, timestamp=1384371580601002)
=> (name=n, value=000000000000003a, timestamp=1384371580601000)
=> (name=role, value=706572666f726d616e636573, timestamp=1384371580600000)
=> (name=sum, value=414cadba80000000, timestamp=1384371580601003)
=> (name=variance, value=420ebb0c6319fb56, timestamp=1384371580600003)

8 Rows Returned.
Elapsed time: 71 msec(s).
[default@sirona] list markers_counters;
Using default limit of 100
Using default cell limit of 100
-------------------
RowKey: 6e6f646531
=> (name=jdbc->ns->jdbc:sirona:hsqldb:mem:db?delegateDriver=org.hsqldb.jdbcDriver->node1, value=, timestamp=1384371580599010)
=> (name=jdbc->ns->select 1 from INFORMATION_SCHEMA.SYSTEM_USERS->node1, value=, timestamp=1384371580597007)
=> (name=performances->ns->org.superbiz.demo.sirona.AnEjb.scheduledMethod->node1, value=, timestamp=1384371580601006)
=> (name=session-durations->ns->session-durations-/->node1, value=, timestamp=1384371580603006)
-------------------
RowKey: 7562756e7475
=> (name=jdbc->ns->jdbc:sirona:hsqldb:mem:db?delegateDriver=org.hsqldb.jdbcDriver->ubuntu, value=, timestamp=1384371568096003)
=> (name=jdbc->ns->select 1 from INFORMATION_SCHEMA.SYSTEM_USERS->ubuntu, value=, timestamp=1384371568092010)
=> (name=performances->ns->org.superbiz.demo.sirona.AnEjb.scheduledMethod->ubuntu, value=, timestamp=1384371568099002)
=> (name=session-durations->ns->session-durations-/->ubuntu, value=, timestamp=1384371568101010)

2 Rows Returned.
Elapsed time: 29 msec(s).
[default@sirona] list gauges_values;
Using default limit of 100
Using default cell limit of 100
-------------------
RowKey: 2f2d485454502d3330312d3e752d3e6e6f646531
=> (name=1384371524000, value=0000000000000000, timestamp=1384371524377000)
=> (name=1384371528000, value=0000000000000000, timestamp=1384371528342000)
=> (name=1384371532000, value=0000000000000000, timestamp=1384371532335001)
=> (name=1384371536000, value=0000000000000000, timestamp=1384371536327001)
=> (name=1384371540000, value=0000000000000000, timestamp=1384371540337000)
=> (name=1384371544000, value=0000000000000000, timestamp=1384371544323000)
=> (name=1384371548000, value=0000000000000000, timestamp=1384371548333000)
=> (name=1384371552000, value=0000000000000000, timestamp=1384371552334000)
=> (name=1384371556000, value=0000000000000000, timestamp=1384371556337000)
=> (name=1384371560000, value=0000000000000000, timestamp=1384371560337000)
=> (name=1384371564000, value=0000000000000000, timestamp=1384371564334000)
=> (name=1384371568000, value=0000000000000000, timestamp=1384371568325000)
=> (name=1384371572000, value=0000000000000000, timestamp=1384371572338000)
=> (name=1384371576000, value=0000000000000000, timestamp=1384371576332000)
=> (name=1384371580000, value=0000000000000000, timestamp=1384371580329000)
-------------------
RowKey: 4350552d3e752d3e6e6f646531
=> (name=1384371523000, value=3ffd1eb851eb851f, timestamp=1384371524379000)
=> (name=1384371527000, value=3ffc000000000000, timestamp=1384371527656000)
=> (name=1384371531000, value=3ff9c28f5c28f5c3, timestamp=1384371531656000)
=> (name=1384371535000, value=3ff9c28f5c28f5c3, timestamp=1384371535656000)
=> (name=1384371539000, value=3ff7ae147ae147ae, timestamp=1384371539658001)
=> (name=1384371543000, value=3ff9c28f5c28f5c3, timestamp=1384371543655000)
=> (name=1384371547000, value=3ffa3d70a3d70a3d, timestamp=1384371547656000)
=> (name=1384371551000, value=3ff970a3d70a3d71, timestamp=1384371551656001)
=> (name=1384371555000, value=3ff970a3d70a3d71, timestamp=1384371555656001)
=> (name=1384371559000, value=3ff75c28f5c28f5c, timestamp=1384371559656000)
=> (name=1384371563000, value=3ff8000000000000, timestamp=1384371563655000)
=> (name=1384371567000, value=3ff6147ae147ae14, timestamp=1384371567654000)
=> (name=1384371571000, value=3ff599999999999a, timestamp=1384371571655000)
=> (name=1384371575000, value=3ff599999999999a, timestamp=1384371575655000)
=> (name=1384371579000, value=3ff51eb851eb851f, timestamp=1384371579656000)

32 Rows Returned.
Elapsed time: 168 msec(s).
[default@sirona] list markers_gauges;
Using default limit of 100
Using default cell limit of 100
-------------------
RowKey: 6e6f646531
=> (name=/-HTTP-200->u->node1, value=, timestamp=1384371580326000)
=> (name=/-HTTP-201->u->node1, value=, timestamp=1384371580327000)
=> (name=/-HTTP-204->u->node1, value=, timestamp=1384371580331000)
=> (name=/-HTTP-301->u->node1, value=, timestamp=1384371580322001)
=> (name=/-HTTP-302->u->node1, value=, timestamp=1384371580321000)
=> (name=/-HTTP-400->u->node1, value=, timestamp=1384371580328000)
=> (name=/-HTTP-403->u->node1, value=, timestamp=1384371580323000)
=> (name=/-HTTP-404->u->node1, value=, timestamp=1384371580330000)
=> (name=/-HTTP-500->u->node1, value=, timestamp=1384371580332000)
=> (name=CPU->u->node1, value=, timestamp=1384371579654000)
=> (name=Used Memory->u->node1, value=, timestamp=1384371579656001)
=> (name=Used Non Heap Memory->u->node1, value=, timestamp=1384371579659000)
=> (name=jta-active->u->node1, value=, timestamp=1384371580339000)
=> (name=jta-commited->u->node1, value=, timestamp=1384371580339001)
=> (name=jta-rollbacked->u->node1, value=, timestamp=1384371580335000)
=> (name=sessions-/->u->node1, value=, timestamp=1384371580319000)
-------------------
RowKey: 7562756e7475
=> (name=/-HTTP-200->u->ubuntu, value=, timestamp=1384371568559000)
=> (name=/-HTTP-201->u->ubuntu, value=, timestamp=1384371568547000)
=> (name=/-HTTP-204->u->ubuntu, value=, timestamp=1384371568567000)
=> (name=/-HTTP-301->u->ubuntu, value=, timestamp=1384371568545002)
=> (name=/-HTTP-302->u->ubuntu, value=, timestamp=1384371568545000)
=> (name=/-HTTP-400->u->ubuntu, value=, timestamp=1384371568561002)
=> (name=/-HTTP-403->u->ubuntu, value=, timestamp=1384371568545001)
=> (name=/-HTTP-404->u->ubuntu, value=, timestamp=1384371568553000)
=> (name=/-HTTP-500->u->ubuntu, value=, timestamp=1384371568559001)
=> (name=CPU->u->ubuntu, value=, timestamp=1384371568140000)
=> (name=Used Memory->u->ubuntu, value=, timestamp=1384371568145000)
=> (name=Used Non Heap Memory->u->ubuntu, value=, timestamp=1384371568147000)
=> (name=jta-active->u->ubuntu, value=, timestamp=1384371568576000)
=> (name=jta-commited->u->ubuntu, value=, timestamp=1384371568573000)
=> (name=jta-rollbacked->u->ubuntu, value=, timestamp=1384371568574000)
=> (name=sessions-/->u->ubuntu, value=, timestamp=1384371568541000)

2 Rows Returned.
Elapsed time: 35 msec(s).
[default@sirona] list statuses_values;
Using default limit of 100
Using default cell limit of 100
-------------------
RowKey: 7562756e74752d3e746f6d65652d64617461736f757263652d524f4f542f6a6462632f64656d6f
=> (name=description, value=76616c69646174696f6e20717565727920657865637574656420636f72726563746c79, timestamp=1384371568128010)
=> (name=name, value=746f6d65652d64617461736f757263652d524f4f542f6a6462632f64656d6f, timestamp=1384371568128009)
=> (name=status, value=4f4b, timestamp=1384371568128011)
-------------------
RowKey: 7562756e74752d3e76616c69646174696f6e202331
=> (name=description, value=616c6c2069732066696e65, timestamp=1384371568128006)
=> (name=name, value=76616c69646174696f6e202331, timestamp=1384371568128005)
=> (name=status, value=4f4b, timestamp=1384371568128007)
-------------------
RowKey: 6e6f6465312d3e746f6d65652d64617461736f757263652d6462
=> (name=description, value=76616c69646174696f6e20717565727920657865637574656420636f72726563746c79, timestamp=1384371580630000)
=> (name=name, value=746f6d65652d64617461736f757263652d6462, timestamp=1384371580629009)
=> (name=status, value=4f4b, timestamp=1384371580630001)
-------------------
RowKey: 6e6f6465312d3e746f6d65652d64617461736f757263652d524f4f542f6a6462632f64656d6f
=> (name=description, value=76616c69646174696f6e20717565727920657865637574656420636f72726563746c79, timestamp=1384371580630004)
=> (name=name, value=746f6d65652d64617461736f757263652d524f4f542f6a6462632f64656d6f, timestamp=1384371580630003)
=> (name=status, value=4f4b, timestamp=1384371580630005)
-------------------
RowKey: 6e6f6465312d3e76616c69646174696f6e202331
=> (name=description, value=616c6c2069732066696e65, timestamp=1384371580629006)
=> (name=name, value=76616c69646174696f6e202331, timestamp=1384371580629005)
=> (name=status, value=4f4b, timestamp=1384371580629007)

5 Rows Returned.
Elapsed time: 33 msec(s).
[default@sirona] list markers_statuses;
Using default limit of 100
Using default cell limit of 100
-------------------
RowKey: 6e6f646531
=> (name=node1->tomee-datasource-ROOT/jdbc/demo, value=, timestamp=1384371580630002)
=> (name=node1->tomee-datasource-db, value=, timestamp=1384371580629008)
=> (name=node1->validation #1, value=, timestamp=1384371580629004)
-------------------
RowKey: 7562756e7475
=> (name=ubuntu->tomee-datasource-ROOT/jdbc/demo, value=, timestamp=1384371568128008)
=> (name=ubuntu->validation #1, value=, timestamp=1384371568128004)

2 Rows Returned.
Elapsed time: 29 msec(s).
```