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
import org.apache.commons.monitoring.gauges.DefaultGaugeManager;
import org.apache.commons.monitoring.gauges.Gauge;
import org.apache.commons.monitoring.stopwatches.CounterStopWatch;
import org.apache.commons.monitoring.stopwatches.StopWatch;
import org.apache.commons.monitoring.store.DataStore;

import java.util.Iterator;
import java.util.Map;

public class DefaultRepository implements Repository {
    private final DataStore dataStore;
    private final DefaultGaugeManager gaugeManager;

    public DefaultRepository() {
        this.dataStore = Configuration.newInstance(DataStore.class);
        this.gaugeManager = new DefaultGaugeManager(dataStore);
    }

    @Configuration.Destroying
    public void stopGaugeTimers() {
        gaugeManager.stop();
    }

    @Override
    public Counter getCounter(final Counter.Key key) {
        return dataStore.getOrCreateCounter(key);
    }

    @Override
    public void clear() {
        dataStore.clearCounters();
    }

    @Override
    public StopWatch start(final Counter monitor) {
        return new CounterStopWatch(monitor);
    }

    @Override
    public Iterator<Counter> iterator() {
        return dataStore.getCounters().iterator();
    }

    @Override
    public Map<Long, Double> getGaugeValues(final long start, final long end, final Role role) {
        return dataStore.getGaugeValues(start, end, role);
    }

    @Override
    public void addGauge(final Gauge gauge) {
        gaugeManager.addGauge(gauge);
    }

    @Override
    public void stopGauge(final Role role) {
        gaugeManager.stopGauge(role);
    }
}
