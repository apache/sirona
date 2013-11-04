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
package org.apache.sirona.reporting.web.graph;

import org.apache.sirona.Role;
import org.apache.sirona.configuration.Configuration;
import org.apache.sirona.store.CollectorGaugeDataStore;
import org.apache.sirona.reporting.web.plugin.json.Jsons;
import org.apache.sirona.repositories.Repository;
import org.apache.sirona.store.GaugeValuesRequest;
import org.apache.sirona.util.Environment;

import java.awt.Color;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

public class Line {
    public static final String DEFAULT_COLOR = "#317eac";

    private static final Random RANDOM = new Random(System.currentTimeMillis());
    private static final int MAX_COLOR = 256;

    // default 5 colors, will grow automatically if more is needed. We need to keep same color between 2 refreshing
    private static final Collection<String> COLORS = new CopyOnWriteArrayList<String>(new String[] {
        generateColor(), generateColor(), generateColor(), generateColor(), generateColor()
    });

    public static String toJson(final String label, final String color, final Map<Long, Double> data) {
        return "{\"label\":\"" + label + "\",\"color\":\"" + color + "\",\"data\": " + Jsons.toJson(data) + "}";
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
            return "[" + Line.toJson(label, Line.DEFAULT_COLOR, Repository.INSTANCE.getGaugeValues(start, end, role)) + "]";
        }

        Configuration.findOrCreateInstance(Repository.class); // ensure CollectorGaugeDataStore exists

        final CollectorGaugeDataStore gaugeStore = Configuration.findOrCreateInstance(CollectorGaugeDataStore.class);
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

            builder.append(
                toJson(
                    label + " (" + marker + ")",
                    color,
                    gaugeStore.getGaugeValues(new GaugeValuesRequest(start, end, role), marker)
                )
            );
            if (markers.hasNext()) {
                builder.append(",");
            }
        }
        return builder.append("]").toString();
    }

    private Line() {
        // no-op
    }
}
