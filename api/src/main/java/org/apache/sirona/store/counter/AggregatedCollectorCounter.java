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

import org.apache.sirona.counters.AggregatedCounter;
import org.apache.sirona.counters.Counter;
import org.apache.sirona.math.Aggregators;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.Lock;

public class AggregatedCollectorCounter extends CollectorCounter implements AggregatedCounter {
    private final ConcurrentMap<String, LeafCollectorCounter> aggregation = new ConcurrentHashMap<String, LeafCollectorCounter>(50);

    public AggregatedCollectorCounter(final Key key) {
        super(key);
    }

    public AggregatedCollectorCounter(final Key key, final Map<String, LeafCollectorCounter> counters) {
        super(key);
        aggregation.putAll(counters);
        update();
    }

    public void update() {
        final Lock workLock = lock.writeLock();
        workLock.lock();
        try {
            final Collection<LeafCollectorCounter> counters = aggregation.values();
            statistics = Aggregators.aggregate(counters);
            concurrency.set(computeConcurrency(counters));
            updateConcurrency(concurrency.get());
        } finally {
            workLock.unlock();
        }
    }

    public void addIfMissing(final String marker, final LeafCollectorCounter counter) {
        aggregation.putIfAbsent(marker, counter);
    }

    private static int computeConcurrency(final Collection<LeafCollectorCounter> counters) {
        int i = 0;
        for (final LeafCollectorCounter counter : counters) {
            i += counter.currentConcurrency().get();
        }
        return i;
    }

    @Override
    public Map<String, ? extends Counter> aggregated() {
        return aggregation;
    }
}
