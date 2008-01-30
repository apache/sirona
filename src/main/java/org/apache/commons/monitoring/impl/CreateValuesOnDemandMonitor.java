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

package org.apache.commons.monitoring.impl;

import org.apache.commons.monitoring.Counter;
import org.apache.commons.monitoring.Gauge;

/**
 * implementation of the <code>Monitor</code> interface that creates StatValues on
 * demand. The application can request for Counters/Gauges without having to
 * handle instantiation of monitors with all required StatValues pre-registered.
 *
 * @author <a href="mailto:nicolas@apache.org">Nicolas De Loof</a>
 */
public class CreateValuesOnDemandMonitor
    extends AbstractMonitor
{

    public CreateValuesOnDemandMonitor( Key key )
    {
        super( key );
    }

    /**
     * Retrieve a Counter or create a new one for the role
     */
    public Counter getCounter( String role )
    {
        Counter counter = (Counter) getValue( role );
        if ( counter != null )
        {
            return counter;
        }
        return register( newCounterInstance(), role );
    }

    /**
     * Create a new Counter instance
     */
    protected Counter newCounterInstance()
    {
        return new ThreadSafeCounter();
    }

    /**
     * Retrieve a Gauge or create a new one for the role
     */
    public Gauge getGauge( String role )
    {
        Gauge gauge = (Gauge) getValue( role );
        if ( gauge != null )
        {
            return gauge;
        }
        return register( newGaugeInstance(), role );
    }

    /**
     * Create a new Gauge instance
     */
    protected Gauge newGaugeInstance()
    {
        return new ThreadSafeGauge();
    }

}
