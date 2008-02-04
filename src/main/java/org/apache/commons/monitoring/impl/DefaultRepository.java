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

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.commons.monitoring.Monitor;

public class DefaultRepository extends AbstractRepository
{

    private List<Listener> listeners = new CopyOnWriteArrayList<Listener>();

    public DefaultRepository()
    {
        super();
    }

    public void addListener( Listener listener )
    {
        listeners.add( listener );
    }

    public void removeListener( Listener listener )
    {
        listeners.remove( listener );
    }

    public Monitor getMonitor( Monitor.Key key )
    {
        Monitor monitor = super.getMonitor( key );
        if ( monitor == null )
        {
            monitor = newMonitorInstance( key );
            Monitor previous = register( monitor );
            if ( previous != null )
            {
                return  previous;
            }
            for ( Listener listener : listeners )
            {
                listener.newMonitorInstance( monitor );
            }
        }
        return monitor;
    }

    protected Monitor newMonitorInstance( Monitor.Key key )
    {
        return new CompositeValuesMonitor( key );
    }
}
