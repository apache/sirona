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
import org.apache.commons.monitoring.counters.Counter;
import org.apache.commons.monitoring.counters.MetricData;
import org.apache.commons.monitoring.gauges.Gauge;
import org.apache.commons.monitoring.repositories.Repository;
import org.apache.commons.monitoring.store.BatchCounterDataStore;
import org.apache.commons.monitoring.store.GaugeValuesRequest;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class CubeDataStore extends BatchCounterDataStore {
    private static final String JSON_BASE = "{" +
        "\"type\": \"%s\"," +
        "\"time\": \"%s\"," +
        "\"data\": %s" +
        "}";

    private static final String GAUGE_TYPE = "gauge";
    private static final String COUNTER_TYPE = "counter";
    private static final String JS_ISO_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";
    private static final String UTC = "UTC";

    private final Cube cube = Configuration.findOrCreateInstance(CubeBuilder.class).build();

    private final BlockingQueue<DateFormat> isoDateFormatters;

    public CubeDataStore() {
        final int maxConcurrency = 2 * Runtime.getRuntime().availableProcessors();
        isoDateFormatters = new ArrayBlockingQueue<DateFormat>(maxConcurrency);
        for (int i = 0; i < maxConcurrency; i++) {
            isoDateFormatters.add(newIsoDateFormatter());
        }
    }

    @Override
    protected synchronized void pushCountersByBatch(final Repository instance) {
        final long ts = System.currentTimeMillis();
        final StringBuilder events = new StringBuilder();
        for (final Counter counter : instance) {
            final MapBuilder data = new MapBuilder()
                .add("name", counter.getKey().getName())
                .add("role", counter.getKey().getRole().getName());

            for (final MetricData metric : MetricData.values()) {
                final double value = metric.value(counter);
                if (!Double.isNaN(value) && !Double.isInfinite(value)) {
                    data.add(metric.name(), value);
                }
            }

            buildEvent(events, COUNTER_TYPE, ts, data.map()).append(',');
        }
        if (events.length() > 0) {
            events.setLength(events.length() - 1);
            cube.post(finalPayload(events));
        }
    }

    @Override
    public void addToGauge(final Gauge gauge, final long time, final double value) {
        final Role role = gauge.role();

        cube.post(finalPayload(
            buildEvent(new StringBuilder(), GAUGE_TYPE, time,
                new MapBuilder()
                    .add("value", value)
                    .add("role", role.getName())
                    .add("unit", role.getUnit().getName())
                    .map())));
    }

    @Override
    public Map<Long, Double> getGaugeValues(final GaugeValuesRequest gaugeValuesRequest) {
        return Collections.emptyMap(); // TODO: maybe query cube?
    }

    private static String finalPayload(final StringBuilder events) {
        return '[' + events.toString() + ']';
    }

    private StringBuilder buildEvent(final StringBuilder builder, final String type, final long time, final Map<String, Object> data) {
        return builder.append(String.format(JSON_BASE, type, isoDate(time), buildData(data)));
    }

    private String isoDate(final long time) {
        final Date date = new Date(time);

        DateFormat formatter = null;
        try {
            formatter = isoDateFormatters.take();
            return formatter.format(date);
        } catch (final InterruptedException e) {
            return newIsoDateFormatter().format(date);
        } finally {
            if (formatter != null) {
                isoDateFormatters.add(formatter);
            }
        }
    }

    private static String buildData(final Map<String, Object> data) {
        final StringBuilder builder = new StringBuilder().append("{");
        for (final Map.Entry<String, Object> entry : data.entrySet()) {
            builder.append('\"').append(entry.getKey()).append('\"').append(':');

            final Object value = entry.getValue();
            if (String.class.isInstance(value)) {
                builder.append('\"').append(value).append('\"');
            } else {
                builder.append(value);
            }

            builder.append(',');
        }
        builder.setLength(builder.length() - 1);
        return builder.append("}").toString();
    }

    private static DateFormat newIsoDateFormatter() {
        final SimpleDateFormat simpleDateFormat = new SimpleDateFormat(JS_ISO_FORMAT, Locale.ENGLISH);
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone(UTC));
        return simpleDateFormat;
    }

    private static class MapBuilder {
        private final Map<String, Object> map = new HashMap<String, Object>();

        public MapBuilder add(final String key, final Object value) {
            map.put(key, value);
            return this;
        }

        public Map<String, Object> map() {
            return map;
        }
    }
}
