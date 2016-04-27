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
package org.apache.sirona.store.memory.counter;

import org.apache.sirona.counters.LockableCounter;
import org.apache.sirona.counters.OptimizedStatistics;
import org.apache.sirona.store.counter.CounterDataStore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;

// exponential decay sampling implementation
// inspired from http://dimacs.rutgers.edu/~graham/pubs/papers/fwddecay.pdf
//
// @Experimental, likely needs some more love for m2 computation
public class ExponentialDecayCounter extends LockableCounter {
    public static final double ACCEPTABLE_DEFAULT_ALPHA = 0.015;
    public static final int ACCEPTABLE_DEFAULT_SIZE = 512;
    public static final long ACCEPTABLE_STATISTICS_REFRESH_SECONDS = 5;

    private final double alpha;
    private final double samplingSize;
    private final long refreshStatInterval;

    private final AtomicInteger currentCount = new AtomicInteger();
    private volatile long recomputeAt;
    private volatile long becameAt;
    private volatile long computedStatsAt;
    private final ConcurrentSkipListMap<Double, Double> values = new ConcurrentSkipListMap<Double, Double>();
    private volatile OptimizedStatistics currentStats;

    public ExponentialDecayCounter(final Key key, final CounterDataStore store,
                                   final double alpha, final int samplingSize, final long refreshStatInterval) {
        super(key, store);
        this.alpha = alpha;
        this.samplingSize = samplingSize;
        this.refreshStatInterval = refreshStatInterval;
        reset();
    }

    protected long seconds() {
        return TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());
    }

    @Override
    public void addInternal(final double delta) {
        final long now = seconds();

        final Lock rl = getLock().readLock();
        rl.lock();
        try {
            final double priority = Math.exp(alpha * (now - becameAt)) / Math.random();
            if (currentCount.incrementAndGet() > samplingSize) { // we need to remove 1 sample to respect samplingSize
                double head = values.firstKey();
                if (priority > head && values.putIfAbsent(priority, delta) == null) {
                    while (values.remove(values.firstKey()) == null) {
                        // no-op
                    }
                }
            } else {
                values.put(priority, delta);
            }
        } finally {
            rl.unlock();
        }

        if (now >= recomputeAt) {
            if (recomputeAt == now) {
                return;
            }
            final Lock lock = getLock().writeLock();
            lock.lock();
            try {
                final long currentLoopBecame = becameAt;
                becameAt = now;
                recomputeAt = nextComputation();
                final long timeDiff = becameAt - currentLoopBecame;

                for (final Double priority : new ArrayList<Double>(values.keySet())) {
                    values.put(priority * Math.exp(-alpha * timeDiff), values.remove(priority));
                }
            } finally {
                lock.unlock();
            }
        }
    }

    @Override
    public void reset() {
        final long now = seconds();
        final Lock lock = getLock().writeLock();
        lock.lock();
        try {
            values.clear();
            this.currentCount.set(0);
            this.becameAt = now;
            this.recomputeAt = nextComputation();
            super.reset();
        } finally {
            lock.unlock();
        }
    }

    private long nextComputation() {
        return this.becameAt + TimeUnit.MILLISECONDS.toSeconds(TimeUnit.HOURS.toMillis(1));
    }

    @Override
    public OptimizedStatistics getStatistics() {
        if (computedStatsAt != 0 && System.currentTimeMillis() - computedStatsAt < refreshStatInterval) {
            return currentStats;
        }

        final LazyOptimizedStatistics stat;
        final Lock lock = getLock().readLock();
        lock.lock();
        try {
            stat = new LazyOptimizedStatistics(new HashMap<Double, Double>(values));
            computedStatsAt = seconds();
        } finally {
            lock.unlock();
        }

        stat.init(); // out of lock

        return (currentStats = stat);
    }

    @Override
    public String toString() {
        return "ExponentialDecayCounter{" +
                "key=" + getKey() +
                ", stats=" + getStatistics() +
                '}';
    }

    private static class LazyOptimizedStatistics extends OptimizedStatistics {
        private final Map<Double, Double> values;

        private LazyOptimizedStatistics(final Map<Double, Double> values) {
            this.values = values;
        }

        @Override
        public OptimizedStatistics addValue(final double value) {
            throw new UnsupportedOperationException();
        }

        private void init() {
            if (values.isEmpty()) {
                return;
            }

            // computed stats init
            n = values.size();
            min = Double.NaN;
            max = Double.NaN;
            m1 = 0;
            m2 = 0;
            sum = 0;

            // sum priorities
            double prioritySum = 0;
            for (final Double priority : values.keySet()) {
                prioritySum += priority;
            }

            { // normalise priorities
                for (final Double priority : new ArrayList<Double>(values.keySet())) {
                    values.put(priority / prioritySum, values.remove(priority));
                }
            }
            { // min/max
                for (final Double val : new ArrayList<Double>(values.values())) {
                    if (Double.isNaN(min)) { // init
                        max = min = val;
                    } else {
                        if (min > val) {
                            min = val;
                        }
                        if (max < val) {
                            max = val;
                        }
                    }
                }
            }
            { // mean
                for (final Map.Entry<Double, Double> priority : values.entrySet()) {
                    m1 += priority.getValue() * priority.getKey();
                }
            }
            if (values.size() > 1) { // variance
                for (final Map.Entry<Double, Double> priority : values.entrySet()) {
                    m2 += Math.pow(priority.getValue() - m1, 2) * priority.getKey();
                }
                m2 *= (n - 1);
            }
            { // sum (doesn't represent much - mathematically wrong - but gives an idea)
                for (final Double v : values.values()) {
                    sum += v;
                }
            }

            values.clear();
        }
    }
}
