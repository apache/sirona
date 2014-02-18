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

import org.apache.sirona.tracking.PathTrackingEntry;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Very simple in memory storage for Path tracking feature
 * <b>MUST NOT be used in production</b>
 * @author Olivier Lamy
 */
public class InMemoryPathTrackingDataStore implements PathTrackingDataStore
{
    /**
     * store path track tracking entries list per path tracking id
     */
    private ConcurrentHashMap<String, List<PathTrackingEntry>> pathTrackingEntries = new ConcurrentHashMap<String, List<PathTrackingEntry>>( 50 );

    @Override
    public void store( PathTrackingEntry pathTrackingEntry )
    {
        List<PathTrackingEntry> pathTrackingEntries = this.pathTrackingEntries.get( pathTrackingEntry.getTrackingId() );

        if (pathTrackingEntries == null) {
            pathTrackingEntries = new ArrayList<PathTrackingEntry>( );
        }
        pathTrackingEntries.add( pathTrackingEntry );
        this.pathTrackingEntries.put( pathTrackingEntry.getTrackingId(), pathTrackingEntries );
    }

    @Override
    public List<PathTrackingEntry> retrieve( String trackingId )
    {
        return this.pathTrackingEntries.get( trackingId );
    }

    @Override
    public List<String> retrieveTrackingIds( Date startTime, Date endTime )
    {
        List<String> trackingIds = new ArrayList<String>(  );
        for ( List<PathTrackingEntry> pathTrackingEntries : this.pathTrackingEntries.values() )
        {
            if (pathTrackingEntries.isEmpty()) {
                continue;
            }
            if (pathTrackingEntries.get( 0 ).getStartTime() / 1000000 > startTime.getTime() //
                && pathTrackingEntries.get( 0 ).getStartTime() / 1000000 < endTime.getTime()) {
              trackingIds.add( pathTrackingEntries.get( 0 ).getTrackingId() );
            }
        }
        return trackingIds;
    }
}
