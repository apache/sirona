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
package org.apache.sirona.gauges;

import org.apache.sirona.Role;
import org.apache.sirona.store.gauge.GaugeDataStore;
import org.apache.sirona.store.gauge.GaugeValuesRequest;

import java.util.Collection;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

public class GaugeDataStoreAdapter implements GaugeDataStore, GaugeAware {
    protected final Map<Role, Gauge> gauges = new ConcurrentHashMap<Role, Gauge>();

    @Override
    public SortedMap<Long, Double> getGaugeValues(final GaugeValuesRequest gaugeValuesRequest) {
        return new TreeMap<Long, Double>();
    }

    @Override
    public void createOrNoopGauge(final Role role) {
        // no-op
    }

    @Override
    public void addToGauge(final Role role, final long time, final double value) {
        // no-op
    }

    @Override
    public Collection<Role> gauges() {
        return gauges.keySet();
    }

    @Override
    public Role findGaugeRole(final String name) {
        return null;
    }

    @Override
    public void gaugeStopped(final Role gauge) {
        gauges.remove(gauge);
    }

    @Override
    public void addGauge(final Gauge gauge) {
        gauges.put(gauge.role(), gauge);
    }

    public Collection<Gauge> getGauges() {
        return gauges.values();
    }
}
