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

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.commons.monitoring.Counter;
import org.apache.commons.monitoring.Gauge;
import org.apache.commons.monitoring.Monitor;
import org.apache.commons.monitoring.StatValue;

/**
 * Abstract {@link Monitor} implementation with implementation for base methods
 *
 * @author <a href="mailto:nicolas@apache.org">Nicolas De Loof</a>
 */
public abstract class AbstractMonitor implements Monitor
{

    private final ConcurrentMap<String, StatValue> values;
    private final Key key;

    public AbstractMonitor( Key key )
    {
        super();
        this.key = key;
        this.values = new ConcurrentHashMap<String, StatValue>();
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

    public final Collection<String> getRoles()
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

    public Counter getCounter( String role )
    {
        return (Counter) getValue( role );
    }

    public Gauge getGauge( String role )
    {
        return (Gauge) getValue( role );
    }

}