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

import org.apache.sirona.gauges.jvm.ActiveThreadGauge;
import org.apache.sirona.gauges.jvm.CPUGauge;
import org.apache.sirona.gauges.jvm.UsedMemoryGauge;
import org.apache.sirona.gauges.jvm.UsedNonHeapMemoryGauge;
import org.apache.sirona.reporting.web.plugin.api.Regex;
import org.apache.sirona.reporting.web.plugin.api.Template;
import org.apache.sirona.util.Environment;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.util.HashMap;
import java.util.Map;

import static org.apache.sirona.reporting.web.plugin.api.graph.Graphs.generateReport;

public class JVMEndpoints {
    @Regex
    public Template home() {
        final OperatingSystemMXBean os = ManagementFactory.getOperatingSystemMXBean();
        final MemoryMXBean memory = ManagementFactory.getMemoryMXBean();

        final Map<String,Object> params = new HashMap<String, Object>();
        if (!Environment.isCollector()) {
            params.put("architecture", os.getArch());
            params.put("name", os.getName());
            params.put("version", os.getVersion());
            params.put("numberProcessor", os.getAvailableProcessors());
            params.put("maxMemory", memory.getHeapMemoryUsage().getMax());
            params.put( "initMemory", memory.getHeapMemoryUsage().getInit() );
            params.put("maxNonHeapMemory", memory.getNonHeapMemoryUsage().getMax());
            params.put("initNonHeapMemory", memory.getNonHeapMemoryUsage().getInit());
        }
        return new Template("jvm/jvm.vm", params);
    }

    @Regex("/cpu/([0-9]*)/([0-9]*)")
    public String cpu(final long start, final long end) {
        return generateReport("CPU Usage", CPUGauge.CPU, start, end);
    }

    @Regex("/memory/([0-9]*)/([0-9]*)")
    public String memory(final long start, final long end) {
        return generateReport("Used Memory", UsedMemoryGauge.USED_MEMORY, start, end);
    }

    @Regex("/nonheapmemory/([0-9]*)/([0-9]*)")
    public String nonHeapmemory(final long start, final long end) {
        return generateReport("Used Non Heap Memory", UsedNonHeapMemoryGauge.USED_NONHEAPMEMORY, start, end);
    }

    @Regex("/activethreads/([0-9]*)/([0-9]*)")
    public String activeThreads(final long start, final long end) {
        return generateReport("Active Thread Count", ActiveThreadGauge.ACTIVE_THREAD, start, end);
    }
}
