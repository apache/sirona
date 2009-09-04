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

import org.apache.commons.math.stat.descriptive.SummaryStatistics;
import org.apache.commons.monitoring.Counter;
import org.apache.commons.monitoring.Metric;

/**
 * A Counter that observe another Counter and computes stats until it gets detached
 *
 * @author <a href="mailto:nicolas@apache.org">Nicolas De Loof</a>
 */
public class ObserverCounter
    extends ObserverMetric<Counter>
    implements Counter
{
    private Counter.Observable observable;

    private ThreadSafeCounter delegate;

    public ObserverCounter( Counter.Observable observable )
    {
        super( observable.getRole() );
        this.observable = observable;
        this.delegate = new RentrantLockCounter( getRole() );
        observable.addListener( this );
    }

    protected Counter getDelegate()
    {
        return delegate;
    }

    @Override
    protected final SummaryStatistics getSummary()
    {
        return delegate.getSummary();
    }

    @Override
    protected final Metric.Observable getObservable()
    {
        return observable;
    }

    public final void onValueChanged( Metric.Observable metric, double value )
    {
        delegate.threadSafeAdd( value );
    }

    public final void add( double delta )
    {
        throw new UnsupportedOperationException( "Observer cannot be updated directly" );
    }

    public final Metric.Type getType()
    {
        return Metric.Type.COUNTER;
    }

    public final void reset()
    {
        throw new UnsupportedOperationException( "Observer cannot be updated directly" );
    }
}
