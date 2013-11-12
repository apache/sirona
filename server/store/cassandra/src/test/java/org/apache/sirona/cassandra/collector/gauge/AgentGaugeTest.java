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
package org.apache.sirona.cassandra.collector.gauge;

import org.apache.sirona.Role;
import org.apache.sirona.cassandra.framework.CassandraRunner;
import org.apache.sirona.counters.Unit;
import org.apache.sirona.store.gauge.GaugeValuesRequest;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Collection;
import java.util.Date;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(CassandraRunner.class)
public class AgentGaugeTest {
    private static final Role ROLE = new Role("_role_", Unit.DECA);
    private static final String MARKER = "node1";

    @Test
    public void checkLongHandling() {
        new CassandraCollectorGaugeDataStore()
            .addToGauge( // was throwing exception cause of a wrong comparator in DDL, that's why we don't have asserts then
                new Role("/-HTTP-301", Unit.UNARY),
                new Date().getTime(),
                0., MARKER
            );
    }

    @Test
    public void createGaugeWithMarker() {
        new CassandraCollectorGaugeDataStore().createOrNoopGauge(ROLE, MARKER);
        new CassandraCollectorGaugeDataStore().createOrNoopGauge(ROLE, MARKER); // createOrNoopGauge is reentrant
        final Collection<String> markers = new CassandraCollectorGaugeDataStore().markers();
        assertNotNull(markers);
        assertEquals(1, markers.size());
        assertEquals(MARKER, markers.iterator().next());
    }

    @Test
    public void addToGaugesWithMarker() {
        new CassandraCollectorGaugeDataStore().addToGauge(ROLE, 1, 5, MARKER);
        new CassandraCollectorGaugeDataStore().addToGauge(ROLE, 2, 6, MARKER);

        final Map<Long, Double> values = new CassandraCollectorGaugeDataStore().getGaugeValues(new GaugeValuesRequest(0, 5, ROLE), MARKER);

        assertNotNull(values);
        assertEquals(2, values.size());
        assertEquals(5., values.get(1L), 0.);
        assertEquals(6., values.get(2L), 0.);
    }

    @Test
    public void getAggregatedGauges() {
        new CassandraCollectorGaugeDataStore().addToGauge(ROLE, 1, 5, MARKER);
        new CassandraCollectorGaugeDataStore().addToGauge(ROLE, 2, 6, MARKER);
        new CassandraCollectorGaugeDataStore().addToGauge(ROLE, 1, 5, "node2");
        new CassandraCollectorGaugeDataStore().addToGauge(ROLE, 2, 6, "node2");

        final Map<Long, Double> values = new CassandraCollectorGaugeDataStore().getGaugeValues(new GaugeValuesRequest(0, 5, ROLE));

        assertNotNull(values);
        assertEquals(2, values.size());
        assertEquals(10., values.get(1L), 0.);
        assertEquals(12., values.get(2L), 0.);
    }

    @Test
    public void gauges() {
        new CassandraCollectorGaugeDataStore().addToGauge(ROLE, 1, 5, MARKER);

        final Collection<Role> gauges = new CassandraCollectorGaugeDataStore().gauges();
        assertNotNull(gauges);
        assertEquals(1, gauges.size());
        assertEquals(ROLE, gauges.iterator().next());
    }

    @Test
    public void findRoles() {
        new CassandraCollectorGaugeDataStore().addToGauge(ROLE, 1, 5, MARKER);

        final Role found = new CassandraCollectorGaugeDataStore().findGaugeRole(ROLE.getName());
        assertNotNull(found);
        assertEquals(ROLE.getUnit(), found.getUnit());
    }
}
