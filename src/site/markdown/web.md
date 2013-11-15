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
## Web module

Reporting module provides web listener to monitor servlet containers.

## Installation

Add sirona-web to your webapp.

## Monitor requests

Simply add the filter `org.apache.sirona.web.servlet.SironaFilter`:

<pre class="prettyprint linenums"><![CDATA[
<filter>
    <filter-name>monitoring-request</filter-name>
    <filter-class>org.apache.sirona.web.servlet.SironaFilter</filter-class>
</filter>
<filter-mapping>
    <filter-name>monitoring-request</filter-name>
    <url-pattern>/*</url-pattern>
</filter-mapping>
]]></pre>

Note: in a servlet 3 container you can simply configure `org.apache.sirona.web.monitored-urls` to the
servlet pattern you want to match. If you want to register the `MonitoringFilter` yourself just set the
init parameter `org.apache.sirona.web.activated` to false.

## Monitor sessions

Simply add the listener `org.apache.sirona.web.servlet.SironaFilter`:

<pre class="prettyprint linenums"><![CDATA[
<listener>
  <listener-class>org.apache.sirona.web.session.SironaSessionListener</listener-class>
</listener>
]]></pre>

Note: in a servlet 3 container and if `org.apache.sirona.web.activated` is not set to false it is added by default.

## Accessing results

If you installed the reporting webapp you should be able to get the result under the report tab.