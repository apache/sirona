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

/**
 * Thread-safe implementation of <code>Counter</code>, based on
 * synchronized methods.
 *
 * @author <a href="mailto:nicolas@apache.org">Nicolas De Loof</a>
 */
public class ThreadSafeCounter
    extends AbstractStatValue
    implements Counter
{
    private long value;

    private long sum;

    private long sumOfSquares;

    /**
     * {@inheritDoc}
     */
    public long get()
    {
        return value;
    }

    public synchronized void reset()
    {
        sum = 0;
        sumOfSquares = 0;
        value = 0;
    }

    /**
     * {@inheritDoc}
     */
    public void set( long l )
    {
        synchronized ( this )
        {
            value = l;
            computeStats( l );
        }
        notifyValueChanged( l );
    }

    public void add( long delta )
    {
        synchronized ( this )
        {
            value += delta;
            computeStats( delta );
        }
        notifyValueChanged( delta );
    }

    @Override
    protected void computeStats( long l )
    {
        sum += l;
        sumOfSquares += l * l;
        super.computeStats( l );
    }

    @Override
    public double getMean()
    {
        return ( (double) sum ) / getHits();
    }

    @Override
    protected long getSquares()
    {
        return sumOfSquares;
    }

    @Override
    public long getSum()
    {
        // TODO Auto-generated method stub
        return 0;
    }

}
