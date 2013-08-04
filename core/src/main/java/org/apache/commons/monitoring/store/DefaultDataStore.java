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
package org.apache.commons.monitoring.store;

import org.apache.commons.monitoring.Role;
import org.apache.commons.monitoring.configuration.Configuration;
import org.apache.commons.monitoring.counters.Counter;
import org.apache.commons.monitoring.counters.DefaultCounter;
import org.apache.commons.monitoring.gauges.Gauge;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.Lock;

public class DefaultDataStore implements DataStore {
    private final ConcurrentMap<Counter.Key, Counter> counters = new ConcurrentHashMap<Counter.Key, Counter>(50);
    private final Map<Role, FixedSizedMap> gauges = new ConcurrentHashMap<Role, FixedSizedMap>();

    @Override
    public Counter getOrCreateCounter(final Counter.Key key) {
        Counter counter = counters.get(key);
        if (counter == null) {
            counter = new DefaultCounter(key, this);
            final Counter previous = counters.putIfAbsent(key, counter);
            if (previous != null) {
                counter = previous;
            }
        }
        return counter;
    }

    @Override
    public void clearCounters() {
        counters.clear();
    }

    @Override
    public Collection<Counter> getCounters() {
        return counters.values();
    }

    @Override
    public void addToCounter(final Counter counter, final double delta) {
        if (!DefaultCounter.class.isInstance(counter)) {
            throw new IllegalArgumentException(DefaultDataStore.class.getName() + " only supports " + DefaultCounter.class.getName());
        }

        final DefaultCounter defaultCounter = DefaultCounter.class.cast(counter);
        final Lock lock = defaultCounter.getLock();
        lock.lock();
        try {
            defaultCounter.addInternal(delta);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public Map<Long, Double> getGaugeValues(final long start, final long end, final Role role) {
        final Map<Long, Double> map = gauges.get(role);
        if (map == null) {
            return Collections.emptyMap();
        }

        final Map<Long, Double> out = new TreeMap<Long, Double>();
        for (final Map.Entry<Long, Double> entry : map.entrySet()) {
            final long time = entry.getKey();
            if (time >= start && time <= end) {
                out.put(time, entry.getValue());
            }
        }
        return out;
    }

    @Override
    public void createOrNoopGauge(final Role role) {
        gauges.put(role, new FixedSizedMap());
    }

    @Override
    public void addToGauge(final Gauge gauge, final long time, final double value) {
        gauges.get(gauge.role()).put(time, value);
    }

    // no perf issues here normally since add is called not that often
    protected static class FixedSizedMap extends LinkedHashMap<Long, Double> {
        private static final int MAX_SIZE = Configuration.getInteger(Configuration.COMMONS_MONITORING_PREFIX + "gauge.max-size", 100);

        protected FixedSizedMap() {
            super(MAX_SIZE);
        }

        protected FixedSizedMap(final Map<Long, Double> value) {
            super(value);
        }

        @Override
        protected boolean removeEldestEntry(final Map.Entry<Long, Double> eldest) {
            return size() > MAX_SIZE;
        }

        public synchronized Map<Long, Double> copy() {
            return Map.class.cast(super.clone());
        }
    }
}
