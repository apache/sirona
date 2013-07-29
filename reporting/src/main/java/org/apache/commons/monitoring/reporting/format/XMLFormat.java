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

import org.apache.commons.monitoring.counter.Counter;
import org.apache.commons.monitoring.monitors.Monitor;
import org.apache.commons.monitoring.repositories.Repository;

import java.io.PrintWriter;
import java.util.Map;

public class XMLFormat implements Format {
    @Override
    public void render(final PrintWriter writer, final Map<String, ?> params) {
        writer.write("<repository>");
        for (final Monitor monitor : Repository.INSTANCE.getMonitors()) {
            writer.write("<monitor name=\"" + monitor.getKey().getName() + "\" category=\"" + monitor.getKey().getCategory() + "\">");
            for (final Counter counter : monitor.getCounters()) {
                writer.write("<counter role=\"" + counter.getRole().getName() + "\" unit=\"" + counter.getRole().getUnit().getName() + "\"");
                for (final MetricData md : MetricData.values()) {
                    writer.write(" " + md.name() + "=\"" + md.value(counter) + "\"");
                }
                writer.write(" />");
            }
            writer.write("</monitor>");
        }
        writer.write("</repository>");
    }

    @Override
    public String type() {
        return "application/xml";
    }
}