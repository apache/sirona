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
# Cube

Cube module allows to push counters and gauges to a Square Cube instance (see [Cube github wiki](https://github.com/square/cube/wiki)).

## Configuration

org.apache.sirona.store.DataStore = org.apache.sirona.cube.CubeDataStore
org.apache.sirona.cube.period = 100
org.apache.sirona.cube.CubeBuilder.collector = http://localhost:1234/collector/1.0/event/put
* `org.apache.sirona.cube.CubeBuilder.collector`: the cube event collector address (`http://xxx:1234/collector/1.0/event/put` for instance)
* `org.apache.sirona.cube.CubeBuilder.proxyHost`: optionally a proxy host
* `org.apache.sirona.cube.CubeBuilder.proxyPort`: optionally a proxy port

For instance your `commons-monitoring.properties` can look like:

```
org.apache.sirona.cube.CubeBuilder.collector = http://localhost:1234/collector/1.0/event/put
```

## DataStore

To push metrics (Gauges + Counters) to Graphite you can use the dedicated `DataStore`: `org.apache.sirona.cube.CubeDataStore`.

Simply add to `commons-monitoring.properties` the line:

```
org.apache.sirona.store.DataStore = org.apache.sirona.cube.CubeDataStore
```

### Counters

You can also configure the period used to flush counters values:

* `org.apache.sirona.cube.period`: which period to use to push counters data to Graphite (default to 1mn).

## Limitations (ATM)

When using CubeDataStore you cannot retrieve locally gauges values (you are expected to use Cube for it).

