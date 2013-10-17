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
package org.apache.commons.monitoring.cube;

import org.apache.commons.monitoring.Role;
import org.apache.commons.monitoring.configuration.Configuration;
import org.apache.commons.monitoring.counters.Counter;
import org.apache.commons.monitoring.counters.MetricData;
import org.apache.commons.monitoring.gauges.Gauge;
import org.apache.commons.monitoring.repositories.Repository;
import org.apache.commons.monitoring.store.BatchCounterDataStore;
import org.apache.commons.monitoring.store.GaugeValuesRequest;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class CubeDataStore extends BatchCounterDataStore {
    private static final String GAUGE_TYPE = "gauge";
    private static final String COUNTER_TYPE = "counter";

    private final Cube cube = Configuration.findOrCreateInstance(CubeBuilder.class).build();

    @Override
    protected synchronized void pushCountersByBatch(final Repository instance) {
        final long ts = System.currentTimeMillis();
        final StringBuilder events = cube.newEventStream();
        for (final Counter counter : instance) {
            final MapBuilder data = new MapBuilder()
                .add("name", counter.getKey().getName())
                .add("role", counter.getKey().getRole().getName());

            for (final MetricData metric : MetricData.values()) {
                final double value = metric.value(counter);
                if (!Double.isNaN(value) && !Double.isInfinite(value)) {
                    data.add(metric.name(), value);
                }
            }

            cube.buildEvent(events, COUNTER_TYPE, ts, data.map());
        }
        cube.post(events);
    }

    @Override
    public void addToGauge(final Gauge gauge, final long time, final double value) {
        final Role role = gauge.role();

        cube.post(
            cube.buildEvent(new StringBuilder(), GAUGE_TYPE, time,
                new MapBuilder()
                    .add("value", value)
                    .add("role", role.getName())
                    .add("unit", role.getUnit().getName())
                    .map()));
    }

    @Override
    public Map<Long, Double> getGaugeValues(final GaugeValuesRequest gaugeValuesRequest) {
        return Collections.emptyMap(); // TODO: maybe query cube?
    }

    private static class MapBuilder {
        private final Map<String, Object> map = new HashMap<String, Object>();

        public MapBuilder add(final String key, final Object value) {
            map.put(key, value);
            return this;
        }

        public Map<String, Object> map() {
            return map;
        }
    }
}
