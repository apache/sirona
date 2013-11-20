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
import org.apache.sirona.configuration.ioc.Created;
import org.apache.sirona.configuration.ioc.Destroying;
import org.apache.sirona.counters.OptimizedStatistics;
import org.apache.sirona.store.BatchFuture;
import org.apache.sirona.util.DaemonThreadFactory;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class AggregatedGaugeDataStoreAdapter extends BatchGaugeDataStoreAdapter {
    private static final Logger LOGGER = Logger.getLogger(AggregatedGaugeDataStoreAdapter.class.getName());

    private final ConcurrentMap<Role, OptimizedStatistics> gauges = new ConcurrentHashMap<Role, OptimizedStatistics>();
    private BatchFuture scheduledAggregatedTask;

    protected abstract void pushAggregatedGauges(final Map<Role, Value> gauges);

    @Created // call it only when main impl not in delegated mode so use IoC lifecycle management
    public void initAggregated() {
        final String name = getClass().getSimpleName().toLowerCase(Locale.ENGLISH).replace("gaugedatastore", "") + ".aggregated";
        final long period = getPeriod(name);

        final ScheduledExecutorService ses = Executors.newSingleThreadScheduledExecutor(new DaemonThreadFactory(name + "-aggregated-gauge-schedule-"));
        final ScheduledFuture<?> future = ses.scheduleAtFixedRate(new PushGaugesTask(), period, period, TimeUnit.MILLISECONDS);
        scheduledAggregatedTask = new BatchFuture(ses, future);
    }

    @Destroying
    public void shutdown() {
        scheduledAggregatedTask.done();
    }

    @Override
    protected void pushGauges(Map<Role, Measure> gauges) {

    }

    @Override
    public void gaugeStopped(final Role gauge) {
        gauges.remove(gauge);
        super.gaugeStopped(gauge);
    }

    @Override
    public void addToGauge(final Role role, final long time, final double value) {
        OptimizedStatistics stat = gauges.get(role);
        if (stat == null) {
            stat = new OptimizedStatistics();
            final OptimizedStatistics existing = gauges.putIfAbsent(role, stat);
            if (existing != null) {
                stat = existing;
            }
        }
        stat.addValue(value);
    }

    private ConcurrentMap<Role, Value> copyAndClearGauges() {
        final ConcurrentMap<Role, OptimizedStatistics> copy = new ConcurrentHashMap<Role, OptimizedStatistics>();
        copy.putAll(gauges);
        gauges.clear();

        final ConcurrentMap<Role, Value> toPush = new ConcurrentHashMap<Role, Value>();
        for (final Map.Entry<Role, OptimizedStatistics> entry : copy.entrySet()) {
            toPush.put(entry.getKey(), new ValueImpl(entry.getValue()));
        }
        return toPush;
    }

    private class PushGaugesTask implements Runnable {
        @Override
        public void run() {
            try {
                pushAggregatedGauges(copyAndClearGauges());
            } catch (final Exception e) {
                LOGGER.log(Level.SEVERE, e.getMessage(), e);
            }
        }
    }

    private static class ValueImpl implements Value {
        private final OptimizedStatistics delegate;

        public ValueImpl(final OptimizedStatistics value) {
            delegate = value;
        }

        @Override
        public double getMean() {
            return delegate.getMean();
        }

        @Override
        public double getMax() {
            return delegate.getMax();
        }

        @Override
        public double getMin() {
            return delegate.getMin();
        }

        @Override
        public long getN() {
            return delegate.getN();
        }

        @Override
        public double getSum() {
            return delegate.getSum();
        }

        @Override
        public String toString() {
            return "ValueImpl{delegate=" + delegate + '}';
        }
    }
}
