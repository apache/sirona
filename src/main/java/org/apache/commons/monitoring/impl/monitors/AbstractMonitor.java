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

package org.apache.commons.monitoring.impl.monitors;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.commons.monitoring.Counter;
import org.apache.commons.monitoring.Gauge;
import org.apache.commons.monitoring.Monitor;
import org.apache.commons.monitoring.Role;
import org.apache.commons.monitoring.StatValue;

/**
 * Abstract {@link Monitor} implementation with implementation for base methods
 *
 * @author <a href="mailto:nicolas@apache.org">Nicolas De Loof</a>
 */
public abstract class AbstractMonitor implements Monitor
{

    @SuppressWarnings("unchecked")
    private final ConcurrentMap<Role, StatValue> values;
    private final Key key;

    @SuppressWarnings("unchecked")
    public AbstractMonitor( Key key )
    {
        super();
        this.key = key;
        this.values = new ConcurrentHashMap<Role, StatValue>();
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
    public final StatValue getValue( String role )
    {
        return values.get( role );
    }

    @SuppressWarnings("unchecked")
    public final <T extends StatValue> T getValue( Role<T> role )
    {
        return (T) values.get( role );
    }

    @SuppressWarnings("unchecked")
    public final Collection<Role> getRoles()
    {
        return Collections.unmodifiableCollection( values.keySet() );
    }

    public final Collection<StatValue> getValues()
    {
        return Collections.unmodifiableCollection( values.values() );
    }

    /**
     * Register a new StatValue in the monitor
     *
     * @param value StatValue instance to get registered
     * @return a previously registered StatValue if existed, or <code>null</code>
     * if value has been successfully registered
     */
    @SuppressWarnings("unchecked")
    protected <T extends StatValue> T register( T value )
    {
        value.setMonitor( this );
        return (T) values.putIfAbsent( value.getRole(), value );
    }

    /**
     * {@inheritDoc}
     */
    public void reset()
    {
        for ( StatValue value : values.values() )
        {
            value.reset();
        }
    }

    public Counter getCounter( Role<Counter> role )
    {
        return getValue( role );
    }

    @SuppressWarnings("unchecked")
    public Counter getCounter( String role )
    {
        return getCounter( (Role<Counter>) Role.getRole( role ) );
    }

    public Gauge getGauge( Role<Gauge> role )
    {
        return getValue( role );
    }

    @SuppressWarnings("unchecked")
    public Gauge getGauge( String role )
    {
        return getGauge( (Role<Gauge>) Role.getRole( role ) );
    }

}