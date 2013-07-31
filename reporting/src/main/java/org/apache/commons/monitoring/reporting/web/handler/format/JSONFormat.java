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

package org.apache.commons.monitoring.reporting.web.handler.format;

import org.apache.commons.monitoring.counter.Counter;
import org.apache.commons.monitoring.monitors.Monitor;
import org.apache.commons.monitoring.repositories.Repository;

import java.io.PrintWriter;
import java.util.Iterator;
import java.util.Map;

public class JSONFormat implements Format {
    private static final MetricData[] METRIC_DATA = MetricData.values();

    @Override
    public void render(final PrintWriter writer, final Map<String, ?> params) {
        writer.write("{\"monitors\":[");
        final Iterator<Monitor> monitors = Repository.INSTANCE.getMonitors().iterator();
        while (monitors.hasNext()) {
            final Monitor monitor = monitors.next();
            writer.write("{\"name\":\"" + monitor.getKey().getName() + "\",");
            writer.write("\"category\":\"" + monitor.getKey().getCategory() + "\",");
            writer.write("\"counters\":[");
            final Iterator<Counter> counters = monitor.getCounters().iterator();
            while (counters.hasNext()) {
                final Counter counter = counters.next();
                writer.write("{\"role\":\"" + counter.getRole().getName() + "\",");
                writer.write("\"unit\":\"" + counter.getRole().getUnit().getName() + "\",");
                for (int i = 0; i < METRIC_DATA.length; i++) {
                    writer.write("\"" + METRIC_DATA[i].name() + "\":\"" + METRIC_DATA[i].value(counter) + "\"");
                    if (i < METRIC_DATA.length - 1) {
                        writer.write(",");
                    }
                }
                writer.write("}");
                if (counters.hasNext()) {
                    writer.write(",");
                }
            }
            writer.write("]}");
            if (monitors.hasNext()) {
                writer.write(",");
            }
        }
        writer.write("]}");
    }

    @Override
    public String type() {
        return "application/json";
    }
}