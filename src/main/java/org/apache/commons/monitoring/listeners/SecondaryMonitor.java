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

package org.apache.commons.monitoring.listeners;

import org.apache.commons.monitoring.Composite;
import org.apache.commons.monitoring.Metric;
import org.apache.commons.monitoring.Monitor;
import org.apache.commons.monitoring.impl.monitors.AbstractMonitor;

/**
 * A Monitor implementation that maintains a set of secondary Metrics in sync
 * with the primary monitor. Register itself as a monitor listener to get notified
 * on new Metrics and automatically create the required secondary.
 * <p>
 * When detached, deregister itself as Monitor.Listener and detaches all secondary
 * from the primary Metrics.
 *
 * @author <a href="mailto:nicolas@apache.org">Nicolas De Loof</a>
 */
public class SecondaryMonitor
    extends AbstractMonitor
    implements Monitor.Listener, Detachable
{
    /** The primary monitor */
    private Monitor.Observable monitor;

    private boolean detached;

    private long attachedAt;

    private long detachedAt;

    public SecondaryMonitor( Monitor.Observable monitor )
    {
        super( monitor.getKey() );
        this.monitor = monitor;
        this.attachedAt = System.currentTimeMillis();
        this.detached = false;
        for ( Metric metric : monitor.getMetrics() )
        {
            onMetricRegistered( metric );
        }
        monitor.addListener( this );
    }

    @SuppressWarnings("unchecked")
    public void detach()
    {
        this.detached = true;
        for ( Metric metric : monitor.getMetrics() )
        {
            if ( metric instanceof Composite )
            {
                ( (Composite<Metric>) metric ).removeSecondary( getMetric( metric.getRole() ) );
            }
        }
        this.detachedAt = System.currentTimeMillis();
    }

    @SuppressWarnings("unchecked")
    public void onMetricRegistered( Metric metric )
    {
        if ( !detached && metric instanceof Composite )
        {
            register( ( (Composite<Metric>) metric ).createSecondary() );
        }
    }

    public boolean isDetached()
    {
        return detached;
    }

    public long getAttachedAt()
    {
        return attachedAt;
    }

    public long getDetachedAt()
    {
        return detachedAt;
    }

}
