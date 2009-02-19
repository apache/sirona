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

package org.apache.commons.monitoring.monitors;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.commons.monitoring.Counter;
import org.apache.commons.monitoring.Gauge;
import org.apache.commons.monitoring.Metric;
import org.apache.commons.monitoring.Monitor;
import org.apache.commons.monitoring.Role;

/**
 * Abstract {@link Monitor} with implementation for base methods
 *
 * @author <a href="mailto:nicolas@apache.org">Nicolas De Loof</a>
 */
public abstract class AbstractMonitor implements Monitor
{

    private final ConcurrentMap<Role, Metric> metrics;
    private final Key key;

    public AbstractMonitor( Key key )
    {
        super();
        this.key = key;
        this.metrics = createConcurrentMap();
    }

    /**
     * User with very specific requirements or fine knowledge of Java Concurrency may override this method and use
     * another implementation of ConcurrentMap. In such case, please post feedback to apache-commons dev list !
     *
     * @return the ConcurrentMap implementation to use for storing metrics
     */
    protected ConcurrentHashMap<Role, Metric> createConcurrentMap()
    {
        return new ConcurrentHashMap<Role, Metric>();
    }

    /**
     * {@inheritDoc}
     */
    public final Key getKey()
    {
        return key;
    }

    /**
     * {@inheritDoc}
     */
    public final Metric getMetric( String role )
    {
        return metrics.get( Role.getRole( role ) );
    }

    public final Metric getMetric( Role role )
    {
        return metrics.get( role );
    }

    public final Collection<Role> getRoles()
    {
        return Collections.unmodifiableCollection( metrics.keySet() );
    }

    public Collection<Metric> getMetrics()
    {
        return Collections.unmodifiableCollection( metrics.values() );
    }

    /**
     * Register a new Metric in the monitor
     *
     * @param metric Metric instance to get registered
     * @return a previously registered Metric if existed, or <code>null</code> if the metric has been successfully
     * registered
     */
    protected Metric register( Metric metric )
    {
        metric.setMonitor( this );
        return metrics.putIfAbsent( metric.getRole(), metric );
    }

    /**
     * {@inheritDoc}
     */
    public void reset()
    {
        for ( Metric metric : metrics.values() )
        {
            metric.reset();
        }
    }

    public Counter getCounter( String role )
    {
        return getCounter( Role.getRole( role ) );
    }

    public Gauge getGauge( String role )
    {
        return getGauge( Role.getRole( role ) );
    }

    public Counter getCounter( Role role )
    {
        return (Counter) getMetric( role );
    }

    public Gauge getGauge( Role role )
    {
        return (Gauge) getMetric( role );
    }

}