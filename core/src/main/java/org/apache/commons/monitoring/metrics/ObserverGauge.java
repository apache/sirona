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
import org.apache.commons.monitoring.Gauge;
import org.apache.commons.monitoring.Metric;
import org.apache.commons.monitoring.Unit;

/**
 * A Gauge that observe another Gauge and computes stats until it gets detached
 *
 * @author <a href="mailto:nicolas@apache.org">Nicolas De Loof</a>
 */
public class ObserverGauge
    extends ObserverMetric<Gauge>
    implements Gauge
{
    private Gauge.Observable observable;

    private ThreadSafeGauge delegate;

    public ObserverGauge( Gauge.Observable observable )
    {
        super( observable.getRole() );
        this.observable = observable;
        this.delegate = new RentrantLockGauge( getRole() );
        delegate.threadSafeSet( observable.getValue() );
        observable.addListener( this );
    }

    protected Gauge getDelegate()
    {
        return delegate;
    }

    @Override
    protected SummaryStatistics getSummary()
    {
        return delegate.getSummary();
    }

    @Override
    protected Metric.Observable getObservable()
    {
        return observable;
    }

    public final void onValueChanged( Metric.Observable metric, double value )
    {
        delegate.threadSafeSet( value );
    }

    public final double getValue()
    {
        return delegate.getValue();
    }

    public final void add( double delta )
    {
        throw new UnsupportedOperationException( "Observer cannot be updated directly" );
    }

    public final void increment()
    {
        throw new UnsupportedOperationException( "Observer cannot be updated directly" );
    }

    public final void increment( Unit unit )
    {
        throw new UnsupportedOperationException( "Observer cannot be updated directly" );
    }

    public final void set( double value, Unit unit )
    {
        throw new UnsupportedOperationException( "Observer cannot be updated directly" );
    }

    public final void decrement()
    {
        throw new UnsupportedOperationException( "Observer cannot be updated directly" );
    }

    public final void decrement( Unit unit )
    {
        throw new UnsupportedOperationException( "Observer cannot be updated directly" );
    }

    public Metric.Type getType()
    {
        return Metric.Type.GAUGE;
    }

    public void reset()
    {
        throw new UnsupportedOperationException( "Observer cannot be updated directly" );
    }
}
