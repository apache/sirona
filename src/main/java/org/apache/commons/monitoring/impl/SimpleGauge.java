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
 * Simple implementation of a Gauge. Maintains a total of (value * time) on each
 * gauge increment/decrement to compute the mean value.
 *
 * @author <a href="mailto:nicolas@apache.org">Nicolas De Loof</a>
 */
public class SimpleGauge
    extends SimpleValue
    implements Gauge
{
    private long value;

    private long sum;

    private long sumOfSquares;

    private long lastUse;

    // Use a double so that unset can be detected as "Not a Number"
    private double firstUse = Double.NaN;

    public synchronized void increment()
    {
        long now = nanoTime();
        computeSums( now );
        computeStats( ++value );
        lastUse = now;
    }

    private void computeSums( long now )
    {
        if ( Double.isNaN( firstUse ) )
        {
            firstUse = now;
        }
        else
        {
            long delta = now - lastUse;
            long s = get() * delta;
            sum += s;
            sumOfSquares += s * s;
        }
    }

    protected long nanoTime()
    {
        return System.nanoTime();
    }

    public synchronized void decrement()
    {
        long now = nanoTime();
        computeSums( now );
        computeStats( --value );
        lastUse = now;
    }

    @Override
    public synchronized double getMean()
    {
        return ( (double) sum ) / ( nanoTime() - firstUse );
    }

    @Override
    protected long getSquares()
    {
        // TODO Auto-generated method stub
        return 0;
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
        long now = nanoTime();
        computeSums( now );
        computeStats( ++value );
        value = l;
        lastUse = now;
    }


}
