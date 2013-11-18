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
package org.apache.sirona.counters;

import org.apache.sirona.Role;
import org.apache.sirona.configuration.Configuration;
import org.apache.sirona.counters.jmx.CounterJMX;
import org.junit.Test;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class JMXTest {
    @Test
    public void register() throws Exception {
        final DefaultCounter counter = new DefaultCounter(new Counter.Key(Role.JDBC, "def"), null) {
            @Override
            public double getMax() {
                return 3;
            }
        };

        final MBeanServer server = ManagementFactory.getPlatformMBeanServer();
        final ObjectName objectName = new ObjectName(
            Configuration.CONFIG_PROPERTY_PREFIX
                + "counter:role=" + counter.getKey().getRole().getName()
                + ",name=" + counter.getKey().getName());
        counter.setJmx(objectName);

        server.registerMBean(new CounterJMX(counter), objectName);
        try {
            assertTrue(server.isRegistered(objectName));
            assertEquals(3., server.getAttribute(objectName, "Max"));
            assertEquals("def", server.getAttribute(objectName, "Name"));
            assertEquals("jdbc", server.getAttribute(objectName, "Role"));
        } finally {
            server.unregisterMBean(objectName);
        }
    }
}
