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
package org.apache.sirona.cassandra.local;

import org.apache.sirona.Role;
import org.apache.sirona.cassandra.agent.counter.CassandraCounterDataStore;
import org.apache.sirona.cassandra.framework.CassandraRunner;
import org.apache.sirona.counters.Counter;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Collection;

import static org.junit.Assert.assertEquals;

@RunWith(CassandraRunner.class)
public class CounterDataStoreTest {
    @Test
    public void getCounters() throws InterruptedException {
        final CassandraCounterDataStore store = new CassandraCounterDataStore() {
            protected void initMarkerIfNotAlreadyDone() {
                marker = "test";
            }

            @Override
            protected int getPeriod(final String prefix) {
                return 100;
            }
        };

        assertEquals(0, store.getCounters().size());

        final Counter counter = store.getOrCreateCounter(new Counter.Key(Role.FAILURES, "oops"));
        counter.add(150);
        counter.updateConcurrency(5);

        Thread.sleep(250);

        final Collection<Counter> counters = store.getCounters();
        assertEquals(1, counters.size());

        final Counter c = counters.iterator().next();
        assertEquals(counter, c); // compare only key
        assertEquals(5, c.getMaxConcurrency());
        assertEquals(150., c.getMax(), 0.);
        assertEquals(1, c.getHits());
    }
}
