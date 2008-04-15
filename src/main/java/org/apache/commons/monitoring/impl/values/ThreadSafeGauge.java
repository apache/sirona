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

import org.apache.commons.monitoring.Gauge;
import org.apache.commons.monitoring.Role;
import org.apache.commons.monitoring.Unit;

/**
 * Thread-safe implementation of <code>Gauge</code>, based on synchronized
 * methods.
 * <p>
 * Maintains a sum of (value * time) on each gauge increment/decrement operation
 * to compute the mean value.
 *
 * @author <a href="mailto:nicolas@apache.org">Nicolas De Loof</a>
 */
public class ThreadSafeGauge
    extends AbstractStatValue<Gauge>
    implements Gauge
{
    private long value;

    private long sum;

    private long sumOfSquares;

    private long lastUse;

    // Use a double so that unset can be detected as "Not a Number"
    private double firstUse = Double.NaN;

    public ThreadSafeGauge( Role<Gauge> role )
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

    public void increment( Unit unit )
    {
        add( 1, unit );
    }

    public void decrement( Unit unit )
    {
        add( -1, unit );
    }

    public void add( long delta, Unit unit )
    {
        delta = normalize( delta, unit );
        long l = threadSafeAdd( delta );
        fireValueChanged( l );
    }

    protected synchronized long threadSafeAdd( long delta )
    {
        computeSums();
        value += delta;
        computeStats( value );
        return value;
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

    public void set( long l, Unit unit )
    {
        l = normalize( l, unit );
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
