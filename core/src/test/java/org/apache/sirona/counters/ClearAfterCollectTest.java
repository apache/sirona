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
import org.apache.sirona.repositories.Repository;
import org.apache.sirona.store.memory.counter.BatchCounterDataStore;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;
import java.util.LinkedList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ClearAfterCollectTest {
    @Before
    @After
    public void reset() {
        Repository.INSTANCE.clearCounters();
    }

    @Test
    public void clear() throws InterruptedException {
        final LinkedList<Integer> size = new LinkedList<Integer>();
        final BatchCounterDataStore store = new BatchCounterDataStore() {
            protected void pushCountersByBatch(final Collection<Counter> instance) {
                size.add(instance.size());
            }

            protected int getPeriod(final String prefix) {
                return 100;
            }

            protected boolean isClearAfterCollect(final String prefix) {
                return true;
            }
        };

        Repository.INSTANCE.getCounter(new Counter.Key(Role.PERFORMANCES, "counter")).add(123);
        Thread.sleep(250);
        store.shutdown();

        assertTrue(size.size() >= 2);
        assertEquals(1, size.iterator().next().intValue());
        assertEquals(0, size.get(1).intValue());
    }
}
