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
package org.apache.sirona.store.gauge;

import org.apache.sirona.Role;
import org.apache.sirona.SironaException;
import org.apache.sirona.configuration.Configuration;
import org.apache.sirona.store.memory.gauge.InMemoryGaugeDataStore;

import java.lang.reflect.Constructor;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class DelegatedCollectorGaugeDataStore implements CollectorGaugeDataStore {
    private final ConcurrentMap<String, GaugeDataStore> dataStores = new ConcurrentHashMap<String, GaugeDataStore>();

    private final Class<? extends GaugeDataStore> delegateClass;

    public DelegatedCollectorGaugeDataStore() {
        try {
            delegateClass = Class.class.cast(
                DelegatedCollectorGaugeDataStore.class.getClassLoader().loadClass( // use this classloader and not TCCL to avoid issues
                    Configuration.getProperty(Configuration.CONFIG_PROPERTY_PREFIX + "collector.gauge.store-class", InMemoryGaugeDataStore.class.getName())));
        } catch (final ClassNotFoundException e) {
            throw new SironaException(e.getMessage(),e);
        }
    }

    protected GaugeDataStore newStore(final String marker) {
        try {
            try {
                final Constructor<? extends GaugeDataStore> cons = delegateClass.getConstructor(String.class);
                return cons.newInstance(marker);
            } catch (final Exception e) {
                // no-op: use default constructor
            }
            return delegateClass.newInstance();
        } catch (final Exception e) {
            throw new SironaException(e);
        }
    }

    @Override
    public SortedMap<Long, Double> getGaugeValues(final GaugeValuesRequest gaugeValuesRequest, final String marker) {
        final GaugeDataStore gaugeDataStore = dataStores.get(marker);
        if (gaugeDataStore == null) {
            return new TreeMap<Long, Double>();
        }
        return gaugeDataStore.getGaugeValues(gaugeValuesRequest);
    }

    @Override
    public void createOrNoopGauge(final Role role, final String marker) {
        GaugeDataStore gaugeDataStore = dataStores.get(marker);
        if (gaugeDataStore == null) {
            gaugeDataStore = newStore(marker);
            final GaugeDataStore existing = dataStores.putIfAbsent(marker, gaugeDataStore);
            if (existing != null) {
                gaugeDataStore = existing;
            }
        }
        gaugeDataStore.createOrNoopGauge(role);
    }

    @Override
    public void addToGauge(final Role role, final long time, final double value, final String marker) {
        createOrNoopGauge(role, marker); // this implementation doesn't mandates createOrNoopGauge call
        dataStores.get(marker).addToGauge(role, time, value);
    }

    @Override
    public Collection<String> markers() {
        return dataStores.keySet();
    }

    @Override // TODO: see if using a period to aggregate data wouldn't make more sense
    public SortedMap<Long, Double> getGaugeValues(final GaugeValuesRequest gaugeValuesRequest) {
        final SortedMap<Long, Double> values = new TreeMap<Long, Double>();

        for (final Map.Entry<String, GaugeDataStore> marker : dataStores.entrySet()) {
            final Map<Long, Double> gaugeValues = marker.getValue().getGaugeValues(gaugeValuesRequest);
            for (final Map.Entry<Long, Double> entry : gaugeValues.entrySet()) {
                final Long key = entry.getKey();
                final Double value = values.get(key);
                final Double thisValue = entry.getValue();
                if (value == null) {
                    values.put(key, thisValue);
                } else {
                    values.put(key, value + thisValue);
                }
            }
        }

        return values;
    }

    @Override
    public Collection<Role> gauges() {
        final Set<Role> roles = new HashSet<Role>();
        for (final GaugeDataStore store : dataStores.values()) {
            roles.addAll(store.gauges());
        }
        return roles;
    }

    @Override
    public Role findGaugeRole(final String name) {
        for (final GaugeDataStore store : dataStores.values()) {
            final Role role = store.findGaugeRole(name);
            if (role != null) {
                return role;
            }
        }
        return null;
    }

    @Override
    public void gaugeStopped(final Role gauge) {
        for (final GaugeDataStore store : dataStores.values()) {
            store.gaugeStopped(gauge);
        }
    }

    public void reset() {
        dataStores.clear();
    }
}
