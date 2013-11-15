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

Be able to monitor an hazelcast cluster.

# Usage

First you need to define a list of cluster:

```
org.apache.sirona.hazelcast.clusters = cluster1, cluster2
```

For each cluster you can define if you want sirona to contact the cluster as a client or a member (client is recommanded):

```
org.apache.sirona.hazelcast.cluster1.client = true
org.apache.sirona.hazelcast.cluster2.client = false
```

Note: next lines explain how to configure a hazelcast client in sirona.properties but since sirona reuses `Xml*Builder` of
hazelcast having a hazelcast config file is enough too.

If a client you can configure it this way:

```
org.apache.sirona.hazelcast.cluster1.addresses = ....
org.apache.sirona.hazelcast.cluster1.credentials = user:password
```

For a member you just need to add a hazelcast.xml in the classloader of sirona hazelcast agent.

Note: in push mode you can configure gauge period through `org.apache.sirona.hazelcast.gauge.members.period` property.

# Data

Sirona hazelcast agent will maintain a gauge representing the member number in a list of clusters.

