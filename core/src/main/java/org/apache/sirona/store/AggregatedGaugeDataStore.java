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
package org.apache.sirona.store;

import org.apache.commons.math3.stat.descriptive.StatisticalSummary;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.apache.sirona.Role;
import org.apache.sirona.configuration.Configuration;
import org.apache.sirona.util.DaemonThreadFactory;

import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public abstract class AggregatedGaugeDataStore implements GaugeDataStore {
    private final ConcurrentMap<Role, SummaryStatistics> gauges = new ConcurrentHashMap<Role, SummaryStatistics>();

    protected final BatchFuture scheduledTask;

    public AggregatedGaugeDataStore() {
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

    @Configuration.Destroying
    public void shutdown() {
        scheduledTask.done();
    }

    protected abstract void pushGauges(final Map<Role, Value> gauges);

    @Override
    public Map<Long, Double> getGaugeValues(final GaugeValuesRequest gaugeValuesRequest) {
        return Collections.emptyMap(); // when using graphite we expect the user to use Graphite to render metrics
    }

    @Override
    public void createOrNoopGauge(Role role) {
        // no-op
    }

    @Override
    public void addToGauge(final Role role, final long time, final double value) {
        SummaryStatistics stat = gauges.get(role);
        if (stat == null) {
            stat = new SummaryStatistics();
            final SummaryStatistics existing = gauges.putIfAbsent(role, stat);
            if (existing != null) {
                stat = existing;
            }
        }
        stat.addValue(value);
    }

    private ConcurrentMap<Role, Value> copyAndClearGauges() {
        final ConcurrentMap<Role, SummaryStatistics> copy = new ConcurrentHashMap<Role, SummaryStatistics>();
        copy.putAll(gauges);
        gauges.clear();

        final ConcurrentMap<Role, Value> toPush = new ConcurrentHashMap<Role, Value>();
        for (final Map.Entry<Role, SummaryStatistics> entry : copy.entrySet()) {
            toPush.put(entry.getKey(), new ValueImpl(entry.getValue()));
        }
        return toPush;
    }

    private class PushGaugesTask implements Runnable {
        @Override
        public void run() {
            pushGauges(copyAndClearGauges());
        }
    }

    private static class ValueImpl implements Value {
        private final SummaryStatistics delegate;

        public ValueImpl(final SummaryStatistics value) {
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
    }
}
