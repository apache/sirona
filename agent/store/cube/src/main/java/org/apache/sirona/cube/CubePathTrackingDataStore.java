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

package org.apache.sirona.cube;

import org.apache.sirona.configuration.Configuration;
import org.apache.sirona.configuration.ioc.Destroying;
import org.apache.sirona.configuration.ioc.IoCs;
import org.apache.sirona.pathtracking.PathTrackingEntry;
import org.apache.sirona.store.tracking.BatchPathTrackingDataStore;
import org.apache.sirona.store.tracking.CollectorPathTrackingDataStore;
import org.apache.sirona.util.SerializeUtils;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 *
 */
public class CubePathTrackingDataStore
    extends BatchPathTrackingDataStore
    implements CollectorPathTrackingDataStore
{
    private static final Cube CUBE = IoCs.findOrCreateInstance( CubeBuilder.class ).build();


    private static final boolean USE_EXECUTORS = Boolean.parseBoolean(
        Configuration.getProperty( Configuration.CONFIG_PROPERTY_PREFIX + "pathtracking.post.useexecutors", "false" ) );

    private static boolean USE_SINGLE_STORE = Boolean.parseBoolean(
        Configuration.getProperty( Configuration.CONFIG_PROPERTY_PREFIX + "pathtracking.singlestore", "false" ) );

    protected static ExecutorService executorService;


    static
    {

        if ( USE_EXECUTORS )
        {
            int threadsNumber =
                Configuration.getInteger( Configuration.CONFIG_PROPERTY_PREFIX + "pathtracking.post.executors", 5 );
            executorService = Executors.newFixedThreadPool( threadsNumber );

        }

    }

    @Override
    public void store( final PathTrackingEntry pathTrackingEntry )
    {
        CUBE.doPostBytes( SerializeUtils.serialize( pathTrackingEntry ), PathTrackingEntry.class.getName() );
    }

    @Override
    protected void pushEntriesByBatch( Map<String, List<Pointer>> pathTrackingEntries )
    {
        if ( !USE_SINGLE_STORE )
        {

            for ( Map.Entry<String, List<Pointer>> entry : pathTrackingEntries.entrySet() )
            {
                for ( Pointer pointer : entry.getValue() )
                {
                    if ( !pointer.isFree() )
                    {
                        CUBE.doPostBytes( readBytes( pointer ), PathTrackingEntry.class.getName() );
                        pointer.freeMemory();
                    }
                }
            }
        }
    }

    @Destroying
    public void destroy()
    {
        executorService.shutdownNow();
    }


}
