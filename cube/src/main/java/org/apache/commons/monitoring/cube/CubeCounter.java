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
package org.apache.commons.monitoring.cube;

import org.apache.commons.monitoring.counters.Counter;
import org.apache.commons.monitoring.counters.Unit;
import org.apache.commons.monitoring.store.CounterDataStore;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class CubeCounter implements Counter {
    private final Key key;
    private volatile long hits = 0;
    private volatile double sum = 0;
    private final AtomicInteger concurrency = new AtomicInteger(0);
    private volatile int maxConcurrency = 0;
    private Lock lock = new ReentrantLock();
    private final CounterDataStore dataStore;

    public CubeCounter(final Key key, final CounterDataStore store) {
        this.key = key;
        this.dataStore = store;
    }

    @Override
    public Key getKey() {
        return key;
    }

    @Override
    public void reset() {
        sum = 0;
        hits = 0;
    }

    @Override
    public void add(final double delta) {
        dataStore.addToCounter(this, delta);
    }

    public void addInternal(final double delta) { // should be called from a thread safe environment
        sum += delta;
        hits++;
    }

    @Override
    public void add(final double delta, final Unit unit) {
        add(key.getRole().getUnit().convert(delta, unit));
    }

    @Override
    public AtomicInteger currentConcurrency() {
        return concurrency;
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
    public long getHits() {
        return hits;
    }

    @Override
    public double getSum() {
        return sum;
    }

    public Lock getLock() {
        return lock;
    }

    @Override
    public double getMax() {
        throw new UnsupportedOperationException();
    }

    @Override
    public double getMin() {
        throw new UnsupportedOperationException();
    }

    @Override
    public double getStandardDeviation() {
        throw new UnsupportedOperationException();
    }

    @Override
    public double getVariance() {
        throw new UnsupportedOperationException();
    }

    @Override
    public double getMean() {
        throw new UnsupportedOperationException();
    }

    @Override
    public double getGeometricMean() {
        throw new UnsupportedOperationException();
    }

    @Override
    public double getSumOfLogs() {
        throw new UnsupportedOperationException();
    }

    @Override
    public double getSumOfSquares() {
        throw new UnsupportedOperationException();
    }
}
