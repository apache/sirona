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
import org.apache.sirona.configuration.ioc.IoCs;
import org.apache.sirona.store.gauge.GaugeDataStore;

import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class DefaultGaugeManager implements GaugeManager {
    private static final Logger LOGGER = Logger.getLogger(DefaultGaugeManager.class.getName());

    private final Map<Gauge, Timer> timers = new ConcurrentHashMap<Gauge, Timer>();

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
    }

    @Override
    public void addGauge(final Gauge gauge) {
        final Role role = gauge.role();

        final ClassLoader old = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(gauge.getClass().getClassLoader());
        try {
            final Timer timer = new Timer("gauge-" + role.getName() + "-timer", true); // this starts a thread so ensure the loader is the right one
            timers.put(gauge, timer);
            timer.scheduleAtFixedRate(new GaugeTask(gauge), 0, gauge.period());
        } finally {
            Thread.currentThread().setContextClassLoader(old);
        }
    }

    private static class GaugeTask extends TimerTask {
        private final Gauge gauge;
        private final GaugeDataStore store;

        public GaugeTask(final Gauge gauge) {
            this.store = IoCs.getInstance(GaugeDataStore.class);
            this.gauge = gauge;
        }

        @Override
        public void run() {
            try {
                final long time = System.currentTimeMillis();
                final double value = gauge.value();

                // role could be dynamic...even if not advised
                store.addToGauge(gauge.role(), time, value);
            } catch (final Exception e) {
                LOGGER.log(Level.WARNING, e.getMessage(), e);
            }
        }
    }
}
