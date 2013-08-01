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

package org.apache.commons.monitoring.repositories;

import org.apache.commons.monitoring.counter.Counter;
import org.apache.commons.monitoring.counter.DefaultCounter;
import org.apache.commons.monitoring.stopwatches.CounterStopWatch;
import org.apache.commons.monitoring.stopwatches.StopWatch;

import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class DefaultRepository implements Repository {
    private final ConcurrentMap<Counter.Key, Counter> counters = new ConcurrentHashMap<Counter.Key, Counter>(50);

    protected Counter newCounterInstance(final Counter.Key key) {
        return new DefaultCounter(key);
    }

    protected Counter register(final Counter monitor) {
        return counters.putIfAbsent(monitor.getKey(), monitor);
    }

    @Override
    public Counter getCounter(final Counter.Key key) {
        Counter monitor = counters.get(key);
        if (monitor == null) {
            monitor = newCounterInstance(key);
            final Counter previous = register(monitor);
            if (previous != null) {
                monitor = previous;
            }
        }
        return monitor;
    }

    @Override
    public void clear() {
        counters.clear();
    }

    @Override
    public void reset() {
        for (final Counter monitor : counters.values()) {
            monitor.reset();
        }
    }

    @Override
    public StopWatch start(final Counter monitor) {
        return new CounterStopWatch(monitor);
    }

    @Override
    public Iterator<Counter> iterator() {
        return counters.values().iterator();
    }
}
