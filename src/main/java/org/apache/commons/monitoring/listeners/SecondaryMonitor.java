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

import org.apache.commons.monitoring.Composite;
import org.apache.commons.monitoring.Monitor;
import org.apache.commons.monitoring.StatValue;
import org.apache.commons.monitoring.impl.monitors.AbstractMonitor;

/**
 * A Monitor implementation that maintains a set of secondary StatValues in sync
 * with the primary monitor. Register itself as a monitor listener to get notified
 * on new StatValues and automatically create the required secondary.
 * <p>
 * When detached, deregister itself as Monitor.Listener and detaches all secondary
 * from the primary StatValues.
 *
 * @author <a href="mailto:nicolas@apache.org">Nicolas De Loof</a>
 */
public class SecondaryMonitor
    extends AbstractMonitor
    implements Monitor.Listener, Detachable
{

    /** The primary monitor */
    private Monitor.Observable monitor;

    private boolean detached;

    public SecondaryMonitor( Monitor.Observable monitor )
    {
        super( monitor.getKey() );
        this.monitor = monitor;
        this.detached = false;
        for ( StatValue value : monitor.getValues() )
        {
            onStatValueRegistered(  value );
        }
        monitor.addListener( this );
    }

    @SuppressWarnings("unchecked")
    public void detach()
    {
        this.detached = true;
        for ( StatValue value : monitor.getValues() )
        {
            if ( value instanceof Composite )
            {
                ( (Composite<StatValue>) value ).removeSecondary( getValue( value.getRole() ) );
            }
        }
    }

    @SuppressWarnings("unchecked")
    public void onStatValueRegistered( StatValue value )
    {
        if ( !detached && value instanceof Composite )
        {
            register( ( (Composite<StatValue>) value ).createSecondary() );
        }
    }

}
