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

import org.apache.commons.monitoring.Role;
import org.apache.commons.monitoring.configuration.Configuration;
import org.apache.commons.monitoring.counters.Counter;
import org.apache.commons.monitoring.counters.DefaultCounter;
import org.apache.commons.monitoring.gauges.DefaultGaugeRepository;
import org.apache.commons.monitoring.stopwatches.CounterStopWatch;
import org.apache.commons.monitoring.stopwatches.StopWatch;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class DefaultRepository implements Repository {
    private final DefaultGaugeRepository gaugeRepository = new DefaultGaugeRepository();
    private final ConcurrentMap<Counter.Key, Counter> counters = new ConcurrentHashMap<Counter.Key, Counter>(50);

    @Configuration.Created
    public void startGaugeTimers() {
        gaugeRepository.start(null); // no persistence
    }

    @Configuration.Destroying
    public void stopGaugeTimers() {
        gaugeRepository.stop();
    }

    @Override
    public Counter getCounter(final Counter.Key key) {
        Counter monitor = counters.get(key);
        if (monitor == null) {
            monitor = new DefaultCounter(key);
            final Counter previous = counters.putIfAbsent(key, monitor);
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
    public StopWatch start(final Counter monitor) {
        return new CounterStopWatch(monitor);
    }

    @Override
    public Iterator<Counter> iterator() {
        return counters.values().iterator();
    }

    @Override
    public Map<Long, Double> getGaugeValues(final Role role) {
        return gaugeRepository.getValues(role);
    }

    @Override
    public void stopGauge(final Role role) {
        gaugeRepository.stopGauge(role);
    }
}
