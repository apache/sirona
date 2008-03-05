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

import org.apache.commons.monitoring.Monitor;
import org.apache.commons.monitoring.Monitor.Key;

/**
 * Abstract implementation of a Repository that creates and register new Monitor instances
 * as the monitored application request them.
 *
 * @author <a href="mailto:nicolas@apache.org">Nicolas De Loof</a>
 */
public abstract class CreateMonitorsOnDemandRepository
    extends ObservableRepository
{

    /**
     * Retrieve a monitor an creates / register a new instance if required
     * <p>
     * {@inheritDoc}
     * @see org.apache.commons.monitoring.impl.repositories.AbstractRepository#getMonitor(org.apache.commons.monitoring.Monitor.Key)
     */
    @Override
    protected Monitor getMonitor( Key key )
    {
        Monitor monitor = super.getMonitor( key );
        if ( monitor == null )
        {
            monitor = newMonitorInstance( key );
            Monitor previous = register( monitor );
            if ( previous != null )
            {
                monitor = previous;
            }
        }
        return monitor;
    }

    protected abstract Monitor newMonitorInstance( Key key );

}
