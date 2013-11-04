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
package org.apache.sirona.repositories;

import org.apache.sirona.MonitoringException;
import org.apache.sirona.Role;
import org.apache.sirona.configuration.Configuration;
import org.apache.sirona.store.CollectorCounterStore;
import org.apache.sirona.counters.Counter;
import org.apache.sirona.store.CollectorGaugeDataStore;
import org.apache.sirona.gauges.DefaultGaugeManager;
import org.apache.sirona.gauges.Gauge;
import org.apache.sirona.stopwatches.CounterStopWatch;
import org.apache.sirona.stopwatches.StopWatch;
import org.apache.sirona.store.CommonGaugeDataStore;
import org.apache.sirona.store.CounterDataStore;
import org.apache.sirona.store.DataStoreFactory;
import org.apache.sirona.store.GaugeDataStore;
import org.apache.sirona.store.GaugeValuesRequest;

import java.util.Collection;
import java.util.Map;

public class DefaultRepository implements Repository {
    private final CounterDataStore counterDataStore;
    private final CommonGaugeDataStore gaugeDataStore;
    private final DefaultGaugeManager gaugeManager;

    public DefaultRepository() {
        CounterDataStore counter = null;
        try {
            counter = Configuration.findOrCreateInstance(CounterDataStore.class);
        } catch (final MonitoringException e) {
            // no-op
        }
        if (counter == null) {
            try {
                counter = Configuration.findOrCreateInstance(CollectorCounterStore.class);
            } catch (final MonitoringException e) {
                // no-op
            }
        }

        CommonGaugeDataStore gauge = null;
        try {
            gauge = Configuration.findOrCreateInstance(GaugeDataStore.class);
        } catch (final MonitoringException e) {
            // no-op
        }
        if (gauge == null) {
            try {
                gauge = Configuration.findOrCreateInstance(CollectorGaugeDataStore.class);
            } catch (final MonitoringException e) {
                // no-op
            }
        }

        if (counter == null) {
            counter = Configuration.findOrCreateInstance(DataStoreFactory.class).getCounterDataStore();
            if (CollectorCounterStore.class.isInstance(counter)) {
                Configuration.setSingletonInstance(CollectorCounterStore.class, counter);
            } else {
                Configuration.setSingletonInstance(CounterDataStore.class, counter);
            }
        }

        if (gauge == null) {
            gauge = Configuration.findOrCreateInstance(DataStoreFactory.class).getGaugeDataStore();
            if (CollectorGaugeDataStore.class.isInstance(gauge)) {
                Configuration.setSingletonInstance(CollectorGaugeDataStore.class, gauge);
            } else {
                Configuration.setSingletonInstance(GaugeDataStore.class, gauge);
            }
        }

        this.counterDataStore = counter;
        this.gaugeDataStore = gauge;

        if (GaugeDataStore.class.isInstance(this.gaugeDataStore)) {
            this.gaugeManager = new DefaultGaugeManager(GaugeDataStore.class.cast(this.gaugeDataStore));
        } else {
            this.gaugeManager = null;
        }
    }

    @Configuration.Destroying
    public void stopGaugeTimers() {
        if (gaugeManager != null) {
            gaugeManager.stop();
        }
    }

    @Override
    public Counter getCounter(final Counter.Key key) {
        return counterDataStore.getOrCreateCounter(key);
    }

    @Override
    public Collection<Counter> counters() {
        return counterDataStore.getCounters();
    }

    @Override
    public void clear() {
        counterDataStore.clearCounters();
    }

    @Override
    public StopWatch start(final Counter monitor) {
        return new CounterStopWatch(monitor);
    }

    @Override
    public Map<Long, Double> getGaugeValues(final long start, final long end, final Role role) {
        return gaugeDataStore.getGaugeValues(new GaugeValuesRequest(start, end, role));
    }

    @Override
    public Collection<Role> gauges() {
        return gaugeDataStore.gauges();
    }

    @Override
    public Role findGaugeRole(final String name) {
        return gaugeDataStore.findGaugeRole(name);
    }

    @Override
    public void addGauge(final Gauge gauge) {
        if (gaugeManager != null) {
            gaugeManager.addGauge(gauge);
        }
    }

    @Override
    public void stopGauge(final Gauge role) {
        if (gaugeManager != null) {
            gaugeManager.stopGauge(role);
        }
    }
}
