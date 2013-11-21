/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.sirona.tomee.agent;

import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.Configuration;
import org.apache.openejb.testing.Module;
import org.apache.sirona.configuration.ioc.IoCs;
import org.apache.sirona.repositories.Repository;
import org.apache.sirona.status.NodeStatus;
import org.apache.sirona.status.Status;
import org.apache.sirona.status.ValidationResult;
import org.apache.sirona.store.status.NodeStatusDataStore;
import org.apache.sirona.store.status.PeriodicNodeStatusDataStore;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Properties;

import static org.junit.Assert.assertEquals;

@RunWith(ApplicationComposer.class)
public class ValidationsTest {
    @Resource
    private DataSource ds;

    @Module
    public EjbJar bean() {
        return new EjbJar();
    }

    @Configuration
    public Properties configuration() {
        return new Properties() {{
            setProperty("db", "new://Resource?type=DataSource");
            setProperty("db.InitialSize", "3");
        }};
    }

    @Before
    public void reset() throws SQLException {
        // touch the datasource to force the pool to get initialized
        // otherwise first validation is slow and we miss it
        ds.getConnection().close();

        Repository.INSTANCE.reset();
    }

    @After
    public void shutdown() {
        Repository.INSTANCE.reset();
        PeriodicNodeStatusDataStore.class.cast(IoCs.getInstance(NodeStatusDataStore.class)).shutdown();
    }

    @Test
    public void checkValidations() throws InterruptedException {
        Thread.sleep(150);

        final NodeStatus result = IoCs.getInstance(NodeStatusDataStore.class).statuses().values().iterator().next();
        assertEquals(1, result.getResults().length);

        final ValidationResult validationResult = result.getResults()[0];
        assertEquals("tomee-datasource-db", validationResult.getName());
        assertEquals(Status.OK, validationResult.getStatus());
    }
}
