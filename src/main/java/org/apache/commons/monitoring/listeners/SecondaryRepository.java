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

import org.apache.commons.monitoring.Monitor;
import org.apache.commons.monitoring.Repository;
import org.apache.commons.monitoring.StopWatch;
import org.apache.commons.monitoring.impl.repositories.AbstractRepository;

/**
 * A repository implementation that registers as a <tt>Listener</tt> to an
 * <tt>Observable</tt> repository and maintains a set of Monitors in sync with
 * the observed repository.
 * <p>
 * As a <tt>Detachable</tt> implementation, the SecondaryRepository can be
 * detached from the observed repository and used to build a report for the
 * observed period.
 *
 * @author <a href="mailto:nicolas@apache.org">Nicolas De Loof</a>
 */
public class SecondaryRepository
    extends AbstractRepository
    implements Repository.Listener, Detachable
{
    private Repository.Observable repository;

    private boolean detached;

    public SecondaryRepository( Repository.Observable repository )
    {
        super();
        this.repository = repository;
        this.detached = false;
        for ( Monitor monitor : repository.getMonitors() )
        {
            if ( monitor instanceof Monitor.Observable )
            {
                register( new SecondaryMonitor( (Monitor.Observable) monitor ) );
            }
        }
        repository.addListener( this );
    }

    public void detach()
    {
        detached = true;
        repository.removeListener( this );
        for ( Monitor monitor : getMonitors() )
        {
            ( (Detachable) monitor ).detach();
        }
    }

    /**
     * @see org.apache.commons.monitoring.Repository.Listener#newMonitorInstance(org.apache.commons.monitoring.Monitor)
     */
    public void newMonitorInstance( Monitor monitor )
    {
        if ( !detached && monitor instanceof Monitor.Observable )
        {
            register( new SecondaryMonitor( (Monitor.Observable) monitor ) );
        }
    }

    /**
     * {@inheritDoc}
     * @see org.apache.commons.monitoring.Repository#start(org.apache.commons.monitoring.Monitor)
     */
    public StopWatch start( Monitor monitor )
    {
        throw new UnsupportedOperationException( "Not available on a secondary repository" );
    }
}
