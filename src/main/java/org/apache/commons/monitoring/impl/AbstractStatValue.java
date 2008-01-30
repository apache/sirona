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

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.commons.monitoring.Monitor;
import org.apache.commons.monitoring.StatValue;

/**
 * A simple implementation of {@link StatValue}. Only provide methods to compute stats from
 * sum provided by derived classes.
 *
 * @author <a href="mailto:nicolas@apache.org">Nicolas De Loof</a>
 */
public abstract class AbstractStatValue
       implements StatValue
{
    private Monitor monitor;

    private String role;

    private int hits;

    private long max;

    private long min;

    private String unit;

    private List<Listener> listeners = new CopyOnWriteArrayList<Listener>();

    public void addListener( Listener listener )
    {
        listeners.add( listener );
    }

    public void removeListener( Listener listener )
    {
        listeners.remove( listener );
    }

    /**
     * {@inheritDoc}
     */
    public abstract double getMean();

    /**
     * {@inheritDoc}
     */
    public long getMax()
    {
        return max;
    }

    /**
     * {@inheritDoc}
     */
    public long getMin()
    {
        return min;
    }

    protected void computeStats( long l )
    {
        if ( ( hits == 0 ) || ( l < min ) )
        {
            min = l;
        }
        if ( ( hits == 0 ) || ( l > max ) )
        {
            max = l;
        }
        hits++;
    }

    protected void notifyValueChanged( long l )
    {
        // Notify listeners if Threshold exceeded
        for ( Listener listener : listeners )
        {
            if ( listener.getThreshold() < l )
            {
                listener.exceeded( this, l );
            }
        }
    }

    /**
     * Computes the
     * {@link http://en.wikipedia.org/wiki/Algorithms_for_calculating_variance}
     * unbiased variance and return the standard deviation
     * <p>
     * {@inheritDoc}
     */
    public double getStandardDeviation()
    {
        long n = hits;
        if ( n <= 1 )
        {
            return Double.NaN;
        }
        double variance = ( getSquares() - getSum() * getMean() ) / ( n - 1 );
        return Math.sqrt( variance );
    }

    protected abstract long getSquares();



    public String getUnit()
    {
        return unit;
    }

    public abstract long getSum();

    public int getHits()
    {
        return hits;
    }

    public Monitor getMonitor()
    {
        return monitor;
    }

    public String getRole()
    {
        return role;
    }

    public void setMonitor( Monitor monitor )
    {
        this.monitor = monitor;
    }

    public void setRole( String role )
    {
        this.role = role;
    }
}
