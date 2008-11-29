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

package org.apache.commons.monitoring.stopwatches;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;

import org.apache.commons.monitoring.Monitor;
import org.apache.commons.monitoring.Unit;

/**
 * Extend DefaultStopWatch to add support for CPU time estimate based on ThreadMXBean
 *
 * @author <a href="mailto:nicolas@apache.org">Nicolas De Loof</a>
 */
public class CpuTimeStopWatch
    extends DefaultStopWatch
{
    private long cpuStartedAt;

    /**
     * Constructor.
     * <p>
     * The monitor can be set to null to use the StopWatch without the monitoring infrastructure.
     *
     * @param monitor the monitor associated with the process to be monitored
     */
    public CpuTimeStopWatch( Monitor monitor )
    {
        super( monitor );
    }

    @Override
    protected void doStart( Monitor monitor )
    {
        super.doStart( monitor );
        ThreadMXBean mx = ManagementFactory.getThreadMXBean();
        if ( mx.isCurrentThreadCpuTimeSupported() )
        {
            cpuStartedAt = mx.getCurrentThreadCpuTime();
        }
    }

    @Override
    protected void doStop()
    {
        super.doStop();
        ThreadMXBean mx = ManagementFactory.getThreadMXBean();
        if ( mx.isCurrentThreadCpuTimeSupported() )
        {
            long cpu = mx.getCurrentThreadCpuTime() - cpuStartedAt;
            monitor.getCounter( Monitor.CPU ).add( cpu, Unit.NANOS );
        }
    }
}
