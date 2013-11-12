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
package org.apache.sirona.gauges;

import org.apache.sirona.Role;
import org.apache.sirona.counters.Unit;
import org.apache.sirona.store.gauge.CollectorGaugeDataStore;
import org.apache.sirona.repositories.Repository;
import org.apache.sirona.store.gauge.DelegatedCollectorGaugeDataStore;
import org.apache.sirona.store.gauge.GaugeValuesRequest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class CollectorGaugeTest {
    @Before
    @After
    public void clear() {
        Repository.INSTANCE.clearCounters();
    }

    @Test
    public void gaugeStore() {
        final Role role = new Role("gauge", Unit.UNARY);

        final CollectorGaugeDataStore store = new DelegatedCollectorGaugeDataStore();
        store.addToGauge(role, 1234, 5678, "client1");
        store.addToGauge(role, 987, 654, "client2");

        final GaugeValuesRequest request = new GaugeValuesRequest(0, Integer.MAX_VALUE, role);
        final Map<Long, Double> result = store.getGaugeValues(request);
        final Map<Long, Double> client1 = store.getGaugeValues(request, "client1");
        final Map<Long, Double> client2 = store.getGaugeValues(request, "client2");

        assertNotNull(result);
        assertNotNull(client1);
        assertNotNull(client2);

        assertEquals(2, result.size());
        assertEquals(654, result.get(987L), 0);
        assertEquals(5678, result.get(1234L), 0);

        assertEquals(1, client1.size());
        assertEquals(5678, client1.get(1234L), 0);

        assertEquals(1, client2.size());
        assertEquals(654, client2.get(987L), 0);
    }
}
