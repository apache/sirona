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

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.commons.monitoring.Counter;
import org.apache.commons.monitoring.Gauge;
import org.apache.commons.monitoring.Monitor;
import org.apache.commons.monitoring.StatValue.Listener;

/**
 * A composite implementation of {@link Gauge} that delegates to a primary
 * implementation and maintains a collection of secondary Gauges.
 * <p>
 * Typical use is to create monitoring graphs : On regular time intervals, a
 * new secondary Gauge is registered to computes stats for the current period,
 * and then removed.
 *
 * @author <a href="mailto:nicolas@apache.org">Nicolas De Loof</a>
 */
public class CompositeGauge implements Gauge
{
    private Gauge primary;

    private Collection<Gauge> secondary;

    public Gauge getPrimary()
    {
        return primary;
    }

    public Collection<Gauge> getSecondary()
    {
        return Collections.unmodifiableCollection( secondary );
    }

    public CompositeGauge( Gauge primary )
    {
        super();
        this.primary = primary;
        this.secondary = new CopyOnWriteArrayList<Gauge>();
    }

    public synchronized void addSecondary( Gauge gauge )
    {
        gauge.set( primary.get() );
        secondary.add( gauge );
    }

    public void removeSecondary( Gauge gauge )
    {
        secondary.remove( gauge );
    }

    public synchronized void increment()
    {
        primary.increment();
        for ( Gauge gauge : secondary )
        {
            gauge.increment();
        }
    }

    public void add( long delta )
    {
        primary.add( delta );
        for ( Gauge gauge : secondary )
        {
            gauge.add( delta );
        }
    }

    public synchronized void decrement()
    {
        primary.decrement();
        for ( Gauge gauge : secondary )
        {
            gauge.increment();
        }
    }

    public synchronized void set( long l )
    {
        primary.set( l );
        for ( Gauge gauge : secondary )
        {
            gauge.set( l );
        }
    }

    public long get()
    {
        return primary.get();
    }

    public long getMax()
    {
        return primary.getMax();
    }

    public double getMean()
    {
        return primary.getMean();
    }

    public long getMin()
    {
        return primary.getMin();
    }

    public double getStandardDeviation()
    {
        return primary.getStandardDeviation();
    }

    public String getUnit()
    {
        return primary.getUnit();
    }

    public void reset()
    {
        primary.reset();
    }

    public Monitor getMonitor()
    {
        return primary.getMonitor();
    }

    public String getRole()
    {
        return primary.getRole();
    }

    public void setMonitor( Monitor monitor )
    {
        primary.setMonitor( monitor );
    }

    public void setRole( String role )
    {
        primary.setRole( role );
    }

    public void addListener( Listener listener )
    {
        primary.addListener( listener );
    }

    public void removeListener( Listener listener )
    {
        primary.removeListener( listener );
    }

}
