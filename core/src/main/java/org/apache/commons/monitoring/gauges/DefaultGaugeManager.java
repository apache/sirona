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
import org.apache.commons.monitoring.store.DataStore;

import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

public final class DefaultGaugeManager implements GaugeManager {
    private final Map<Role, Timer> timers = new ConcurrentHashMap<Role, Timer>();
    private final GaugeObserver[] observers = Configuration.newInstances(GaugeObserver.class);
    private final DataStore store;

    public DefaultGaugeManager(final DataStore dataStore) {
        store = dataStore;
    }

    @Override
    public void stop() {
        for (final Timer timer : timers.values()) {
            timer.cancel();
        }
        timers.clear();
    }

    @Override
    public void stopGauge(final Role role) {
        final Timer timer = timers.remove(role);
        if (timer != null) {
            timer.cancel();
        }
    }

    @Override
    public void addGauge(final Gauge gauge) {
        final Role role = gauge.role();

        this.store.createOrNoopGauge(role);

        final Timer timer = new Timer("gauge-" + role.getName() + "-timer", true);
        timers.put(role, timer);
        timer.scheduleAtFixedRate(new GaugeTask(store, gauge, observers), 0, gauge.period());
    }

    private static class GaugeTask extends TimerTask {
        private final Gauge gauge;
        private final DataStore store;
        private final GaugeObserver[] observers; // acceptable since gauges are not fast update entities

        public GaugeTask(final DataStore store, final Gauge gauge, final GaugeObserver[] observers) {
            this.store = store;
            this.gauge = gauge;
            this.observers = observers;
        }

        @Override
        public void run() {
            final long time = System.currentTimeMillis();
            final double value = gauge.value();

            store.addToGauge(gauge, time, value);
            for (final GaugeObserver observer : observers) {
                try {
                    observer.observe(gauge, time, value);
                } catch (final Exception e) {
                    // no-op: don't make the thread failling because of an observer
                }
            }
        }
    }
}
