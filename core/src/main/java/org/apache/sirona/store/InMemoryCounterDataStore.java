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

import org.apache.sirona.counters.Counter;
import org.apache.sirona.counters.DefaultCounter;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.Lock;

public class InMemoryCounterDataStore implements CounterDataStore {
    protected final ConcurrentMap<Counter.Key, Counter> counters = newCounterMap();

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
            }
        }
        return counter;
    }

    @Override
    public void clearCounters() {
        counters.clear();
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
}
