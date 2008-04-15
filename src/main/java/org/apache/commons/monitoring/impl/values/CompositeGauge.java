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

package org.apache.commons.monitoring.impl.values;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

import org.apache.commons.monitoring.Composite;
import org.apache.commons.monitoring.Gauge;
import org.apache.commons.monitoring.Role;

/**
 * A composite implementation of {@link Gauge} that delegates to a primary
 * implementation and maintains a collection of secondary Gauges.
 * <p>
 * Typical use is to create monitoring graphs : On regular time intervals, a new
 * secondary Gauge is registered to computes stats for the current period, and
 * then removed.
 *
 * @author <a href="mailto:nicolas@apache.org">Nicolas De Loof</a>
 */
public class CompositeGauge
    extends ThreadSafeGauge
    implements Composite<Gauge>
{
    private Collection<Gauge> secondary;

    public Collection<Gauge> getSecondary()
    {
        return Collections.unmodifiableCollection( secondary );
    }

    public CompositeGauge( Role<Gauge> role )
    {
        super( role );
        this.secondary = new LinkedList<Gauge>();
    }

    public synchronized Gauge createSecondary()
    {
        // Must be synchronized to ensure the new gauge shares the primary
        // initial value
        Gauge gauge = new ThreadSafeGauge( getRole() );
        if ( getUnit() != null )
        {
            // If the primary gauge has not been used yet, unit is not set and
            // value is default
            gauge.set( get(), getUnit() );
        }
        secondary.add( gauge );
        return gauge;
    }

    public synchronized void removeSecondary( Gauge gauge )
    {
        secondary.remove( gauge );
    }

    @Override
    protected synchronized long threadSafeAdd( long delta )
    {
        for ( Gauge gauge : secondary )
        {
            gauge.add( delta, getUnit() );
        }
        return super.threadSafeAdd( delta );
    }

    @Override
    protected synchronized void threadSafeSet( long value )
    {
        for ( Gauge gauge : secondary )
        {
            gauge.set( value, getUnit() );
        }
        super.threadSafeSet( value );
    }
}
