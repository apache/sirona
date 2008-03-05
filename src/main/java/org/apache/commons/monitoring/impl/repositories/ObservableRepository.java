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

package org.apache.commons.monitoring.impl.repositories;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.commons.monitoring.Monitor;
import org.apache.commons.monitoring.Repository;

/**
 * Implement observale pattern on the repository
 *
 * @author <a href="mailto:nicolas@apache.org">Nicolas De Loof</a>
 */
public abstract class ObservableRepository
    extends AbstractRepository
    implements Repository.Observable
{

    protected List<Listener> listeners = new CopyOnWriteArrayList<Listener>();

    public void addListener( Listener listener )
    {
        listeners.add( listener );
    }

    public void removeListener( Listener listener )
    {
        listeners.remove( listener );
    }

    /**
     * {@inheritDoc}
     *
     * @see org.apache.commons.monitoring.impl.repositories.AbstractRepository#register(org.apache.commons.monitoring.Monitor)
     */
    @Override
    protected Monitor register( Monitor monitor )
    {
        Monitor previous = super.register( monitor );
        if ( previous != null )
        {
            return previous;
        }
        for ( Listener listener : listeners )
        {
            listener.newMonitorInstance( monitor );
        }
        return null;
    }

}
