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
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.commons.monitoring.Monitor;
import org.apache.commons.monitoring.Repository;
import org.apache.commons.monitoring.Monitor.Key;

/**
 * Abstract implementation of {@link Repository} with support for base methods
 *
 * @author <a href="mailto:nicolas@apache.org">Nicolas De Loof</a>
 */
public abstract class AbstractRepository
    implements Repository
{
    private final ConcurrentMap<Monitor.Key, Monitor> monitors;

    public AbstractRepository()
    {
        super();
        this.monitors = new ConcurrentHashMap<Monitor.Key, Monitor>( 50 );
    }

    public Monitor getMonitor( String name )
    {
        return getMonitor( name, null, null );
    }

    public Monitor getMonitor( String name, String category )
    {
        return getMonitor( name, category, null );
    }

    public Monitor getMonitor( String name, String category, String subsystem )
    {
        return getMonitor( new Monitor.Key( name, category, subsystem ) );
    }

    protected Monitor getMonitor( Key key )
    {
        return monitors.get( key );
    }

    /**
     * Register a new monitor in the repository
     *
     * @param monitor Monitor instance to get registered
     * @return a previously registered monitor if existed, or <code>null</code>
     * if monitor has been successfully registered
     */
    protected Monitor register( Monitor monitor )
    {
        return monitors.putIfAbsent( monitor.getKey(), monitor );
    }

    public Set<String> getCategories()
    {
        Set<String> categories = new HashSet<String>();
        for ( Key key : monitors.keySet() )
        {
            categories.add( key.getCategory() );
        }
        return categories;
    }

    public Set<String> getSubSystems()
    {
        Set<String> subsystems = new HashSet<String>();
        for ( Key key : monitors.keySet() )
        {
            subsystems.add( key.getSubsystem() );
        }
        return subsystems;
    }

    public Collection<Monitor> getMonitors()
    {
        return Collections.unmodifiableCollection( monitors.values() );
    }

    public Collection<Monitor> getMonitorsFromCategory( String category )
    {
        Collection<Monitor> filtered = new LinkedList<Monitor>();
        for ( Monitor monitor : monitors.values() )
        {
            if ( category.equals( monitor.getKey().getCategory() ) )
            {
                filtered.add( monitor );
            }
        }
        return filtered;
    }

    public Collection<Monitor> getMonitorsFromSubSystem( String subsystem )
    {
        Collection<Monitor> filtered = new LinkedList<Monitor>();
        for ( Monitor monitor : monitors.values() )
        {
            if ( subsystem.equals( monitor.getKey().getSubsystem() ) )
            {
                filtered.add( monitor );
            }
        }
        return filtered;
    }

    public void reset()
    {
        for ( Monitor monitor : monitors.values() )
        {
            monitor.reset();
        }
    }

}