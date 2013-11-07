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
## JTA module

This module aims to monitor commits/rollbacks and active transaction number.

## Installation

`sirona-jta` should be added to your webapp. You need to register the jta gauges. To do it the easiest is
to add `commons-monitoring-web` to your webapp and register the listener `org.apache.sirona.web.discovery.GaugeDiscoveryListener`:

<pre class="prettyprint linenums"><![CDATA[
<web-app xmlns="http://java.sun.com/xml/ns/javaee"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://java.sun.com/xml/ns/javaee http://java.sun.com/xml/ns/javaee/web-app_2_5.xsd"
         version="2.5">
  <listener>
    <listener-class>org.apache.sirona.web.discovery.GaugeDiscoveryListener</listener-class>
  </listener>
</web-app>
]]></pre>

Note: you can register it manually using `org.apache.sirona.gauges.Gauge$LoaderHelper` or `org.apache.sirona.repositories.Repository#addGauge`.
Note 2: in a servlet 3 container it is done automatically if `org.apache.sirona.web.activated` is true (by default)

Then you need to add on the beans which can be enrolled in transactions you want to monitor the annotation
`org.apache.sirona.jta.JTAMonitored` (CDI beans) or the interceptor `org.apache.sirona.jta.JTAInterceptor`
(for EJB for instance it can be done through configuration, see ejb-jar.xml).

To configure it and not modify your code you can use the `cdi` module and just add the `jta` in the list of values:

You can configure it (without adding the `@JTAMonitored` annotation) using `org.apache.sirona.cdi.jta` key
and listing predicates as values (see CDI for details). This feature needs cdi module.

For instance:

<pre class="prettyprint linenums"><![CDATA[
org.apache.sirona.cdi.jta = prefix:org.superbiz.MyService,regex:.*Bean
]]></pre>
