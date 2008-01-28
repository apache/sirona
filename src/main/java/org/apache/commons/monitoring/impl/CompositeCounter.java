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
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.commons.monitoring.Counter;

/**
 * A composite implementation of {@link Counter} that delegates to a primary
 * implementation and maintains a collection of secondary counters.
 * <p>
 * Typical use is to create monitoring graphs : On regular time intervals, a
 * new secondary counter is registered to computes stats for the current period,
 * and then removed.
 *
 * @author <a href="mailto:nicolas@apache.org">Nicolas De Loof</a>
 */
public class CompositeCounter implements Counter
{
    private Counter primary;

    private Collection<Counter> secondary;

    public CompositeCounter( Counter primary )
    {
        super();
        this.primary = primary;
        this.secondary = new CopyOnWriteArrayList<Counter>();
    }

    public void addSecondary( Counter counter )
    {
        secondary.add( counter );
    }

    public void removeSecondary( Counter counter )
    {
        secondary.remove( counter );
    }

    public void add( long delta )
    {
        primary.add( delta );
        for ( Counter counter : secondary )
        {
            counter.add( delta );
        }
    }

    public void set( long l )
    {
        primary.set( l );
        for ( Counter counter : secondary )
        {
            counter.set( l );
        }
    }

    public long get()
    {
        return primary.get();
    }

    public int getHits()
    {
        return primary.getHits();
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

    public long getSum()
    {
        return primary.getSum();
    }

    public String getUnit()
    {
        return primary.getUnit();
    }



}
