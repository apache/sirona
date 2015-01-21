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
import org.apache.sirona.store.counter.CollectorCounterStore;
import org.apache.sirona.math.M2AwareStatisticalSummary;
import org.apache.sirona.repositories.Repository;
import org.apache.sirona.store.memory.counter.InMemoryCollectorCounterStore;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class AggregatorTest {
    @Before @After
    public void clear() {
        Repository.INSTANCE.clearCounters();
    }

    @Test
    public void counterStore() {
        final Counter.Key key = new Counter.Key(new Role("r", Unit.UNARY), "n");

        // note: the input data are maybe not that consistent (min > max) but this test just checks computations
        final CollectorCounterStore store = new InMemoryCollectorCounterStore();
        store.update(key, "client1", new M2AwareStatisticalSummary(1, 2, 5, 0, 10, 6, 7), 4);
        store.update(key, "client2", new M2AwareStatisticalSummary(2, 4, 8, 1, 15, 9, 5), 2);

        assertEquals(2, store.markers().size());
        assertTrue(store.markers().contains("client1"));
        assertTrue(store.markers().contains("client2"));

        final Counter counter1 = store.getCounters("client1").iterator().next();
        assertEquals(4, counter1.getMaxConcurrency());
        assertEquals(4, counter1.currentConcurrency().get());
        assertEquals(5, counter1.getHits());
        assertEquals(10., counter1.getMin(), 0);
        assertEquals(0., counter1.getMax(), 0);
        assertEquals(1.4142, counter1.getStandardDeviation(), 0.001);
        assertEquals(2., counter1.getVariance(), 0);
        assertEquals(6., counter1.getSum(), 0);

        final Counter counter2 = store.getCounters("client2").iterator().next();
        assertEquals(2, counter2.getMaxConcurrency());
        assertEquals(2, counter2.currentConcurrency().get());
        assertEquals(8, counter2.getHits());
        assertEquals(15., counter2.getMin(), 0);
        assertEquals(1., counter2.getMax(), 0);
        assertEquals(2., counter2.getStandardDeviation(), 0.);
        assertEquals(4., counter2.getVariance(), 0);
        assertEquals(9., counter2.getSum(), 0);

        final Counter aggregate = store.getOrCreateCounter(key);
        assertEquals(6, aggregate.getMaxConcurrency());
        assertEquals(6, aggregate.currentConcurrency().get());
        assertEquals(13, aggregate.getHits());
        assertEquals(10., aggregate.getMin(), 0);
        assertEquals(1., aggregate.getMax(), 0);
        assertEquals(1.12089, aggregate.getStandardDeviation(), 0.001);
        assertEquals(1.2564, aggregate.getVariance(), 0.001);
        assertEquals(15., aggregate.getSum(), 0);
    }
}
