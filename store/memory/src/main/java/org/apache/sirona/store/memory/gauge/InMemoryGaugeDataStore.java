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
package org.apache.sirona.store.memory.gauge;

import org.apache.sirona.Role;
import org.apache.sirona.configuration.Configuration;
import org.apache.sirona.store.gauge.BatchGaugeDataStoreAdapter;
import org.apache.sirona.store.gauge.GaugeValuesRequest;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListMap;

public class InMemoryGaugeDataStore extends BatchGaugeDataStoreAdapter
{
    protected final ConcurrentMap<Role, SortedMap<Long, Double>> gauges = new ConcurrentHashMap<Role, SortedMap<Long, Double>>();
    protected final Map<String, Role> roleMapping = new ConcurrentHashMap<String, Role>();

    @Override
    public SortedMap<Long, Double> getGaugeValues(final GaugeValuesRequest gaugeValuesRequest) {
        final Map<Long, Double> map = gauges.get(gaugeValuesRequest.getRole());
        if (map == null) {
            return new TreeMap<Long, Double>();
        }

        final Map<Long, Double> copy = new HashMap<Long, Double>(map);

        final SortedMap<Long, Double> out = new TreeMap<Long, Double>();
        for (final Map.Entry<Long, Double> entry : copy.entrySet()) {
            final long time = entry.getKey();
            if (time >= gaugeValuesRequest.getStart() && time <= gaugeValuesRequest.getEnd()) {
                out.put(time, entry.getValue());
            }
        }

        return out;
    }

    @Override
    public void createOrNoopGauge(final Role gauge) {
        gauges.putIfAbsent(gauge, new FixedSizedMap());
        roleMapping.put(gauge.getName(), gauge);
    }

    @Override
    public void addToGauge(final Role role, final long time, final double value) {
        gauges.get(role).put(time, value);
    }

    @Override
    public Collection<Role> gauges() {
        return gauges.keySet();
    }

    @Override
    public Role findGaugeRole(final String name) {
        return roleMapping.get(name);
    }

    @Override
    protected void pushGauges(final Map<Role, Measure> gauges) {
        for (final Map.Entry<Role, Measure> entry : gauges.entrySet()) {
            final Measure value = entry.getValue();
            addToGauge(entry.getKey(), value.getTime(), value.getValue());
        }
    }

    @Override
    public void gaugeStopped(final Role gauge) {
        super.gaugeStopped(gauge);
        roleMapping.remove(gauge.getName());
    }

    // no perf issues here normally since add is called not that often
    protected static class FixedSizedMap extends ConcurrentSkipListMap<Long, Double> {
        private static final int MAX_SIZE = Configuration.getInteger(Configuration.CONFIG_PROPERTY_PREFIX + "gauge.max-size", 100);

        protected FixedSizedMap() {
            // no-op
        }

        @Override
        public Double put(final Long key, final Double value) {
            if (size() >= MAX_SIZE) {
                remove(keySet().iterator().next());
            }
            return super.put(key, value);
        }
    }
}
