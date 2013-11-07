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
package org.apache.sirona.cassandra;

import me.prettyprint.hector.api.Cluster;
import me.prettyprint.hector.api.Keyspace;
import org.apache.sirona.Role;
import org.apache.sirona.cassandra.collector.counter.CassandraCollectorCounterDataStore;
import org.apache.sirona.cassandra.collector.counter.CounterDao;
import org.apache.sirona.cassandra.framework.CassandraRunner;
import org.apache.sirona.cassandra.framework.CassandraTestInject;
import org.apache.sirona.collector.server.store.counter.LeafCollectorCounter;
import org.apache.sirona.counters.Counter;
import org.apache.sirona.counters.Unit;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@RunWith(CassandraRunner.class)
public class AgentCounterTest {
    @CassandraTestInject
    private Cluster cluster;

    @CassandraTestInject
    private Keyspace keyspace;

    @Test
    public void keyspaceInit() {
        assertEquals(3, cluster.describeKeyspaces().size()); // system keyspaces

        new CassandraCollectorCounterDataStore();

        assertEquals(3 + 1, cluster.describeKeyspaces().size());
        assertNotNull(cluster.describeKeyspace(keyspace.getKeyspaceName()));
    }

    @Test
    public void getOrCreateCounter() {
        final Counter.Key key = new Counter.Key(new Role("K100Drap", Unit.UNARY), "K100Drap#1");
        final String marker = "node1";

        assertNull(new CounterDao().findByKey(key, marker));
        new CassandraCollectorCounterDataStore().getOrCreateCounter(key, marker);
        assertNotNull(new CounterDao().findByKey(key, marker));

        final LeafCollectorCounter counter = new CassandraCollectorCounterDataStore().getOrCreateCounter(key, marker);
        assertEquals("K100Drap#1", counter.getKey().getName());
        assertEquals("K100Drap", counter.getKey().getRole().getName());
    }

    @Test
    public void markers() {
        final Counter.Key key = new Counter.Key(new Role("K100Drap", Unit.UNARY), "K100Drap#1");
        final String marker = "node1";

        assertNull(new CounterDao().findByKey(key, marker));
        new CassandraCollectorCounterDataStore().getOrCreateCounter(key, marker);
        assertNotNull(new CounterDao().findByKey(key, marker));

        final Collection<String> markers = new CassandraCollectorCounterDataStore().markers();
        assertEquals(1, markers.size());
        assertTrue(markers.contains("node1"));
    }
}
