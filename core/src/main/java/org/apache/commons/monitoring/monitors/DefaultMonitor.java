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

package org.apache.commons.monitoring.monitors;

import org.apache.commons.monitoring.Role;
import org.apache.commons.monitoring.configuration.Configuration;
import org.apache.commons.monitoring.counter.Counter;
import org.apache.commons.monitoring.counter.factory.CounterFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

public class DefaultMonitor implements Monitor {
    private static final CounterFactory METRIC_FACTORY = Configuration.newInstance(CounterFactory.class);

    private final ConcurrentMap<Role, Counter> metrics;
    private final AtomicInteger concurrency;
    private final Key key;
    private volatile int maxConcurrency = 0;

    public DefaultMonitor(final Key key) {
        this.key = key;
        this.metrics = new ConcurrentHashMap<Role, Counter>();
        this.concurrency = new AtomicInteger(0);
    }

    @Override
    public Key getKey() {
        return key;
    }

    @Override
    public Counter getCounter(final Role role) {
        if (role == null) {
            return null;
        }

        { // if existing use it
            final Counter existingCounter = metrics.get(role);
            if (existingCounter != null) {
                return existingCounter;
            }
        }

        // else create it
        final Counter counter = METRIC_FACTORY.newCounter(role);
        counter.setMonitor(this);

        final Counter previous = metrics.putIfAbsent(counter.getRole(), counter);
        if (previous == null) {
            METRIC_FACTORY.counterCreated(counter);
            return counter;
        }
        return previous;
    }

    @Override
    public Counter getCounter(final String role) {
        return getCounter(Role.getRole(role));
    }

    @Override
    public Collection<Role> getRoles() {
        return Collections.unmodifiableCollection(metrics.keySet());
    }

    @Override
    public Collection<Counter> getCounters() {
        return Collections.unmodifiableCollection(metrics.values());
    }

    @Override
    public AtomicInteger currentConcurrency() {
        return concurrency;
    }

    @Override
    public void reset() {
        for (final Counter counter : metrics.values()) {
            counter.reset();
        }
    }

    @Override
    public void updateConcurrency(int concurrency) {
        if (concurrency > maxConcurrency) {
            maxConcurrency = concurrency;
        }
    }

    @Override
    public int getMaxConcurrency() {
        return maxConcurrency;
    }

    @Override
    public int getConcurrency() {
        return currentConcurrency().get();
    }
}
