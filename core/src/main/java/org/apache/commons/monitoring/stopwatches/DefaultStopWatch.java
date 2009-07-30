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

import org.apache.commons.monitoring.Monitor;
import org.apache.commons.monitoring.Role;
import org.apache.commons.monitoring.Unit;

/**
 * Implementation of StopWatch that maintains a Gauge of concurrent threads accessing the monitored resource.
 *
 * @author <a href="mailto:nicolas@apache.org">Nicolas De Loof</a>
 */
public class DefaultStopWatch extends SimpleStopWatch
{
    private Role concurrency;

    /**
     * Constructor.
     * <p>
     * The monitor can be set to null to use the StopWatch without the monitoring infrastructure.
     *
     * @param monitor the monitor associated with the process to be monitored
     */
    public DefaultStopWatch( Monitor monitor )
    {
        this( monitor, Monitor.CONCURRENCY );
    }

    public DefaultStopWatch( Monitor monitor, Role concurrency )
    {
        super( monitor );
        this.concurrency = concurrency;
        doStart();
    }

    public DefaultStopWatch( Monitor monitor, Role concurrency, Role role )
    {
        super( monitor, role );
        this.concurrency = concurrency;
        doStart();
    }

    protected void doStart()
    {
        monitor.getGauge( concurrency ).increment( Unit.UNARY );
    }

    protected void doStop()
    {
        super.doStop();
        monitor.getGauge( concurrency ).decrement( Unit.UNARY );
    }


    protected void doCancel()
    {
        monitor.getGauge( concurrency ).decrement( Unit.UNARY );
    }

}