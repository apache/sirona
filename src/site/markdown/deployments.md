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
# Solutions

Apache Sirona supports several deployments. Basically you can:

* deploy everything locally (agent, reporting)
* deploy agent in "client" JVMs and a remote collector ("server")
* (not yet available - needs a custom persistent store) deploy client JVMs and twi servers: one for the collection and one for the reporting (GUI)
* in agent/collector mode you can either use agent push mecanism or collector pulling

## Everything locally

TBD

<pre class="prettyprint linenums"><![CDATA[
<dependency>
  <groupId>org.apache.sirona</groupId>
  <artifactId>sirona-core</artifactId>
  <version>${sirona.version}</version>
</dependency>
<dependency>
  <groupId>org.apache.sirona</groupId>
  <artifactId>sirona-jdbc</artifactId>
  <version>${sirona.version}</version>
</dependency>
<dependency>
  <groupId>org.apache.sirona</groupId>
  <artifactId>sirona-jpa</artifactId>
  <version>${sirona.version}</version>
</dependency>
<dependency>
  <groupId>org.apache.sirona</groupId>
  <artifactId>sirona-cdi</artifactId>
  <version>${sirona.version}</version>
</dependency>
<dependency>
  <groupId>org.apache.sirona</groupId>
  <artifactId>sirona-jta</artifactId>
  <version>${sirona.version}</version>
</dependency>
<dependency>
  <groupId>org.apache.sirona</groupId>
  <artifactId>sirona-web</artifactId>
  <version>${sirona.version}</version>
</dependency>
<dependency>
  <groupId>org.apache.sirona</groupId>
  <artifactId>sirona-cube</artifactId>
  <version>${sirona.version}</version>
</dependency>
<dependency>
  <groupId>org.apache.sirona</groupId>
  <artifactId>sirona-reporting</artifactId>
  <version>${sirona.version}</version>
  <classifier>classes</classifier>
</dependency>
...
]]></pre>


## Agent/Collector
### Push mode

TDB (cube datastore + collector)

### Pull mode

TDB (pull datastore + collector)
