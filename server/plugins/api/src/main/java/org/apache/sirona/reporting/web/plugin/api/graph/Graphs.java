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
package org.apache.sirona.reporting.web.plugin.api.graph;

import org.apache.sirona.Role;
import org.apache.sirona.configuration.Configuration;
import org.apache.sirona.configuration.ioc.IoCs;
import org.apache.sirona.repositories.Repository;
import org.apache.sirona.store.gauge.CollectorGaugeDataStore;
import org.apache.sirona.store.gauge.GaugeValuesRequest;
import org.apache.sirona.util.Environment;

import java.awt.Color;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.CopyOnWriteArrayList;

// a helper class to generate JSon (without dependencies) from gauges to plot graphes
public class Graphs {
    public static final String DEFAULT_COLOR = "#317eac";

    private static final int MAX_POINTS = Configuration.getInteger(Configuration.CONFIG_PROPERTY_PREFIX + "reporting.graph.max-points", 200) - 1;

    private static final Random RANDOM = new Random(System.currentTimeMillis());
    private static final int MAX_COLOR = 256;

    // default 5 colors, will grow automatically if more is needed. We need to keep same color between 2 refreshing
    private static final Collection<String> COLORS = new CopyOnWriteArrayList<String>(new String[] {
        generateColor(), generateColor(), generateColor(), generateColor(), generateColor()
    });

    public static String toJson(final String label, final String color, final SortedMap<Long, Double> data) {
        return "{\"label\":\"" + label + "\",\"color\":\"" + color + "\",\"data\": " + toJson(data) + "}";
    }

    public static String toJson(final SortedMap<Long, Double> data) { // helper for gauges
        final StringBuilder builder = new StringBuilder().append("[");
        final Iterator<Map.Entry<Long,Double>> iterator = data.entrySet().iterator();
        while (iterator.hasNext()) {
            final Map.Entry<Long, Double> entry = iterator.next();
            builder.append("[").append(entry.getKey()).append(", ").append(entry.getValue()).append("]");
            if (iterator.hasNext()) {
                builder.append(", ");
            }
        }
        return builder.append("]").toString();
    }

    private static String generateColor() {
        int red, green, blue;
        synchronized (RANDOM) {
            red = RANDOM.nextInt(MAX_COLOR);
            green = RANDOM.nextInt(MAX_COLOR);
            blue = RANDOM.nextInt(MAX_COLOR);
        }

        final Color color = new Color(red, green, blue);
        final String hexString = Integer.toHexString(color.getRGB());
        return "#" + hexString.substring(2, hexString.length());
    }

    public static String generateReport(final String label, final Role role, final long start, final long end) {
        if (!Environment.isCollector()) {
            final SortedMap<Long, Double> gaugeValues = Repository.INSTANCE.getGaugeValues(start, end, role);
            return "[" + Graphs.toJson(label, Graphs.DEFAULT_COLOR, aggregate(gaugeValues)) + "]";
        }

        final CollectorGaugeDataStore gaugeStore = IoCs.findOrCreateInstance(CollectorGaugeDataStore.class);
        final Iterator<String> markers = gaugeStore.markers().iterator();
        final StringBuilder builder = new StringBuilder("[");
        final Iterator<String> colors = COLORS.iterator();
        while (markers.hasNext()) {
            final String marker = markers.next();
            final String color;
            if (colors.hasNext()) {
                color = colors.next();
            } else {
                color = generateColor();
                COLORS.add(color);
            }

            final SortedMap<Long, Double> gaugeValues = gaugeStore.getGaugeValues(new GaugeValuesRequest(start, end, role), marker);
            builder.append(
                toJson(
                    label + " (" + marker + ")",
                    color,
                    aggregate(gaugeValues)
                )
            );
            if (markers.hasNext()) {
                builder.append(",");
            }
        }
        return builder.append("]").toString();
    }

    private static SortedMap<Long, Double> aggregate(final SortedMap<Long, Double> gaugeValues) {
        if (gaugeValues.size() < MAX_POINTS || !TreeMap.class.isInstance(gaugeValues)) {
            return gaugeValues;
        }

        final long min = gaugeValues.keySet().iterator().next();
        final long max = Number.class.cast(TreeMap.class.cast(gaugeValues).lastKey()).longValue();
        final long step = (long) ((max - min) * 1. / MAX_POINTS);

        final SortedMap<Long, Double> aggregation = new TreeMap<Long, Double>();
        double currentValue = 0;
        long switchValue = min + step;
        long number = 0;
        for (final Map.Entry<Long, Double> entry : gaugeValues.entrySet()) {
            final long key = entry.getKey();
            double value = entry.getValue();
            if (value == Double.NaN) {
                value = 0;
            }

            if (key < switchValue) {
                currentValue += value;
                number++;
            } else {
                aggregation.put(switchValue, currentValue / Math.max(1, number));
                switchValue += step;
                number = 0;
                currentValue = value;
            }
        }
        aggregation.put(switchValue, currentValue / Math.max(1, number));
        return aggregation;
    }

    private Graphs() {
        // no-op
    }
}
