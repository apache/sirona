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
package org.apache.commons.monitoring.reporting.format;

import org.apache.commons.monitoring.configuration.Configuration;
import org.apache.commons.monitoring.monitors.Monitor;

import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class CSVFormat implements Format {
    private static final String SEPARATOR = Configuration.getProperty(Configuration.COMMONS_MONITORING_PREFIX + "csv.separator", ";");
    private static final String ATTRIBUTES_CSV = buildMetricDataHeader();

    public static final String HEADER = "Monitor" + SEPARATOR + "Category" + SEPARATOR + "Role" + SEPARATOR + ATTRIBUTES_CSV + "\n";

    private static final Collection<String> ATTRIBUTES_ORDERED_LIST = Arrays.asList(ATTRIBUTES_CSV.split(SEPARATOR));

    private static String buildMetricDataHeader() {
        final StringBuilder builder = new StringBuilder();
        for (final MetricData md : MetricData.values()) {
            builder.append(md.name()).append(SEPARATOR);
        }

        final String str = builder.toString();
        return str.substring(0, str.length() - 1);
    }

    private String monitorName;
    private StringBuilder builder = new StringBuilder();
    private final Map<String, String> attributes = new HashMap<String, String>();

    public void repositoryStart(final PrintWriter writer) {
        writer.write(HEADER);
    }

    public void repositoryEnd(final PrintWriter writer) {
        // no-op
    }

    public void monitorStart(final PrintWriter writer, final Monitor monitor) {
        monitorName = monitor.getKey().getName() + SEPARATOR + monitor.getKey().getCategory() + SEPARATOR;
    }

    public void monitorEnd(final PrintWriter writer, final String name) {
        // no-op
    }

    public void counterStart(final PrintWriter writer, final String name) {
        builder.append(monitorName).append(name).append(SEPARATOR);
        attributes.clear();
    }

    public void counterEnd(final PrintWriter writer, final String name) {
        for (final String key : ATTRIBUTES_ORDERED_LIST) {
            final String value = attributes.get(key);
            if (value != null) {
                builder.append(value);
            } else {
                builder.append("?");
            }
            builder.append(SEPARATOR);
        }

        final String s = builder.toString();
        writer.write(s.substring(0, s.length() - 1) + "\n");
        builder = new StringBuilder();
    }

    public void attribute(final PrintWriter writer, String name, final String value) {
        attributes.put(name, value);
    }

    public void separator(final PrintWriter writer) {
        // no-op
    }

    public void escape(final PrintWriter writer, final String string) {
        builder.append(string).append(SEPARATOR);
    }
}
