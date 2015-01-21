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

import org.apache.sirona.configuration.ioc.IoCs;
import org.apache.sirona.pathtracking.PathCallInformation;
import org.apache.sirona.pathtracking.PathTrackingEntry;

import java.util.Collection;
import java.util.Date;

/**
 *
 */
public class DelegatedCollectorPathTrackingDataStore
    implements CollectorPathTrackingDataStore
{
    private final PathTrackingDataStore delegatedPathTrackingDataStore;


    public DelegatedCollectorPathTrackingDataStore()
    {
        this.delegatedPathTrackingDataStore = IoCs.findOrCreateInstance( PathTrackingDataStore.class );
    }


    @Override
    public void store( PathTrackingEntry pathTrackingEntry )
    {
        this.delegatedPathTrackingDataStore.store( pathTrackingEntry );
    }

    @Override
    public void store( Collection<PathTrackingEntry> pathTrackingEntries )
    {
        this.delegatedPathTrackingDataStore.store( pathTrackingEntries );
    }

    @Override
    public void clearEntries()
    {
        this.delegatedPathTrackingDataStore.clearEntries();
    }

    @Override
    public Collection<PathTrackingEntry> retrieve( String trackingId )
    {
        return this.delegatedPathTrackingDataStore.retrieve( trackingId );
    }

    @Override
    public Collection<PathCallInformation> retrieveTrackingIds( Date startTime, Date endTime )
    {
        return this.delegatedPathTrackingDataStore.retrieveTrackingIds( startTime, endTime );
    }

    @Override
    public Collection<PathTrackingEntry> retrieve( String trackingId, int number )
    {
        return this.delegatedPathTrackingDataStore.retrieve( trackingId, number );
    }

    @Override
    public Collection<PathTrackingEntry> retrieve( String trackingId, String start, String end )
    {
        return this.delegatedPathTrackingDataStore.retrieve( trackingId, start, end );
    }
}
