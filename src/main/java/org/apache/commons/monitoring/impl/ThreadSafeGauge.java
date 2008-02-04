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

import org.apache.commons.monitoring.Gauge;

/**
 * Thread-safe implementation of <code>Gauge</code>, based on
 * synchronized methods.
 * <p>
 * Maintains a sum of (value * time) on each gauge increment/decrement operation to
 * compute the mean value.
 *
 * @author <a href="mailto:nicolas@apache.org">Nicolas De Loof</a>
 */
public class ThreadSafeGauge
    extends AbstractStatValue
    implements Gauge
{
    private long value;

    private long sum;

    private long sumOfSquares;

    private long lastUse;

    // Use a double so that unset can be detected as "Not a Number"
    private double firstUse = Double.NaN;

    public ThreadSafeGauge( String role )
    {
        super( role );
    }

    public synchronized void reset()
    {
        // Don't reset value !
        sum = 0;
        sumOfSquares = 0;
        lastUse = 0;
        firstUse = Double.NaN;
    }

    public void increment()
    {
        long l = threadSafeIncrement();
        fireValueChanged( l );
    }

    protected synchronized long threadSafeIncrement()
    {
        long l;
        computeSums();
        l = ++value;
        computeStats( l );
        return l;
    }

    public void add( long delta )
    {
        long l = trheadSageAdd( delta );
        fireValueChanged( l );
    }

    protected synchronized long trheadSageAdd( long delta )
    {
        long l;
        computeSums();
        value += delta;
        l = value;
        computeStats( value );
        return l;
    }

    public void decrement()
    {
        long l = threadSafeDecrement();
        fireValueChanged( l );
    }

    protected synchronized long threadSafeDecrement()
    {
        long l;
        computeSums();
        l = --value;
        computeStats( l );
        return l;
    }

    protected void computeSums()
    {
        long now = nanotime();
        if ( Double.isNaN( firstUse ) )
        {
            firstUse = now;
        }
        else
        {
            long delta = now - lastUse;
            long s = value * delta;
            sum += s;
            sumOfSquares += s * s;
        }
        lastUse = now;
    }

    protected long nanotime()
    {
        return System.nanoTime();
    }

    @Override
    public synchronized double getMean()
    {
        return ( (double) sum ) / ( nanotime() - firstUse );
    }

    @Override
    protected long getSquares()
    {
        return sumOfSquares;
    }

    @Override
    public long getSum()
    {
        return sum;
    }

    public long get()
    {
        return value;
    }

    public void set( long l )
    {
        threadSafeSet( l );
        fireValueChanged( l );
    }

    protected synchronized void threadSafeSet( long l )
    {
        computeSums();
        value = l;
        computeStats( value );
    }

}
