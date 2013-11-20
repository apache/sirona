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
package org.apache.sirona.counters;

import org.apache.commons.math3.stat.descriptive.StatisticalSummary;
import org.apache.sirona.store.counter.CounterDataStore;

import javax.management.ObjectName;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class DefaultCounter implements Counter {
    private final AtomicInteger concurrency = new AtomicInteger(0);
    private final Key key;
    private final CounterDataStore dataStore;
    private volatile int maxConcurrency = 0;
    protected final OptimizedStatistics statistics;
    protected final ReadWriteLock lock = new ReentrantReadWriteLock();
    private ObjectName jmx = null;

    public DefaultCounter(final Key key, final CounterDataStore store) {
        this.key = key;
        this.dataStore = store;

        this.statistics = new OptimizedStatistics();
    }

    public void addInternal(final double delta) { // should be called from a thread safe environment
        statistics.addValue(delta);
    }

    @Override
    public void updateConcurrency(final int concurrency) {
        if (concurrency > maxConcurrency) {
            maxConcurrency = concurrency;
        }
    }

    @Override
    public int getMaxConcurrency() {
        return maxConcurrency;
    }

    @Override
    public AtomicInteger currentConcurrency() {
        return concurrency;
    }

    @Override
    public Key getKey() {
        return key;
    }

    @Override
    public void reset() {
        statistics.clear();
        concurrency.set(0);
    }

    @Override
    public void add(final double delta) {
        dataStore.addToCounter(this, delta);
    }

    @Override
    public void add(final double delta, final Unit deltaUnit) {
        add(key.getRole().getUnit().convert(delta, deltaUnit));
    }

    @Override
    public double getMax() {
        final Lock rl = lock.readLock();
        rl.lock();
        try {
            return statistics.getMax();
        } finally {
            rl.unlock();
        }
    }

    @Override
    public double getMin() {
        final Lock rl = lock.readLock();
        rl.lock();
        try {
            return statistics.getMin();
        } finally {
            rl.unlock();
        }
    }

    @Override
    public double getSum() {
        final Lock rl = lock.readLock();
        rl.lock();
        try {
            return statistics.getSum();
        } finally {
            rl.unlock();
        }
    }

    @Override
    public double getStandardDeviation() {
        final Lock rl = lock.readLock();
        rl.lock();
        try {
            return statistics.getStandardDeviation();
        } finally {
            rl.unlock();
        }
    }

    @Override
    public double getVariance() {
        final Lock rl = lock.readLock();
        rl.lock();
        try {
            return statistics.getVariance();
        } finally {
            rl.unlock();
        }
    }

    @Override
    public double getMean() {
        final Lock rl = lock.readLock();
        rl.lock();
        try {
            return statistics.getMean();
        } finally {
            rl.unlock();
        }
    }

    @Override
    public double getSecondMoment() {
        final Lock rl = lock.readLock();
        rl.lock();
        try {
            return statistics.getSecondMoment();
        } finally {
            rl.unlock();
        }
    }

    @Override
    public long getHits() {
        final Lock rl = lock.readLock();
        rl.lock();
        try {
            return statistics.getN();
        } finally {
            rl.unlock();
        }
    }

    public StatisticalSummary getStatistics() {
        final Lock rl = lock.readLock();
        rl.lock();
        try {
            return statistics.copy();
        } finally {
            rl.unlock();
        }
    }

    public ReadWriteLock getLock() {
        return lock;
    }

    public void setJmx(final ObjectName jmx) {
        this.jmx = jmx;
    }

    public ObjectName getJmx() {
        return jmx;
    }

    @Override
    public String toString() {
        return "DefaultCounter{" +
            "concurrency=" + concurrency +
            ", key=" + key +
            ", dataStore=" + dataStore +
            ", maxConcurrency=" + maxConcurrency +
            ", statistics=" + statistics +
            '}';
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!Counter.class.isInstance(o)) {
            return false;
        }

        final Counter that = Counter.class.cast(o);
        return key.equals(that.getKey());
    }

    @Override
    public int hashCode() {
        return key.hashCode();
    }
}
