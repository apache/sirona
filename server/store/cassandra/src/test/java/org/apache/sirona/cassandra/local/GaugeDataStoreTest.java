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
import org.apache.sirona.cassandra.agent.gauge.CassandraGaugeDataStore;
import org.apache.sirona.cassandra.framework.CassandraRunner;
import org.apache.sirona.configuration.ioc.Created;
import org.apache.sirona.configuration.ioc.IoCs;
import org.apache.sirona.gauges.Gauge;
import org.apache.sirona.store.gauge.GaugeValuesRequest;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Collection;
import java.util.SortedMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(CassandraRunner.class)
public class GaugeDataStoreTest {
    @Test
    public void gauges() throws InterruptedException, IllegalAccessException {
        final CassandraGaugeDataStore store = IoCs.processInstance(new CassandraGaugeDataStore() {
            @Created
            protected void forceMarker() {
                marker = "test";
            }

            @Override
            protected int getPeriod(final String prefix) {
                return 100;
            }
        });

        assertEquals(0, store.gauges().size());

        final Gauge gauge = new Gauge() {
            @Override
            public Role role() {
                return Role.FAILURES;
            }

            @Override
            public double value() {
                return 5;
            }
        };
        store.createOrNoopGauge(gauge.role());
        store.addGauge(gauge);

        Thread.sleep(250);
        store.shutdown();

        final Collection<Role> gauges = store.gauges();
        assertEquals(1, gauges.size());

        assertEquals(gauge.role(), gauges.iterator().next());
        final SortedMap<Long,Double> gaugeValues = store.getGaugeValues(new GaugeValuesRequest(0, System.currentTimeMillis(), Role.FAILURES));
        assertTrue(gaugeValues.size() > 0);
        assertEquals(5., gaugeValues.values().iterator().next(), 0.);

        store.gaugeStopped(gauge.role());
    }
}
