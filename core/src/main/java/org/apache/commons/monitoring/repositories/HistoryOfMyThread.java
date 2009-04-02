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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.monitoring.StopWatch;
import org.apache.commons.monitoring.stopwatches.StopWatchDecorator;


/**
 * @author ndeloof
 *
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

    protected StopWatch add( StopWatch stopWatch )
    {
        if ( history.size() == 0 )
        {
            stopWatch = new StopWatchDecorator( stopWatch )
            {
                public StopWatch stop()
                {
                    super.stop();
                    historyEnd();
                    return getDecorated();
                }

                public StopWatch stop( boolean canceled )
                {
                    super.stop( canceled );
                    historyEnd();
                    return getDecorated();
                }

                public StopWatch cancel()
                {
                    super.cancel();
                    historyEnd();
                    return getDecorated();
                }
            };
        }
        history.add( stopWatch );
        return stopWatch;
    }

    private void historyEnd()
    {
        for ( Listener listener : listeners )
        {
            listener.onHistoryEnd( this );
        }
    }

    public Iterator<StopWatch> history()
    {
        return history.iterator();
    }

    public interface Listener
    {
        void onHistoryEnd( HistoryOfMyThread history );
    }
}
