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
package org.apache.sirona.collector.server.store.counter;

import org.apache.sirona.math.M2AwareStatisticalSummary;
import org.apache.sirona.counters.Counter;
import org.apache.sirona.counters.Unit;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public abstract class CollectorCounter implements Counter {
    protected final Key key;
    protected final ReadWriteLock lock = new ReentrantReadWriteLock();
    protected volatile int maxConcurrency = 0;
    protected volatile AtomicInteger concurrency = new AtomicInteger(0);
    protected M2AwareStatisticalSummary statistics;

    public CollectorCounter(final Key key) {
        this.key = key;
        reset();
    }

    @Override
    public Key getKey() {
        return key;
    }

    @Override
    public void reset() {
        final Lock workLock = lock.writeLock();
        workLock.lock();
        try {
            statistics = new M2AwareStatisticalSummary(Double.NaN, Double.NaN, 0, Double.NaN, Double.NaN, Double.NaN, Double.NaN);
        } finally {
            workLock.unlock();
        }
    }

    @Override
    public void add(final double delta) {
        // no-op: this counter is updated through update method
    }

    @Override
    public void add(final double delta, Unit unit) {
        add(key.getRole().getUnit().convert(delta, unit));
    }

    @Override
    public AtomicInteger currentConcurrency() {
        return concurrency;
    }

    @Override
    public void updateConcurrency(final int concurrency) {
        if (concurrency > maxConcurrency) {
            final Lock workLock = lock.writeLock();
            workLock.lock();
            try {
                maxConcurrency = concurrency;
            } finally {
                workLock.unlock();
            }
        }
    }

    @Override
    public int getMaxConcurrency() {
        final Lock workLock = lock.readLock();
        workLock.lock();
        try {
            return maxConcurrency;
        } finally {
            workLock.unlock();
        }
    }

    @Override
    public double getMax() {
        final Lock workLock = lock.readLock();
        workLock.lock();
        try {
            return statistics.getMax();
        } finally {
            workLock.unlock();
        }
    }

    @Override
    public double getMin() {
        final Lock workLock = lock.readLock();
        workLock.lock();
        try {
            return statistics.getMin();
        } finally {
            workLock.unlock();
        }
    }

    @Override
    public long getHits() {
        final Lock workLock = lock.readLock();
        workLock.lock();
        try {
            return statistics.getN();
        } finally {
            workLock.unlock();
        }
    }

    @Override
    public double getSum() {
        final Lock workLock = lock.readLock();
        workLock.lock();
        try {
            return statistics.getSum();
        } finally {
            workLock.unlock();
        }
    }

    @Override
    public double getStandardDeviation() {
        final Lock workLock = lock.readLock();
        workLock.lock();
        try {
            return Math.sqrt(statistics.getVariance());
        } finally {
            workLock.unlock();
        }
    }

    @Override
    public double getVariance() {
        final Lock workLock = lock.readLock();
        workLock.lock();
        try {
            return statistics.getVariance();
        } finally {
            workLock.unlock();
        }
    }

    @Override
    public double getMean() {
        final Lock workLock = lock.readLock();
        workLock.lock();
        try {
            return statistics.getMean();
        } finally {
            workLock.unlock();
        }
    }

    @Override
    public double getSecondMoment() {
        final Lock workLock = lock.readLock();
        workLock.lock();
        try {
            return statistics.getSecondMoment();
        } finally {
            workLock.unlock();
        }
    }
}
