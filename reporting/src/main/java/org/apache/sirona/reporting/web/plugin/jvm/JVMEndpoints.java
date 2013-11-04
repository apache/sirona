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
package org.apache.sirona.reporting.web.plugin.jvm;

import org.apache.sirona.reporting.web.handler.api.Regex;
import org.apache.sirona.reporting.web.handler.api.Template;
import org.apache.sirona.reporting.web.plugin.json.Jsons;
import org.apache.sirona.gauges.jvm.CPUGauge;
import org.apache.sirona.gauges.jvm.UsedMemoryGauge;
import org.apache.sirona.reporting.web.template.MapBuilder;
import org.apache.sirona.repositories.Repository;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.OperatingSystemMXBean;

public class JVMEndpoints {
    @Regex
    public Template home() {
        final OperatingSystemMXBean os = ManagementFactory.getOperatingSystemMXBean();
        final MemoryMXBean memory = ManagementFactory.getMemoryMXBean();

        return new Template("jvm/jvm.vm", new MapBuilder<String, Object>()
            .set("architecture", os.getArch())
            .set("name", os.getName())
            .set("version", os.getVersion())
            .set("numberProcessor", os.getAvailableProcessors())
            .set("maxMemory", memory.getHeapMemoryUsage().getMax())
            .set("initMemory", memory.getHeapMemoryUsage().getInit())
            .build());
    }

    @Regex("/cpu/([0-9]*)/([0-9]*)")
    public String cpu(final long start, final long end) {
        return "{ \"data\": " + Jsons.toJson(Repository.INSTANCE.getGaugeValues(start, end, CPUGauge.CPU)) + ", \"label\": \"CPU Usage\", \"color\": \"#317eac\" }";
    }

    @Regex("/memory/([0-9]*)/([0-9]*)")
    public String memory(final long start, final long end) {
        return "{ \"data\": " + Jsons.toJson(Repository.INSTANCE.getGaugeValues(start, end, UsedMemoryGauge.USED_MEMORY)) + ", \"label\": \"Used Memory\", \"color\": \"#317eac\" }";
    }
}
