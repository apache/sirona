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
package org.apache.commons.monitoring.gauges;

import org.apache.commons.monitoring.Role;
import org.apache.commons.monitoring.configuration.Configuration;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

public final class DefaultGaugeManager implements GaugeManager {
    private static final int MAX_SIZE = Configuration.getInteger(Configuration.COMMONS_MONITORING_PREFIX + "gauge.max-size", 10);

    private final Map<Role, Timer> timers = new ConcurrentHashMap<Role, Timer>();
    private final Map<Role, FixedSizedMap> values = new ConcurrentHashMap<Role, FixedSizedMap>();

    @Override
    public void start(final Map<Role, Map<Long, Double>> initialData) {
        if (initialData != null) {
            for (final Map.Entry<Role, Map<Long, Double>> entry : initialData.entrySet()) {
                values.put(entry.getKey(), new FixedSizedMap(entry.getValue()));
            }
        }

        startFoundGaugeTimers();
    }

    @Override
    public void stop() {
        for (final Timer timer : timers.values()) {
            timer.cancel();
        }
        timers.clear();
    }

    @Override
    public Map<Long, Double> getValues(final Role role) {
        return values.get(role).copy();
    }

    protected void startFoundGaugeTimers() {
        for (final Gauge gauge : findGauges()) {
            final Role role = gauge.role();

            final FixedSizedMap gaugeValues = new FixedSizedMap();
            this.values.put(role, gaugeValues);

            final Timer timer = new Timer("gauge-" + role.getName() + "-timer", true);
            timers.put(role, timer);
            timer.scheduleAtFixedRate(new GaugeTask(gauge, gaugeValues), 0, gauge.period());
        }
    }

    protected ServiceLoader<Gauge> findGauges() {
        // core (where gauge is) is often in an upper classloader so don't use Gauge classloader
        return ServiceLoader.load(Gauge.class, Thread.currentThread().getContextClassLoader());
    }

    @Override
    public void stopGauge(final Role role) {
        final Timer timer = timers.get(role);
        if (timer != null) {
            timer.cancel();
        }
    }

    private static class GaugeTask extends TimerTask {
        private final Gauge gauge;
        private final FixedSizedMap values;

        public GaugeTask(final Gauge gauge, FixedSizedMap values) {
            this.gauge = gauge;
            this.values = values;
        }

        @Override
        public void run() {
            values.add(gauge.value());
        }
    }

    // no perf issues here normally since add is called not that often
    protected static class FixedSizedMap extends LinkedHashMap<Long, Double> {
        protected FixedSizedMap() {
            super(MAX_SIZE);
        }

        protected FixedSizedMap(final Map<Long, Double> value) {
            super(value);
        }

        public synchronized void add(final double value) {
            put(System.currentTimeMillis(), value);
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
