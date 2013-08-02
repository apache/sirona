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
import org.apache.commons.monitoring.counters.Unit;
import org.apache.commons.monitoring.gauges.Gauge;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;

public class MemoryGauge implements Gauge {
    public static final Role MEMORY = new Role("Memory", Unit.UNARY);

    private static final MemoryMXBean MEMORY_MX_BEAN = ManagementFactory.getMemoryMXBean();

    @Override
    public Role role() {
        return MEMORY;
    }

    @Override
    public double value() {
        return MEMORY_MX_BEAN.getHeapMemoryUsage().getUsed();
    }

    @Override
    public long period() {
        return 60000;
    }
}
