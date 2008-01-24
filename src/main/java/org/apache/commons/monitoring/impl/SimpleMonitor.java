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

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.commons.monitoring.Counter;
import org.apache.commons.monitoring.Gauge;
import org.apache.commons.monitoring.Monitor;
import org.apache.commons.monitoring.StatValue;

/**
 * Simple implementation of the {@link Monitor} interface.
 *
 * @author <a href="mailto:nicolas@apache.org">Nicolas De Loof</a>
 */
public class SimpleMonitor
    implements Monitor
{
    private final ConcurrentMap<String, StatValue> values;

    private final Key key;

    public SimpleMonitor( String name, String category, String subsystem )
    {
        this( new Key( name, category, subsystem ) );
    }

    public SimpleMonitor( Key key )
    {
        super();
        this.key = key;
        this.values = new ConcurrentHashMap<String, StatValue>();
    }

    public Counter getCounter( String role )
    {
        return (Counter) getValue( role );
    }

    public Gauge getGauge( String role )
    {
        return (Gauge) getValue( role );
    }

    public Key getKey()
    {
        return key;
    }

    public StatValue getValue( String role )
    {
        return values.get( role );
    }

    public boolean setValue( StatValue value, String role )
    {
        return values.putIfAbsent( role, value ) != null;
    }

}
