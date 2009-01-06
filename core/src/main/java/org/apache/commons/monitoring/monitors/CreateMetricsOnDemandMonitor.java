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

package org.apache.commons.monitoring.monitors;

import org.apache.commons.monitoring.Counter;
import org.apache.commons.monitoring.Gauge;
import org.apache.commons.monitoring.Role;

/**
 * implementation of the <code>Monitor</code> interface that creates Metrics on
 * demand. The application can request for Counters/Gauges without having to
 * handle instantiation of monitors with all required Metrics pre-registered.
 *
 * @author <a href="mailto:nicolas@apache.org">Nicolas De Loof</a>
 */
public abstract class CreateMetricsOnDemandMonitor
    extends AbstractMonitor
{

    public CreateMetricsOnDemandMonitor( Key key )
    {
        super( key );
    }

    public Counter getCounter( Role role )
    {
        Counter counter = (Counter) getMetric( role );
        if ( counter != null )
        {
            return counter;
        }
        counter = newCounterInstance( role );
        Counter previous = (Counter) register( counter );
        return previous != null ? previous : counter;
    }

    public Gauge getGauge( Role role )
    {
        Gauge gauge = (Gauge) getMetric( role );
        if ( gauge != null )
        {
            return gauge;
        }
        gauge = newGaugeInstance( role );
        Gauge previous = (Gauge) register( gauge );
        return previous != null ? previous : gauge;
    }

    /**
     * Create a new Counter instance
     * <p>
     * As the monitor can be used by multiple threads, this method MAY be
     * executed to create more than one instance for the same role, so DON'T
     * assume unicity here.
     */
    protected abstract Counter newCounterInstance( Role role );

    /**
     * Create a new Gauge instance
     * <p>
     * As the monitor can be used by multiple threads, this method MAY be
     * executed to create more than one instance for the same role, so DON'T
     * assume unicity here.
     */
    protected abstract Gauge newGaugeInstance( Role role );
}
