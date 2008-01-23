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
import org.apache.commons.monitoring.StatValue;

/**
 * A simple implementation of {@link StatValue}
 * 
 * @author <a href="mailto:nicolas@apache.org">Nicolas De Loof</a>
 */
public class SimpleValue
    implements Gauge, Counter
{
    private long value;

    private long total;

    private int hits;

    private long max;

    private long min;

    /** total of squares */
    private long squares;

    private String unit;

    /**
     * {@inheritDoc}
     */
    public double average()
    {
        if ( hits == 0 )
        {
            return Double.NaN;
        }
        return ( (double) total ) / hits;
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
    public long max()
    {
        return max;
    }

    /**
     * {@inheritDoc}
     */
    public long min()
    {
        return min;
    }

    /**
     * {@inheritDoc}
     */
    public synchronized void set( long l )
    {
        value = l;
        onValueSet();
    }

    private void onValueSet()
    {
        if ( ( hits == 0 ) || ( value < min ) )
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
    public double standardDeviation()
    {
        long n = hits;
        if ( n <= 1 )
        {
            return Double.NaN;
        }
        double variance = ( squares - total * average() ) / ( n - 1 );
        return Math.sqrt( variance );
    }

    public long total()
    {
        return total;
    }

    public int hits()
    {
        return hits;
    }

    protected long getSquares()
    {
        return squares;
    }

    public synchronized void increment()
    {
        value++;
        onValueSet();
    }

    public synchronized void decrement()
    {
        value--;
        onValueSet();
    }

    public void add( long delta )
    {
        value += delta;
        onValueSet();
    }

    public String getUnit()
    {
        return unit;
    }

}
