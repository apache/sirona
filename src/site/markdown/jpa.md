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
## JPA module

This module aims to monitor JPA invocations.

## Installation

To use this module and get execution time of queries creation (`createNamedQuery`, ...) set
as JPA provider `org.apache.sirona.jpa.MonitoringPersistence`.

If you have in your environment a single "real" JPA provider it should be found automatically but if that's not the
case of if you want to force the implementation set the property `org.apache.sirona.jpa.provider`
to the real implementation you want. For instance:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<persistence version="2.0"
             xmlns="http://java.sun.com/xml/ns/persistence"
             xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
             xsi:schemaLocation="http://java.sun.com/xml/ns/persistence
                       http://java.sun.com/xml/ns/persistence/persistence_2_0.xsd">
  <persistence-unit name="my-unit">
    ...
    <properties>
      <property name="org.apache.sirona.jpa.provider"
                value="org.apache.openjpa.persistence.PersistenceProviderImpl"/>
      ...
    </properties>
  </persistence-unit>
</persistence>
```

Note: it works for JTA transaction-type units too.
