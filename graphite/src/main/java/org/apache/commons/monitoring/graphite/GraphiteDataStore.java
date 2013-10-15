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
package org.apache.commons.monitoring.graphite;

import org.apache.commons.monitoring.configuration.Configuration;
import org.apache.commons.monitoring.counters.Counter;
import org.apache.commons.monitoring.counters.MetricData;
import org.apache.commons.monitoring.gauges.Gauge;
import org.apache.commons.monitoring.repositories.Repository;
import org.apache.commons.monitoring.store.BatchCounterDataStore;
import org.apache.commons.monitoring.store.GaugeValuesRequest;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GraphiteDataStore extends BatchCounterDataStore {
    private static final Logger LOGGER = Logger.getLogger(GraphiteDataStore.class.getName());

    private static final String GAUGE_PREFIX = "gauge-";
    private static final String COUNTER_PREFIX = "counter-";
    private static final char SEP = '-';
    private static final char SPACE_REPLACEMENT_CHAR = '_';
    private static final char SPACE = ' ';

    private final Graphite graphite = Configuration.findOrCreateInstance(GraphiteBuilder.class).build();

    @Override
    protected synchronized void pushCountersByBatch(final Repository instance) {
        try {
            graphite.open();

            final long ts = System.currentTimeMillis();

            for (final Counter counter : instance) {
                final Counter.Key key = counter.getKey();
                final String prefix = noSpace(COUNTER_PREFIX + key.getRole().getName() + SEP + key.getName() + SEP);

                for (final MetricData data : MetricData.values()) {
                    graphite.push(
                        prefix + data.name(),
                        data.value(counter),
                        ts);
                }
            }
        } catch (final IOException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        } finally {
            graphite.close();
        }
    }

    @Override
    public void addToGauge(final Gauge gauge, final long time, final double value) {
        try {
            graphite.simplePush(GAUGE_PREFIX + noSpace(gauge.role().getName()), value, time);
        } catch (final IOException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    private static String noSpace(final String s) {
        return s.replace(SPACE, SPACE_REPLACEMENT_CHAR);
    }

    @Override
    public Map<Long, Double> getGaugeValues(final GaugeValuesRequest gaugeValuesRequest) {
        return Collections.emptyMap(); // when using graphite we expect the user to use Graphite to render metrics
    }
}
