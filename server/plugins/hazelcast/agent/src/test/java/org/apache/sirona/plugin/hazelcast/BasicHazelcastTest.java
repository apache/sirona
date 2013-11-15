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
package org.apache.sirona.plugin.hazelcast;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.LifecycleEvent;
import com.hazelcast.core.LifecycleListener;
import org.apache.sirona.Role;
import org.apache.sirona.configuration.ioc.IoCs;
import org.apache.sirona.counters.Unit;
import org.apache.sirona.gauges.Gauge;
import org.apache.sirona.repositories.Repository;
import org.junit.After;
import org.junit.Test;

import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CountDownLatch;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class BasicHazelcastTest {
    @After
    public void after() {
        Repository.INSTANCE.reset();
        IoCs.shutdown();
    }

    @Test
    public void gauges() throws InterruptedException {
        final HazelcastInstance instance1 = Hazelcast.newHazelcastInstance();
        final HazelcastInstance instance2 = Hazelcast.newHazelcastInstance();
        HazelcastInstance instance3 = null;

        Map<Long, Double> members1 = null, members2 = null, partitions = null;

        final Gauge.LoaderHelper loader = new Gauge.LoaderHelper(false);
        try {
            Thread.sleep(250);
            instance3 = Hazelcast.newHazelcastInstance();
            Thread.sleep(250);

            members1 = gaugeValues("hazelcast-members-cluster");
            partitions = gaugeValues("hazelcast-partitions-cluster");

            final CountDownLatch instance1Stopped = new CountDownLatch(1);
            instance1.getLifecycleService().addLifecycleListener(new LifecycleListener() {
                @Override
                public void stateChanged(final LifecycleEvent event) {
                    if (LifecycleEvent.LifecycleState.SHUTDOWN.equals(event.getState())) {
                        instance1Stopped.countDown();
                    }
                }
            });
            instance1.getLifecycleService().shutdown();
            instance1Stopped.await();
            Thread.sleep(300);

            assertNotNull(partitions);
            assertEquals(instance2.getPartitionService().getPartitions().size(), partitions.values().iterator().next(), 0.);

            members2 = gaugeValues("hazelcast-members-cluster");
        } finally {
            loader.destroy();
            instance2.getLifecycleService().shutdown();
            if (instance3 != null) {
                instance3.getLifecycleService().shutdown();
            }
            if (instance1.getLifecycleService().isRunning()) {
                instance1.getLifecycleService().shutdown();
            }
        }

        assertNotNull(members1);
        assertNotNull(members2);

        assertEquals(2., members1.values().iterator().next(), 0.);
        assertEquals(2., members2.values().iterator().next(), 0.);
        assertEquals(3., new TreeMap<Long, Double>(members1).lastEntry().getValue(), 0.);
        assertEquals(2., new TreeMap<Long, Double>(members2).lastEntry().getValue(), 0.);
    }

    private static Map<Long, Double> gaugeValues(final String role) {
        return Repository.INSTANCE.getGaugeValues(0, System.currentTimeMillis(), new Role(role, Unit.UNARY));
    }
}
