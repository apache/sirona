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

package org.apache.commons.monitoring.metrics;

import org.apache.commons.math.stat.descriptive.rank.Max;
import org.apache.commons.math.stat.descriptive.rank.Min;
import org.apache.commons.monitoring.Gauge;
import org.apache.commons.monitoring.Role;
import org.apache.commons.monitoring.Unit;

/**
 * * Thread-safe implementation of <code>Gauge</code>, based on synchronized methods.
 * <p>
 * Maintains a sum of (value * time) on each gauge increment/decrement operation to compute the mean value.
 *
 * @author <a href="mailto:nicolas@apache.org">Nicolas De Loof</a>
 */
public abstract class ThreadSafeGauge
    extends ObservableMetric
    implements Gauge, Gauge.Observable
{
    protected double value;

    protected long lastUse;

    protected double firstUse = Double.NaN;

    protected Min min = new Min();

    protected Max max = new Max();

    protected long hits;

    public ThreadSafeGauge( Role role )
    {
        super( role );
    }

    public final Type getType()
    {
        return Type.GAUGE;
    }

    public final long getHits()
    {
        return hits;
    }

    public final double getMax()
    {
        return max.getResult();
    }

    public final double getMin()
    {
        return min.getResult();
    }

    public final double getMean()
    {
        if ( Double.isNaN( lastUse ) || Double.isNaN( firstUse ) )
        {
            return Double.NaN;
        }
        return getSummary().getMean() / ( lastUse - firstUse );
    }

    public final double getValue()
    {
        return value;
    }

    public final void increment()
    {
        add( 1 );
    }

    public final void increment( Unit unit )
    {
        add( 1, unit );
    }

    public final void decrement( Unit unit )
    {
        add( -1, unit );
    }

    public final void decrement()
    {
        add( -1 );
    }

    public final void add( double delta )
    {
        double d = threadSafeAdd( delta );
        fireValueChanged( d );
    }

    protected final double threadSafeAdd( double delta )
    {
        threadSafeSet( value + delta );
        return value;
    }

    protected long nanotime()
    {
        return System.nanoTime();
    }

    public final double get()
    {
        return value;
    }

    public final void set( double d, Unit unit )
    {
        d = normalize( d, unit );
        threadSafeSet( d );
        fireValueChanged( d );
    }

    /**
     * Set the Gauge value in a thread-safe way
     * 
     * @param d value to set
     */
    protected abstract void threadSafeSet( double d );

    protected final void doReset()
    {
        // Don't reset value !
        getSummary().clear();
        lastUse = 0;
        firstUse = Double.NaN;
    }

    protected final void doThreadSafeSet( double d )
    {
        value = d;
        long now = nanotime();
        if ( Double.isNaN( firstUse ) )
        {
            firstUse = now;
        }
        else
        {
            long delta = now - lastUse;
            double s = d * delta;
            getSummary().addValue( s );
        }
        lastUse = now;
        hits++;
        min.increment( value );
        max.increment( value );
    }

}