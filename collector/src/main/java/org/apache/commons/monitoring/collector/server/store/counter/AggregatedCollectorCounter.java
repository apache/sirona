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
package org.apache.commons.monitoring.collector.server.store.counter;

import org.apache.commons.monitoring.collector.server.math.Aggregators;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.Lock;

public class AggregatedCollectorCounter extends CollectorCounter {
    private final ConcurrentMap<String, LeafCollectorCounter> aggregation = new ConcurrentHashMap<String, LeafCollectorCounter>(50);

    public AggregatedCollectorCounter(Key key) {
        super(key);
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
}
