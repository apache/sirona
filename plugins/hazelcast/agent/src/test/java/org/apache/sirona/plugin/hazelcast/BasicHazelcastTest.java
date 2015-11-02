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

import com.hazelcast.config.Config;
import com.hazelcast.config.JoinConfig;
import com.hazelcast.config.NetworkConfig;
import com.hazelcast.config.TcpIpConfig;
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
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.CountDownLatch;

import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class BasicHazelcastTest {
    @After
    public void after() {
        Repository.INSTANCE.reset();
        IoCs.shutdown();
    }

    @Test
    public void gauges() throws Throwable {
        final Config config = new Config();
        final NetworkConfig networkConfig = new NetworkConfig();
        final JoinConfig join = new JoinConfig();
        final TcpIpConfig tcpIpConfig = new TcpIpConfig();
        tcpIpConfig.setEnabled(true);
        tcpIpConfig.setMembers(singletonList("localhost"));
        join.setTcpIpConfig(tcpIpConfig);
        networkConfig.setJoin(join);
        config.setNetworkConfig(networkConfig);

        final HazelcastInstance instance1 = Hazelcast.newHazelcastInstance(config);
        final HazelcastInstance instance2 = Hazelcast.newHazelcastInstance(config);
        HazelcastInstance instance3 = null;

        Map<Long, Double> members1 = null, partitions;

        final Gauge.LoaderHelper loader = new Gauge.LoaderHelper(false);
        try {
            Thread.sleep(250);
            instance3 = Hazelcast.newHazelcastInstance(config);
            Thread.sleep(250);

            members1 = gaugeValues("hazelcast-members-cluster");
            partitions = gaugeValues("hazelcast-partitions-cluster");

            Thread.sleep(300);
            assertNotNull(partitions);
            assertEquals(instance2.getPartitionService().getPartitions().size(), partitions.values().iterator().next(), 0.);

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

            assertNotNull(members1);

            assertEquals(2., members1.values().iterator().next(), 0.);
            assertEquals(2., gaugeValues("hazelcast-members-cluster").values().iterator().next(), 0.);
            assertEquals(3., new TreeMap<Long, Double>(members1).lastEntry().getValue(), 0.);
            int tryCount = 0;
            while (true) { // we shouldn't need it but MVN+machine related things can make it fragile if not
                try {
                    assertEquals(2., new TreeMap<Long, Double>(gaugeValues("hazelcast-members-cluster")).lastEntry().getValue(), 0.);
                    break;
                } catch (final Throwable th) {
                    if (tryCount < 10) {
                        tryCount++;
                        Thread.sleep(100);
                    } else {
                        throw th;
                    }
                }
            }
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
    }

    private static SortedMap<Long, Double> gaugeValues(final String role) {
        return Repository.INSTANCE.getGaugeValues(0, System.currentTimeMillis(), new Role(role, Unit.UNARY));
    }
}
