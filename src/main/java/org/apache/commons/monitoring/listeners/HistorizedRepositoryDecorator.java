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

package org.apache.commons.monitoring.listeners;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.commons.monitoring.Monitor;
import org.apache.commons.monitoring.Monitoring;
import org.apache.commons.monitoring.Repository;
import org.apache.commons.monitoring.StopWatch;
import org.apache.commons.monitoring.Unit;
import org.apache.commons.monitoring.reporting.AbstractPeriodicLogger;

/**
 * @author <a href="mailto:nicolas@apache.org">Nicolas De Loof</a>
 */
public class HistorizedRepositoryDecorator
    extends AbstractPeriodicLogger
    implements Repository.Observable
{
    /** The decorated repository */
    private Repository.Observable repository;

    private List<SecondaryRepository> history;

    private int size;

    public HistorizedRepositoryDecorator( int period, final int size, Observable repository )
    {
        super( period, repository );
        this.repository = repository;
        this.size = size;
        this.history = new LinkedList<SecondaryRepository>();
    }

    /**
     * Store the period Repository into the history Map, using
     * System.currentTimeMillis().
     * <p>
     * {@inheritDoc}
     *
     * @see org.apache.commons.monitoring.reporting.AbstractPeriodicLogger#log(SecondaryRepository)
     */
    @Override
    protected void log( SecondaryRepository period )
        throws IOException
    {
        history.add( period );
        while ( history.size() > size )
        {
            history.remove( history.size() - 1 );
        }
    }

    public void addListener( Listener listener )
    {
        repository.addListener( listener );
    }

    public Set<String> getCategories()
    {
        return repository.getCategories();
    }

    public Monitor getMonitor( String name, String category, String subsystem )
    {
        return repository.getMonitor( name, category, subsystem );
    }

    public Monitor getMonitor( String name, String category )
    {
        return repository.getMonitor( name, category );
    }

    public Monitor getMonitor( String name )
    {
        return repository.getMonitor( name );
    }

    public Collection<Monitor> getMonitors()
    {
        return repository.getMonitors();
    }

    public Collection<Monitor> getMonitorsFromCategory( String category )
    {
        return repository.getMonitorsFromCategory( category );
    }

    public Collection<Monitor> getMonitorsFromSubSystem( String subsystem )
    {
        return repository.getMonitorsFromSubSystem( subsystem );
    }

    public Set<String> getSubSystems()
    {
        return repository.getSubSystems();
    }

    public void removeListener( Listener listener )
    {
        repository.removeListener( listener );
    }

    public void reset()
    {
        repository.reset();
    }

    public StopWatch start( Monitor monitor )
    {
        return repository.start( monitor );
    }

    public List<SecondaryRepository> getHistory()
    {
        return history;
    }
}
