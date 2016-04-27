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

import org.apache.sirona.store.counter.CounterDataStore;

import javax.management.ObjectName;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public abstract class LockableCounter implements Counter {
    private final Key key;
    private final CounterDataStore dataStore;
    protected final ReadWriteLock lock = new ReentrantReadWriteLock();

    private final AtomicInteger concurrency = new AtomicInteger(0);
    private volatile int maxConcurrency = 0;
    private ObjectName jmx = null;

    protected LockableCounter(final Key key, final CounterDataStore dataStore) {
        this.key = key;
        this.dataStore = dataStore;
    }

    public abstract void addInternal(double delta);
    public abstract OptimizedStatistics getStatistics();

    @Override
    public void add(final double delta) {
        dataStore.addToCounter(this, delta);
    }

    @Override
    public void add(final double delta, final Unit deltaUnit) {
        add(getKey().getRole().getUnit().convert(delta, deltaUnit));
    }

    @Override
    public Key getKey() {
        return key;
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

    public void setJmx(final ObjectName jmx) {
        this.jmx = jmx;
    }

    public ObjectName getJmx() {
        return jmx;
    }

    public ReadWriteLock getLock() {
        return lock;
    }

    public void reset() {
        maxConcurrency = 0;
    }

    @Override
    public double getMax() {
        return getStatistics().getMax();
    }

    @Override
    public double getMin() {
        return getStatistics().getMin();
    }

    @Override
    public long getHits() {
        return getStatistics().getN();
    }

    @Override
    public double getSum() {
        return getStatistics().getSum();
    }

    @Override
    public double getStandardDeviation() {
        return getStatistics().getStandardDeviation();
    }

    @Override
    public double getVariance() {
        return getStatistics().getVariance();
    }

    @Override
    public double getMean() {
        return getStatistics().getMean();
    }

    @Override
    public double getSecondMoment() {
        return getStatistics().getSecondMoment();
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
        return getKey().equals(that.getKey());
    }

    @Override
    public int hashCode() {
        return getKey().hashCode();
    }
}
