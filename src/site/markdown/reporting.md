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
## Reporting module

Reporting module provides a lightweight GUI to visualize monitoring information.

## Installation
### The webapp

commons-monitoring-reporting is available as a webapp (.war) so you can just drop it in your servlet container.

Note 1: commons-monitoring-core is not provided and should be in the container.
Note 2: if you use commons-monitoring-jdbc put it in the container too.

### Embeded in your web application

Just adding commons-monitoring-reporting jar (classifier `classes` if you use maven) in your application
you can embed it. You'll need to update your web.xml to declare the monitoring filter:

    <web-app xmlns="http://java.sun.com/xml/ns/javaee"
             xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
             xsi:schemaLocation="http://java.sun.com/xml/ns/javaee http://java.sun.com/xml/ns/javaee/web-app_2_5.xsd"
             version="2.5">

      <listener>
        <listener-class>org.apache.commons.monitoring.reporting.web.listener.CleanupListener</listener-class>
      </listener>

      <filter>
        <filter-name>Monitoring</filter-name>
        <filter-class>org.apache.commons.monitoring.reporting.web.MonitoringController</filter-class>
        <init-param> <!-- should match your filter mapping base -->
          <param-name>monitoring-mapping</param-name>
          <param-value>/monitoring/</param-value>
        </init-param>
      </filter>

      <filter-mapping>
        <filter-name>Monitoring</filter-name>
        <url-pattern>/monitoring/*</url-pattern>
      </filter-mapping>

    </web-app>

## Usage

Once started you'll arrive on the home page which should look like:

![Home](images/gui/home.png)

By default you have three activated plugins:

* Report: view to work with counter data
* JMX: access to MBeans (read attribute, invoke operations)
* JVM: basic measure of CPU and used heap memory

### Report

![Report](images/gui/report.png)

### JMX

![JMX](images/gui/mbean-attributes.png)
![JMX](images/gui/mbean-operations.png)

### JVM

![JVM](images/gui/jvm.png)
