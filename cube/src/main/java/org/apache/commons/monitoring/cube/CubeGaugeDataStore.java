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
package org.apache.commons.monitoring.cube;

import org.apache.commons.monitoring.Role;
import org.apache.commons.monitoring.configuration.Configuration;
import org.apache.commons.monitoring.gauges.Gauge;
import org.apache.commons.monitoring.store.GaugeDataStore;
import org.apache.commons.monitoring.store.GaugeValuesRequest;

import java.util.Collections;
import java.util.Map;

public class CubeGaugeDataStore implements GaugeDataStore {
    private static final String GAUGE_TYPE = "gauge";

    private final Cube cube = Configuration.findOrCreateInstance(CubeBuilder.class).build();

    @Override
    public Map<Long, Double> getGaugeValues(GaugeValuesRequest gaugeValuesRequest) {
        return Collections.emptyMap();
    }

    @Override
    public void createOrNoopGauge(Role role) {
        // no-op
    }

    @Override
    public void addToGauge(final Gauge gauge, final long time, final double value) {
        final Role role = gauge.role();

        cube.post(
                cube.buildEvent(new StringBuilder(), GAUGE_TYPE, time,
                        new MapBuilder()
                                .add("value", value)
                                .add("role", role.getName())
                                .add("unit", role.getUnit().getName())
                                .map()));
    }
}
