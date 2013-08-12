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

Add commons-monitoring-web to your webapp.

## Monitor requests

Simply add the filter `org.apache.commons.monitoring.web.servlet.MonitoringFilter`:

    <filter>
        <filter-name>monitoring-request</filter-name>
        <filter-class>org.apache.commons.monitoring.web.servlet.MonitoringFilter</filter-class>
    </filter>
    <filter-mapping>
        <filter-name>monitoring-request</filter-name>
        <url-pattern>/*</url-pattern>
    </filter-mapping>

## Monitor sessions

Simply add the listener `org.apache.commons.monitoring.web.servlet.MonitoringFilter`:

    <listener>
      <listener-class>org.apache.commons.monitoring.web.servlet.MonitoringFilter</listener-class>
    </listener>


## Accessing results

If you installed the reporting webapp you should be able to get the result under the report tab.