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

package org.apache.commons.monitoring.stopwatches;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.monitoring.StopWatch;

/**
 * @author <a href="mailto:nicolas@apache.org">Nicolas De Loof</a>
 */
public class HistoryOfMyThread
{
    private List<StopWatch> history = new LinkedList<StopWatch>();

    private Collection<HistoryOfMyThread.Listener> listeners;

    /**
     * @param listeners
     */
    public HistoryOfMyThread( Collection<Listener> listeners )
    {
        super();
        this.listeners = listeners;
    }

    public StopWatch add( StopWatch stopWatch )
    {
        if ( history.size() == 0 )
        {
            stopWatch = new StopWatchDecorator( stopWatch )
            {
                public StopWatch stop()
                {
                    return stop( false );
                }

                public StopWatch stop( boolean canceled )
                {
                    super.stop( canceled );
                    if ( !canceled )
                    {
                        historyEnd( super.getElapsedTime() );
                    }
                    return getDecorated();
                }

            };
        }
        history.add( stopWatch );
        return stopWatch;
    }

    private void historyEnd( long elapsedTime )
    {
        for ( Listener listener : listeners )
        {
            listener.onHistoryEnd( this, elapsedTime );
        }
    }

    public List<StopWatch> history()
    {
        return history;
    }

    public interface Listener
    {
        void onHistoryEnd( HistoryOfMyThread history, long elapsedTime );
    }
}
