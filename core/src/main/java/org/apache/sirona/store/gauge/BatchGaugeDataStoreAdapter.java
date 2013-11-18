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
package org.apache.sirona.store.gauge;

import org.apache.sirona.Role;
import org.apache.sirona.configuration.Configuration;
import org.apache.sirona.configuration.ioc.Created;
import org.apache.sirona.configuration.ioc.Destroying;
import org.apache.sirona.gauges.Gauge;
import org.apache.sirona.gauges.GaugeDataStoreAdapter;
import org.apache.sirona.store.BatchFuture;
import org.apache.sirona.util.DaemonThreadFactory;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class BatchGaugeDataStoreAdapter extends GaugeDataStoreAdapter {
    private static final Logger LOGGER = Logger.getLogger(BatchGaugeDataStoreAdapter.class.getName());

    protected BatchFuture scheduledTask;

    @Created // call it only when main impl not in delegated mode so use IoC lifecycle management
    public void initBatch() {
        final String name = getClass().getSimpleName().toLowerCase(Locale.ENGLISH).replace("gaugedatastore", "");
        final long period = getPeriod(name);

        final ScheduledExecutorService ses = Executors.newSingleThreadScheduledExecutor(new DaemonThreadFactory(name + "-gauge-schedule-"));
        final ScheduledFuture<?> future = ses.scheduleAtFixedRate(new PushGaugesTask(), period, period, TimeUnit.MILLISECONDS);
        scheduledTask = new BatchFuture(ses, future);
    }

    protected int getPeriod(final String name) {
        return Configuration.getInteger(Configuration.CONFIG_PROPERTY_PREFIX + name + ".gauge.period",
            Configuration.getInteger(Configuration.CONFIG_PROPERTY_PREFIX + name + ".period", 60000));
    }

    @Destroying
    public void shutdown() {
        scheduledTask.done();
    }

    protected abstract void pushGauges(final Map<Role, Measure> gauges);

    protected Map<Role, Measure> snapshot() {
        final long ts = System.currentTimeMillis();
        final Map<Role, Measure> snapshot = new HashMap<Role, Measure>();
        for (final Gauge gauge : gauges.values()) {
            final Role role = gauge.role();
            final double value = gauge.value();

            addToGauge(role, ts, value);
            snapshot.put(role, new Measure(ts, value));
        }
        return snapshot;
    }

    private class PushGaugesTask implements Runnable {
        @Override
        public void run() {
            try {
                pushGauges(snapshot());
            } catch (final Exception e) {
                LOGGER.log(Level.SEVERE, e.getMessage(), e);
            }
        }
    }

    public static class Measure {
        private long time;
        private double value;

        private Measure(final long time, final double value) {
            this.time = time;
            this.value = value;
        }

        public long getTime() {
            return time;
        }

        public double getValue() {
            return value;
        }

        @Override
        public String toString() {
            return "Measure{" +
                "time=" + time +
                ", value=" + value +
                '}';
        }
    }
}
