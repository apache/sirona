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

import java.util.concurrent.locks.Lock;

public class DefaultCounter extends LockableCounter {
    protected final OptimizedStatistics statistics;

    public DefaultCounter(final Key key, final CounterDataStore store) {
        this(key, store, new OptimizedStatistics());
    }
    public DefaultCounter(final Key key, final CounterDataStore store, final OptimizedStatistics statistics) {
        super(key, store);
        this.statistics = statistics;
    }

    public void addInternal(final double delta) {
        final Lock lock = getLock().writeLock();
        lock.lock();
        try {
            statistics.addValue(delta);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void reset() {
        statistics.clear();
        super.reset();
    }

    @Override
    public double getMax() {
        final Lock rl = getLock().readLock();
        rl.lock();
        try {
            return statistics.getMax();
        } finally {
            rl.unlock();
        }
    }

    @Override
    public double getMin() {
        final Lock rl = getLock().readLock();
        rl.lock();
        try {
            return statistics.getMin();
        } finally {
            rl.unlock();
        }
    }

    @Override
    public double getSum() {
        final Lock rl = getLock().readLock();
        rl.lock();
        try {
            return statistics.getSum();
        } finally {
            rl.unlock();
        }
    }

    @Override
    public double getStandardDeviation() {
        final Lock rl = getLock().readLock();
        rl.lock();
        try {
            return statistics.getStandardDeviation();
        } finally {
            rl.unlock();
        }
    }

    @Override
    public double getVariance() {
        final Lock rl = getLock().readLock();
        rl.lock();
        try {
            return statistics.getVariance();
        } finally {
            rl.unlock();
        }
    }

    @Override
    public double getMean() {
        final Lock rl = getLock().readLock();
        rl.lock();
        try {
            return statistics.getMean();
        } finally {
            rl.unlock();
        }
    }

    @Override
    public double getSecondMoment() {
        final Lock rl = getLock().readLock();
        rl.lock();
        try {
            return statistics.getSecondMoment();
        } finally {
            rl.unlock();
        }
    }

    @Override
    public long getHits() {
        final Lock rl = getLock().readLock();
        rl.lock();
        try {
            return statistics.getN();
        } finally {
            rl.unlock();
        }
    }

    @Override
    public OptimizedStatistics getStatistics() {
        final Lock rl = getLock().readLock();
        rl.lock();
        try {
            return statistics.copy();
        } finally {
            rl.unlock();
        }
    }

    @Override
    public String toString() {
        return "DefaultCounter{" +
            "concurrency=" + currentConcurrency().get() +
            ", key=" + getKey() +
            ", maxConcurrency=" + getMaxConcurrency() +
            ", statistics=" + statistics +
            '}';
    }
}
