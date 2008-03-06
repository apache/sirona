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

package org.apache.commons.monitoring.impl.monitors;

import org.apache.commons.monitoring.Counter;
import org.apache.commons.monitoring.Gauge;
import org.apache.commons.monitoring.Unit;
import org.apache.commons.monitoring.impl.values.ThreadSafeCounter;
import org.apache.commons.monitoring.impl.values.ThreadSafeGauge;

/**
 * implementation of the <code>Monitor</code> interface that creates StatValues on
 * demand. The application can request for Counters/Gauges without having to
 * handle instantiation of monitors with all required StatValues pre-registered.
 *
 * @author <a href="mailto:nicolas@apache.org">Nicolas De Loof</a>
 */
public class CreateValuesOnDemandMonitor
    extends ObservableMonitor
{

    public CreateValuesOnDemandMonitor( Key key )
    {
        super( key );
    }

    /**
     * Retrieve a Counter or create a new one for the role
     */
    @Override
    public Counter getCounter( String role )
    {
        Counter counter = (Counter) getValue( role );
        if ( counter != null )
        {
            return counter;
        }
        counter = newCounterInstance( role );
        Counter previous = register( counter );
        return previous != null ? previous : counter;
    }

    /**
     * Create a new Counter instance
     */
    protected Counter newCounterInstance( String role )
    {
        return new ThreadSafeCounter( role );
    }

    /**
     * Retrieve a Gauge or create a new one for the role
     */
    @Override
    public Gauge getGauge( String role )
    {
        Gauge gauge = (Gauge) getValue( role );
        if ( gauge != null )
        {
            return gauge;
        }
        gauge = newGaugeInstance( role );
        Gauge previous = register( gauge );
        return previous != null ? previous : gauge;
    }

    /**
     * Create a new Gauge instance
     */
    protected Gauge newGaugeInstance( String role )
    {
        return  new ThreadSafeGauge( role );
    }

}
