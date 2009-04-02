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

package org.apache.commons.monitoring.repositories;

import java.util.Collection;
import java.util.Set;

import org.apache.commons.monitoring.Monitor;
import org.apache.commons.monitoring.Repository;
import org.apache.commons.monitoring.StopWatch;
import org.apache.commons.monitoring.Monitor.Key;

/**
 * @author ndeloof
 *
 */
public abstract class RepositoryDecorator
    implements Repository
{
    private Repository decorated;

    public Repository decorate( Repository decorated )
    {
        this.decorated = decorated;
        return this;
    }

    public void clear()
    {
        decorated.clear();
    }

    public Set<String> getCategories()
    {
        return decorated.getCategories();
    }

    public Monitor getMonitor( Key key )
    {
        return decorated.getMonitor( key );
    }

    public Monitor getMonitor( String name, String category, String subsystem )
    {
        return decorated.getMonitor( name, category, subsystem );
    }

    public Monitor getMonitor( String name, String category )
    {
        return decorated.getMonitor( name, category );
    }

    public Monitor getMonitor( String name )
    {
        return decorated.getMonitor( name );
    }

    public Collection<Monitor> getMonitors()
    {
        return decorated.getMonitors();
    }

    public Collection<Monitor> getMonitorsFromCategory( String category )
    {
        return decorated.getMonitorsFromCategory( category );
    }

    public Collection<Monitor> getMonitorsFromSubSystem( String subsystem )
    {
        return decorated.getMonitorsFromSubSystem( subsystem );
    }

    public Set<String> getSubSystems()
    {
        return decorated.getSubSystems();
    }

    public void reset()
    {
        decorated.reset();
    }

    public StopWatch start( Monitor monitor )
    {
        return decorated.start( monitor );
    }
}
