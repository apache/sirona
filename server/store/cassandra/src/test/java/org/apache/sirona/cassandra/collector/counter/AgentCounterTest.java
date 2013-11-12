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
package org.apache.sirona.cassandra.collector.counter;

import me.prettyprint.hector.api.Cluster;
import me.prettyprint.hector.api.Keyspace;
import org.apache.sirona.Role;
import org.apache.sirona.cassandra.framework.CassandraRunner;
import org.apache.sirona.cassandra.framework.CassandraTestInject;
import org.apache.sirona.store.counter.AggregatedCollectorCounter;
import org.apache.sirona.store.counter.LeafCollectorCounter;
import org.apache.sirona.counters.Counter;
import org.apache.sirona.counters.Unit;
import org.apache.sirona.math.M2AwareStatisticalSummary;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
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

        assertNull(new CassandraCollectorCounterDataStore().findByKey(key, marker));
        new CassandraCollectorCounterDataStore().getOrCreateCounter(key, marker);
        assertNotNull(new CassandraCollectorCounterDataStore().findByKey(key, marker));

        final LeafCollectorCounter counter = new CassandraCollectorCounterDataStore().getOrCreateCounter(key, marker);
        assertEquals("K100Drap#1", counter.getKey().getName());
        assertEquals("K100Drap", counter.getKey().getRole().getName());
    }

    @Test
    public void markers() {
        final Counter.Key key = new Counter.Key(new Role("K100Drap", Unit.UNARY), "K100Drap#1");
        final String marker = "node1";

        assertNull(new CassandraCollectorCounterDataStore().findByKey(key, marker));
        new CassandraCollectorCounterDataStore().getOrCreateCounter(key, marker);
        assertNotNull(new CassandraCollectorCounterDataStore().findByKey(key, marker));

        final Collection<String> markers = new CassandraCollectorCounterDataStore().markers();
        assertEquals(1, markers.size());
        assertTrue(markers.contains("node1"));
    }

    @Test
    public void getAggregatedCountersByKey() {
        final Counter.Key key = new Counter.Key(new Role("cassandra", Unit.MEGA), "k");
        final CassandraCollectorCounterDataStore store = new CassandraCollectorCounterDataStore();
        final CassandraLeafCounter counter1 = new CassandraLeafCounter(key, store, "node1").sync(new M2AwareStatisticalSummary(1, 1, 1, 1, 1, 1, 1), 1);
        final CassandraLeafCounter counter2 = new CassandraLeafCounter(key, store, "node2").sync(new M2AwareStatisticalSummary(3, 4, 4, 4, 4, 4, 4), 2);

        new CassandraCollectorCounterDataStore().getOrCreateCounter(key, "node1").update(counter1.getStatistics(), counter1.getMaxConcurrency());
        new CassandraCollectorCounterDataStore().getOrCreateCounter(key, "node2").update(counter2.getStatistics(), counter2.getMaxConcurrency());

        final AggregatedCollectorCounter aggregation = new CassandraCollectorCounterDataStore().getOrCreateCounter(key);
        assertEquals(3, aggregation.getMaxConcurrency());
        assertEquals(1., aggregation.getMean(), 0.);
        assertEquals(1., aggregation.getMin(), 0.);
        assertEquals(5., aggregation.getHits(), 0.);
        assertEquals(2.05, aggregation.getVariance(), 0.);
        assertEquals(4., aggregation.getMax(), 0.);
        assertEquals(5., aggregation.getSum(), 0.);
        assertEquals(8.2, aggregation.getSecondMoment(), 0.);
    }

    @Test
    public void getAggregatedCounters() {
        final Counter.Key key1 = new Counter.Key(new Role("cassandra", Unit.MEGA), "k1");
        final Counter.Key key2 = new Counter.Key(new Role("cassandra", Unit.MEGA), "k2");

        new CassandraCollectorCounterDataStore().getOrCreateCounter(key1, "node1").update(new M2AwareStatisticalSummary(1, 1, 1, 1, 1, 1, 1), 1);
        new CassandraCollectorCounterDataStore().getOrCreateCounter(key1, "node2").update(new M2AwareStatisticalSummary(3, 4, 4, 4, 4, 4, 4), 2);
        new CassandraCollectorCounterDataStore().getOrCreateCounter(key2, "node1").update(new M2AwareStatisticalSummary(1, 1, 1, 1, 1, 1, 1), 1);
        new CassandraCollectorCounterDataStore().getOrCreateCounter(key2, "node2").update(new M2AwareStatisticalSummary(3, 4, 4, 4, 4, 4, 4), 2);

        final Collection<Counter> aggregations = new CassandraCollectorCounterDataStore().getCounters();
        assertEquals(2, aggregations.size());

        for (final Counter aggregation : aggregations) {
            assertEquals(3, aggregation.getMaxConcurrency());
            assertEquals(1., aggregation.getMean(), 0.);
            assertEquals(1., aggregation.getMin(), 0.);
            assertEquals(5., aggregation.getHits(), 0.);
            assertEquals(2.05, aggregation.getVariance(), 0.);
            assertEquals(4., aggregation.getMax(), 0.);
            assertEquals(5., aggregation.getSum(), 0.);
            assertEquals(8.2, aggregation.getSecondMoment(), 0.);
        }
    }

    @Test
    public void update() {
        final Counter.Key key = new Counter.Key(new Role("K100Drap", Unit.UNARY), "K100Drap#1");

        new CassandraCollectorCounterDataStore().getOrCreateCounter(key, "node1");
        assertNotNull(new CassandraCollectorCounterDataStore().findByKey(key, "node1"));
        new CassandraCollectorCounterDataStore().update(key, "node1", new M2AwareStatisticalSummary(3, 4, 4, 4, 4, 4, 4), 2);

        final LeafCollectorCounter counter = new CassandraCollectorCounterDataStore().getOrCreateCounter(key, "node1");
        assertEquals("K100Drap#1", counter.getKey().getName());
        assertEquals("K100Drap", counter.getKey().getRole().getName());
        assertEquals(4, counter.getMax(), 0.);
        assertEquals(2, counter.getMaxConcurrency(), 0.);
    }
}
