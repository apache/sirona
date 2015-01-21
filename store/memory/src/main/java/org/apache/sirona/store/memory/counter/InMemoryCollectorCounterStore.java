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

import org.apache.sirona.counters.Counter;
import org.apache.sirona.math.M2AwareStatisticalSummary;
import org.apache.sirona.store.counter.AggregatedCollectorCounter;
import org.apache.sirona.store.counter.CollectorCounterStore;
import org.apache.sirona.store.counter.LeafCollectorCounter;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class InMemoryCollectorCounterStore extends InMemoryCounterDataStore implements CollectorCounterStore
{
    private final ConcurrentMap<String, ConcurrentMap<Counter.Key, LeafCollectorCounter>> countersByMarker = new ConcurrentHashMap<String, ConcurrentMap<Counter.Key, LeafCollectorCounter>>();

    @Override
    public void update(final Counter.Key key, final String marker, final M2AwareStatisticalSummary stats, final int concurrency) {
        getOrCreateCounter(key, marker).update(stats, concurrency);
        getOrCreateCounter(key).update();
    }

    @Override
    public Collection<String> markers() {
        return countersByMarker.keySet();
    }

    @Override
    public Collection<? extends LeafCollectorCounter> getCounters(final String marker) {
        return countersByMarker.get(marker).values();
    }

    @Override
    public LeafCollectorCounter getOrCreateCounter(final Counter.Key key, final String marker) {
        ConcurrentMap<Counter.Key, LeafCollectorCounter> subCounters = countersByMarker.get(marker);
        if (subCounters == null) {
            final ConcurrentMap<Counter.Key, LeafCollectorCounter> map = new ConcurrentHashMap<Counter.Key, LeafCollectorCounter>(50);
            final ConcurrentMap<Counter.Key, LeafCollectorCounter> existing = countersByMarker.putIfAbsent(marker, map);
            if (existing != null) {
                subCounters = existing;
            } else {
                subCounters = map;
            }
        }

        LeafCollectorCounter counter = subCounters.get(key);
        if (counter == null) {
            counter = new LeafCollectorCounter(key);
            final LeafCollectorCounter previous = subCounters.putIfAbsent(key, counter);
            if (previous != null) {
                counter = previous;
            }
            final AggregatedCollectorCounter aggregate = AggregatedCollectorCounter.class.cast(super.getOrCreateCounter(key));
            aggregate.addIfMissing(marker, counter);
        }

        return counter;
    }

    @Override
    protected Counter newCounter(final Counter.Key key) {
        return new AggregatedCollectorCounter(key);
    }

    @Override
    public AggregatedCollectorCounter getOrCreateCounter(final Counter.Key key) {
        return AggregatedCollectorCounter.class.cast(super.getOrCreateCounter(key));
    }

    @Override
    public void clearCounters() {
        for (final Map.Entry<String, ConcurrentMap<Counter.Key, LeafCollectorCounter>> maps : countersByMarker.entrySet()) {
            maps.getValue().clear();
        }
        countersByMarker.clear();
        super.clearCounters();
    }

    @Override
    public void addToCounter(final Counter defaultCounter, final double delta) {
        throw new UnsupportedOperationException("shouldn't be used");
    }
}
