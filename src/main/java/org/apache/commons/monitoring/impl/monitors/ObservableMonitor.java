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

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.commons.monitoring.Metric;
import org.apache.commons.monitoring.Monitor;

/**
 * <code>Monitor</code> implementation with support for listeners
 *
 * @author <a href="mailto:nicolas@apache.org">Nicolas De Loof</a>
 */
public abstract class ObservableMonitor
    extends AbstractMonitor
    implements Monitor.Observable
{

    private List<Listener> listeners;

    /**
     * Constructor
     *
     * @param key the monitor identifier
     */
    public ObservableMonitor( Key key )
    {
        super( key );
        this.listeners = new CopyOnWriteArrayList<Listener>();
    }

    /**
     * Register the Metric for the role, if none was registered before
     * 
     * @param metric
     * @param role
     * @return the registered metric, or a previously existing one for the role
     */
    @Override
    protected <T extends Metric> T register( T metric )
    {
        T previous = (T) super.register( metric );
        if ( previous != null )
        {
            return previous;
        }
        for ( Listener listener : listeners )
        {
            listener.onMetricRegistered( metric );
        }
        return null;
    }

    public void addListener( Listener listener )
    {
        listeners.add( listener );
    }

    public void removeListener( Listener listener )
    {
        listeners.remove( listener );
    }
}