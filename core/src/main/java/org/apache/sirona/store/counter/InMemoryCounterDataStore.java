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
package org.apache.sirona.store.counter;

import org.apache.sirona.configuration.Configuration;
import org.apache.sirona.counters.Counter;
import org.apache.sirona.counters.DefaultCounter;
import org.apache.sirona.counters.MetricData;
import org.apache.sirona.gauges.Gauge;
import org.apache.sirona.gauges.counter.CounterGauge;
import org.apache.sirona.repositories.Repository;

import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.Lock;

public class InMemoryCounterDataStore implements CounterDataStore {
    protected final boolean gauged = Configuration.is(Configuration.CONFIG_PROPERTY_PREFIX + "counter.with-gauge", false);
    protected final ConcurrentMap<Counter.Key, Counter> counters = newCounterMap();
    protected final Collection<Gauge> gauges = new LinkedList<Gauge>();

    protected ConcurrentMap<Counter.Key, Counter> newCounterMap() {
        return new ConcurrentHashMap<Counter.Key, Counter>(50);
    }

    protected Counter newCounter(final Counter.Key key) {
        return new DefaultCounter(key, this);
    }

    @Override
    public Counter getOrCreateCounter(final Counter.Key key) {
        Counter counter = counters.get(key);
        if (counter == null) {
            counter = newCounter(key);
            final Counter previous = counters.putIfAbsent(key, counter);
            if (previous != null) {
                counter = previous;
            } else if (gauged) {
                final Values values = new Values(counter);
                newGauge(new SyncCounterGauge(counter, MetricData.Sum, values));
                newGauge(new SyncCounterGauge(counter, MetricData.Max, values));
                newGauge(new SyncCounterGauge(counter, MetricData.Hits, values));
            }
        }
        return counter;
    }

    private void newGauge(final Gauge gauge) {
        Repository.INSTANCE.addGauge(gauge);
        synchronized (gauges) {
            gauges.add(gauge);
        }
    }

    @Override
    public void clearCounters() {
        counters.clear();
        synchronized (gauges) {
            for (final Gauge g : gauges) {
                Repository.INSTANCE.stopGauge(g);
            }
            gauges.clear();
        }
    }

    @Override
    public Collection<Counter> getCounters() {
        return counters.values();
    }

    @Override
    public void addToCounter(final Counter counter, final double delta) {
        if (!DefaultCounter.class.isInstance(counter)) {
            throw new IllegalArgumentException(getClass().getName() + " only supports " + DefaultCounter.class.getName());
        }

        final DefaultCounter defaultCounter = DefaultCounter.class.cast(counter);
        final Lock lock = defaultCounter.getLock().writeLock();
        lock.lock();
        try {
            defaultCounter.addInternal(delta);
        } finally {
            lock.unlock();
        }
    }

    private static class SyncCounterGauge extends CounterGauge {
        private final Values values;

        private SyncCounterGauge(final Counter counter, final MetricData metric, final Values values) {
            super(counter, metric);
            this.values = values;
        }

        @Override
        public double value() {
            values.take();
            if (MetricData.Hits == metric) {
                return values.getHits();
            } else if (MetricData.Sum == metric) {
                return values.getSum();
            } else if (MetricData.Max == metric) {
                return values.getMax();
            }
            throw new IllegalArgumentException(metric.name());
        }
    }

    private static class Values {
        private double max;
        private double sum;
        private double hits;

        private int called = -1;

        private final Counter counter;

        private Values(final Counter counter) {
            this.counter = counter;
        }

        public synchronized void take() {
            if (called == 3 || called == -1) {
                max = counter.getMax();
                sum = counter.getSum();
                hits = counter.getHits();
                counter.reset();
                called = 0;
            }
            called++;
        }

        public double getMax() {
            return max;
        }

        public double getSum() {
            return sum;
        }

        public double getHits() {
            return hits;
        }
    }
}
