/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.sirona.jdbc;

import org.apache.sirona.Role;
import org.apache.sirona.counters.Counter;
import org.apache.sirona.repositories.Repository;
import org.hsqldb.jdbcDriver;
import org.junit.BeforeClass;
import org.junit.Test;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Statement;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class HsqlDBTest {
    @BeforeClass
    public static void init() {
        SironaDriver.load();
    }

    @Test
    public void driverMonitoring() throws Exception {
        final Connection connection = DriverManager.getConnection("jdbc:sirona:hsqldb:mem:monitoring?delegateDriver=" + jdbcDriver.class.getName(), "SA", "");
        assertNotNull(connection);
        assertTrue(Proxy.isProxyClass(connection.getClass()));
        final InvocationHandler handler = Proxy.getInvocationHandler(connection);
        assertThat(handler, instanceOf(MonitoredConnection.class));

        final String create = "CREATE TABLE Address (Nr INTEGER, Name VARCHAR(128));";
        final Statement statement = connection.createStatement();
        statement.execute(create);
        assertEquals(1, Repository.INSTANCE.getCounter(new Counter.Key(Role.JDBC, create)).getMaxConcurrency(), 0.);

        final String insert = "INSERT INTO Address (Nr, Name) VALUES(1, 'foo')";
        final PreparedStatement preparedStatement = connection.prepareStatement(insert);
        preparedStatement.execute();
        assertEquals(1, Repository.INSTANCE.getCounter(new Counter.Key(Role.JDBC, insert)).getMaxConcurrency(), 0.);
        preparedStatement.execute();
        assertEquals(1, Repository.INSTANCE.getCounter(new Counter.Key(Role.JDBC, insert)).getMaxConcurrency(), 0.);
        assertEquals(2, Repository.INSTANCE.getCounter(new Counter.Key(Role.JDBC, insert)).getHits(), 0.);
    }
}
