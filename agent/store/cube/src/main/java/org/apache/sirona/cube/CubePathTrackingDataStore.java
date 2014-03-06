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
import org.apache.sirona.store.tracking.BatchPathTrackingDataStore;
import org.apache.sirona.store.tracking.CollectorPathTrackingDataStore;
import org.apache.sirona.tracking.PathTrackingEntry;

import java.util.Collection;
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
    private final Cube cube = IoCs.findOrCreateInstance( CubeBuilder.class ).build();


    private static final boolean useExecutors = Boolean.parseBoolean(
        Configuration.getProperty( Configuration.CONFIG_PROPERTY_PREFIX + "pathtracking.post.useexecutors", "false" ) );


    protected static ExecutorService executorService;

    static
    {

        if ( useExecutors )
        {
            int threadsNumber =
                Configuration.getInteger( Configuration.CONFIG_PROPERTY_PREFIX + "pathtracking.post.executors", 5 );
            executorService = Executors.newFixedThreadPool( threadsNumber );

        }
    }

    @Override
    protected void pushEntriesByBatch( Map<String, List<Pointer>> pathTrackingEntries )
    {
        for ( Map.Entry<String, List<Pointer>> entry : pathTrackingEntries.entrySet() )
        {
            for ( Pointer pointer : entry.getValue() )
            {
                cube.postBytes( readBytes( pointer ), PathTrackingEntry.class.getName() );
            }
        }
    }

    @Destroying
    public void destroy()
    {
        executorService.shutdownNow();
    }
}
