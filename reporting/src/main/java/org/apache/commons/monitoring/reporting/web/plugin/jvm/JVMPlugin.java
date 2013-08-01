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

import org.apache.commons.monitoring.configuration.Configuration;
import org.apache.commons.monitoring.counter.Counter;
import org.apache.commons.monitoring.counter.Role;
import org.apache.commons.monitoring.counter.Unit;
import org.apache.commons.monitoring.reporting.web.handler.Handler;
import org.apache.commons.monitoring.reporting.web.plugin.Plugin;
import org.apache.commons.monitoring.repositories.Repository;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Timer;
import java.util.TimerTask;

public class JVMPlugin implements Plugin {
    public static final Counter.Key CPU_KEY = new Counter.Key(new Role("cpu", Unit.Time.MILLISECOND), "CPU");
    public static final Counter.Key MEMORY_KEY = new Counter.Key(new Role("memory", Unit.Time.MILLISECOND), "Memory");

    private static final OperatingSystemMXBean SYSTEM_MX_BEAN = ManagementFactory.getOperatingSystemMXBean();
    private static final MemoryMXBean MEMORY_MX_BEAN = ManagementFactory.getMemoryMXBean();

    private final Timer timer;

    // TODO: limit size + make it accessible to the jvm handler
    private final Collection<Double> cpuHistory = new ArrayList<Double>();
    private final Collection<Long> memHistory = new ArrayList<Long>();

    public JVMPlugin() {
        // TODO: save values in a sized list (= historical values)
        final Counter cpu = Repository.INSTANCE.getCounter(CPU_KEY);
        final Counter memory = Repository.INSTANCE.getCounter(MEMORY_KEY);

        timer = new Timer("monitoring-jvm", true);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                final double systemLoadAverage = SYSTEM_MX_BEAN.getSystemLoadAverage();
                cpu.add(systemLoadAverage);
                cpuHistory.add(systemLoadAverage);

                final long used = MEMORY_MX_BEAN.getHeapMemoryUsage().getUsed();
                memory.add(used);
                memHistory.add(used);
            }
        }, 0L, Long.parseLong(Configuration.getProperty(Configuration.COMMONS_MONITORING_PREFIX + "jvm.rate", "60000")));
    }

    @Configuration.Destroying
    public void stop() {
        timer.cancel();
    }

    @Override
    public String name() {
        return "JVM";
    }

    @Override
    public Class<? extends Handler> handler() {
        return JVMHandler.class;
    }

    @Override
    public String mapping() {
        return "jvm";
    }
}
