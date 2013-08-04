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
package org.apache.commons.monitoring.reporting.web.plugin.jvm;

import org.apache.commons.monitoring.Role;
import org.apache.commons.monitoring.reporting.web.handler.HandlerRendererAdapter;
import org.apache.commons.monitoring.reporting.web.handler.Renderer;
import org.apache.commons.monitoring.reporting.web.plugin.jvm.gauges.CPUGauge;
import org.apache.commons.monitoring.reporting.web.plugin.jvm.gauges.UsedMemoryGauge;
import org.apache.commons.monitoring.reporting.web.template.MapBuilder;
import org.apache.commons.monitoring.repositories.Repository;

import java.io.PrintWriter;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.util.Iterator;
import java.util.Map;

public class JVMHandler extends HandlerRendererAdapter {
    @Override
    protected Renderer rendererFor(final String path) {
        if (isPath("/jvm", path)) {
            return this;
        }

        final String[] parts = path.substring("/jvm/".length()).split("/");
        if (parts.length == 3) {
            if ("cpu".equals(parts[0])) {
                return new GaugeJSonRenderer("CPU Usage", CPUGauge.CPU, parts[1], parts[2]);
            }
            if ("memory".equals(parts[0])) {
                return new GaugeJSonRenderer("Used Memory", UsedMemoryGauge.USED_MEMORY, parts[1], parts[2]);
            }
        }

        return this;
    }

    @Override
    protected String getTemplate() {
        return "jvm/jvm.vm";
    }

    @Override
    protected Map<String,?> getVariables() {
        final long now = System.currentTimeMillis();
        final OperatingSystemMXBean os = ManagementFactory.getOperatingSystemMXBean();
        final MemoryMXBean memory = ManagementFactory.getMemoryMXBean();
        return new MapBuilder<String, Object>()
            .set("architecture", os.getArch())
            .set("name", os.getName())
            .set("version", os.getVersion())
            .set("numberProcessor", os.getAvailableProcessors())
            .set("maxMemory", memory.getHeapMemoryUsage().getMax())
            .set("initMemory", memory.getHeapMemoryUsage().getInit())
            .build();
    }

    private static class GaugeJSonRenderer implements Renderer {
        private final String label;
        private final Map<Long, Double> data;

        public GaugeJSonRenderer(final String label, final Role role, final String start, final String end) {
            this.label = label;
            this.data = Repository.INSTANCE.getGaugeValues(Long.parseLong(start), Long.parseLong(end), role);
        }

        @Override
        public void render(final PrintWriter writer, final Map<String, ?> params) {
            writer.write("{ \"data\": " + toJson() + ", \"label\": \"" + label + "\", \"color\": \"#317eac\" }");
        }

        private String toJson() {
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
    }
}
