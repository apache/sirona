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

import org.apache.commons.monitoring.StatValue;

/**
 * A simple implementation of {@link StatValue}
 *
 * @author <a href="mailto:nicolas@apache.org">Nicolas De Loof</a>
 */
public abstract class SimpleValue
       implements StatValue
{
    private long value;

    private long sum;

    private int hits;

    private long max;

    private long min;

    private long sumOfSquares;

    private String unit;

    /**
     * {@inheritDoc}
     */
    public double getMean()
    {
        if ( hits == 0 )
        {
            return Double.NaN;
        }
        return ( (double) sum ) / hits;
    }

    /**
     * {@inheritDoc}
     */
    public long get()
    {
        return value;
    }

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

    /**
     * {@inheritDoc}
     */
    public synchronized void set( long l )
    {
        value = l;
        onValueSet( l );
    }

    protected void onValueSet(long l)
    {
        if ( ( hits == 0 ) || ( l < min ) )
        {
            min = value;
        }
        if ( ( hits == 0 ) || ( value > max ) )
        {
            max = value;
        }
        hits++;
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
        double variance = ( sumOfSquares - sum * getMean() ) / ( n - 1 );
        return Math.sqrt( variance );
    }

    protected long getSquares()
    {
        return sumOfSquares;
    }

    public synchronized void increment()
    {
        onValueSet( ++value );
    }

    public synchronized void decrement()
    {
        onValueSet( --value );
    }

    public String getUnit()
    {
        return unit;
    }

    public void add( long delta )
    {
        onValueSet( value += delta );
    }

    public long getSum()
    {
        return sum;
    }

    public int getHits()
    {
        return hits;
    }
}
