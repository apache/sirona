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

package org.apache.sirona.store.tracking;

import org.apache.sirona.configuration.Configuration;
import org.apache.sirona.configuration.ioc.Created;
import org.apache.sirona.store.BatchFuture;
import org.apache.sirona.store.memory.tracking.InMemoryPathTrackingDataStore;
import org.apache.sirona.util.DaemonThreadFactory;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 */
public abstract class BatchPathTrackingDataStore
    extends InMemoryPathTrackingDataStore
{

    private static final Logger LOGGER = Logger.getLogger( BatchPathTrackingDataStore.class.getName() );

    protected BatchFuture scheduledTask;

    @Created // call it only when main impl not in delegated mode so use IoC lifecycle management
    public void initBatch()
    {
        final String name =
            getClass().getSimpleName().toLowerCase( Locale.ENGLISH ).replace( "pathtrackingdatastore", "" );

        final long period = getPeriod( name );

        final ScheduledExecutorService ses =
            Executors.newSingleThreadScheduledExecutor( new DaemonThreadFactory( name + "-pathtracking-schedule-" ) );

        final ScheduledFuture<?> future =
            ses.scheduleAtFixedRate( new PushPathTrackingTask(), period, period, TimeUnit.MILLISECONDS );

        scheduledTask = new BatchFuture( ses, future );
    }

    protected int getPeriod( final String name )
    {
        int period = Configuration.getInteger( Configuration.CONFIG_PROPERTY_PREFIX + name + ".pathtracking.period", //
                                         Configuration.getInteger(
                                             Configuration.CONFIG_PROPERTY_PREFIX + name + ".period", 60000 ) );

        return period;
    }


    private class PushPathTrackingTask
        implements Runnable
    {
        @Override
        public void run()
        {
            try
            {
                pushEntriesByBatch( getPointers() );
                clearEntries();
            }
            catch ( final Exception e )
            {
                LOGGER.log( Level.SEVERE, e.getMessage(), e );
            }
        }
    }

    protected abstract void pushEntriesByBatch( final Map<String, List<Pointer>> pathTrackingEntries );

}
