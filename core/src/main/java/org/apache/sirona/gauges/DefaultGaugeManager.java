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
import org.apache.sirona.configuration.Configuration;
import org.apache.sirona.gauges.jvm.CPUGauge;
import org.apache.sirona.gauges.jvm.UsedMemoryGauge;
import org.apache.sirona.store.gauge.GaugeDataStore;

import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

public final class DefaultGaugeManager implements GaugeManager {
    private final Map<Gauge, Timer> timers = new ConcurrentHashMap<Gauge, Timer>();
    private final GaugeDataStore store;

    public DefaultGaugeManager(final GaugeDataStore dataStore) {
        store = dataStore;
        if (Configuration.is(Configuration.CONFIG_PROPERTY_PREFIX + "core.gauge.activated", true)) {
            addGauge(new CPUGauge());
            addGauge(new UsedMemoryGauge());
        }
    }

    @Override
    public void stop() {
        for (final Timer timer : timers.values()) {
            timer.cancel();
        }
        timers.clear();
    }

    @Override
    public void stopGauge(final Gauge gauge) {
        final Timer timer = timers.remove(gauge);
        if (timer != null) {
            timer.cancel();
        }
        store.gaugeStopped(gauge.role());
    }

    @Override
    public void addGauge(final Gauge gauge) {
        final Role role = gauge.role();
        this.store.createOrNoopGauge(role);

        final ClassLoader old = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(gauge.getClass().getClassLoader());
        try {
            final Timer timer = new Timer("gauge-" + role.getName() + "-timer", true); // this starts a thread so ensure the loader is the right one
            timers.put(gauge, timer);
            timer.scheduleAtFixedRate(new GaugeTask(store, gauge), 0, gauge.period());
        } finally {
            Thread.currentThread().setContextClassLoader(old);
        }
    }

    private static class GaugeTask extends TimerTask {
        private final Gauge gauge;
        private final GaugeDataStore store;

        public GaugeTask(final GaugeDataStore store, final Gauge gauge) {
            this.store = store;
            this.gauge = gauge;
        }

        @Override
        public void run() {
            final long time = System.currentTimeMillis();
            final double value = gauge.value();

            // role could be dynamic...even if not advised
            store.addToGauge(gauge.role(), time, value);
        }
    }
}
