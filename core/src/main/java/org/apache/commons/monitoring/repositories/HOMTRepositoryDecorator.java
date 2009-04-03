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
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.commons.monitoring.Monitor;
import org.apache.commons.monitoring.Repository;
import org.apache.commons.monitoring.StopWatch;
import org.apache.commons.monitoring.stopwatches.HistoryOfMyThread;


/**
 * @author <a href="mailto:nicolas@apache.org">Nicolas De Loof</a>
 *
 */
public class HOMTRepositoryDecorator
    extends RepositoryDecorator
    implements Repository, HistoryOfMyThread.Listener
{
    private ThreadLocal<HistoryOfMyThread> history = new ThreadLocal<HistoryOfMyThread>();

    private Collection<HistoryOfMyThread.Listener> listeners = new CopyOnWriteArrayList<HistoryOfMyThread.Listener>();

    public HOMTRepositoryDecorator()
    {
        super();
        // Act myself as a listener to force cleanup of the ThreadLocal
        addListener( this );
    }

    public void addListener( HistoryOfMyThread.Listener listener )
    {
        listeners.add( listener );
    }

    public void removeListener( HistoryOfMyThread.Listener listener )
    {
        listeners.remove( listener );
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.apache.commons.monitoring.repositories.RepositoryDecorator#start(org.apache.commons.monitoring.Monitor)
     */
    @Override
    public StopWatch start( Monitor monitor )
    {
        StopWatch stopWatch = super.start( monitor );
        HistoryOfMyThread myThread = getThreadHistory();
        return myThread.add( stopWatch );
    }

    public HistoryOfMyThread getThreadHistory()
    {
        HistoryOfMyThread myThread = history.get();
        if ( myThread == null )
        {
            myThread = new HistoryOfMyThread( listeners );
            history.set( myThread );
        }
        return myThread;
    }

    public void onHistoryEnd( HistoryOfMyThread myThread, long elapsedTime )
    {
        history.remove();
    }
}
