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
package org.apache.sirona.collector.server;

import org.apache.sirona.Role;
import org.apache.sirona.configuration.ioc.IoCs;
import org.apache.sirona.counters.Counter;
import org.apache.sirona.counters.DefaultCounter;
import org.apache.sirona.counters.Unit;
import org.apache.sirona.cube.Cube;
import org.apache.sirona.cube.CubeBuilder;
import org.apache.sirona.cube.CubeCounterDataStore;
import org.apache.sirona.repositories.Repository;
import org.apache.sirona.store.counter.CollectorCounterStore;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;

public class CubeDataStoreCompatibilityTest {
    private CollectorServer server;

    @Before
    public void start() {
        server = new CollectorServer("localhost", Integer.getInteger("collector.server.port", 1234)).start();
        if ("http://localhost:".equals(System.getProperty("org.apache.sirona.cube.CubeBuilder.collector", "http://localhost:"))) {
            System.setProperty("org.apache.sirona.cube.CubeBuilder.collector", "http://localhost:" + server.getPort());
        }
        Repository.INSTANCE.clearCounters();
    }

    @After
    public void shutdown() {
        server.stop();
        Repository.INSTANCE.clearCounters();
    }

    @Test
    public void cubeMe() {
        new SeeMyProtectedStuffStore().doPush();

        final CollectorCounterStore store = IoCs.getInstance(CollectorCounterStore.class);
        final Counter counter1 = store.getOrCreateCounter(new Counter.Key(new Role("cube", Unit.UNARY), "client"));
        final Counter counter1Client1 = store.getOrCreateCounter(new Counter.Key(new Role("cube", Unit.UNARY), "client"), "local");

        assertEquals(50, counter1.getHits());
        assertEquals(counter1.getHits(), counter1Client1.getHits());
    }

    private static class SeeMyProtectedStuffStore extends CubeCounterDataStore {
        public void doPush() {
            pushCountersByBatch(Arrays.<Counter>asList(
                new DefaultCounter(new Counter.Key(new Role("cube", Unit.UNARY), "client"), null) {
                    @Override
                    public long getHits() {
                        return 50;
                    }
                })
            );
        }
    }
}
