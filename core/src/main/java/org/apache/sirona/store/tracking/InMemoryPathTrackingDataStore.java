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
import org.apache.sirona.tracking.PathTrackingEntryComparator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Very simple in memory storage for Path tracking feature
 * <b>MUST NOT be used in production</b>
 */
public class InMemoryPathTrackingDataStore
    implements PathTrackingDataStore
{
    /**
     * store path track tracking entries list per path tracking id
     */
    private ConcurrentMap<String, Set<PathTrackingEntry>> pathTrackingEntries =
        new ConcurrentHashMap<String, Set<PathTrackingEntry>>( 50 );

    @Override
    public void store( PathTrackingEntry pathTrackingEntry )
    {
        store( Collections.singletonList( pathTrackingEntry ) );
    }

    @Override
    public void store( Collection<PathTrackingEntry> pathTrackingEntries )
    {
        // possible different trackingId so get that
        Map<String, Set<PathTrackingEntry>> entries = new HashMap<String, Set<PathTrackingEntry>>();

        for ( PathTrackingEntry pathTrackingEntry : pathTrackingEntries )
        {
            Set<PathTrackingEntry> entriesList = entries.get( pathTrackingEntry.getTrackingId() );

            if ( pathTrackingEntries == null )
            {
                pathTrackingEntries = new TreeSet<PathTrackingEntry>( PathTrackingEntryComparator.INSTANCE );
            }
            entriesList.add( pathTrackingEntry );
            entries.put( pathTrackingEntry.getTrackingId(), entriesList );
        }

        for ( Map.Entry<String, Set<PathTrackingEntry>> entry : entries.entrySet() )
        {
            Set<PathTrackingEntry> entriesList = this.pathTrackingEntries.get( entry.getKey() );
            if ( entriesList == null )
            {
                entriesList = new TreeSet<PathTrackingEntry>( PathTrackingEntryComparator.INSTANCE );
            }
            entriesList.addAll( entry.getValue() );
            entries.put( entry.getKey(), entriesList );
        }

    }

    @Override
    public Collection<PathTrackingEntry> retrieve( String trackingId )
    {
        return this.pathTrackingEntries.get( trackingId );
    }

    @Override
    public List<String> retrieveTrackingIds( Date startTime, Date endTime )
    {
        List<String> trackingIds = new ArrayList<String>();
        for ( Set<PathTrackingEntry> pathTrackingEntries : this.pathTrackingEntries.values() )
        {
            if ( pathTrackingEntries.isEmpty() )
            {
                continue;
            }

            PathTrackingEntry first = pathTrackingEntries.iterator().next();

            if ( first.getStartTime() / 1000000 > startTime.getTime() //
                && first.getStartTime() / 1000000 < endTime.getTime() )
            {
                trackingIds.add( first.getTrackingId() );
            }
        }
        return trackingIds;
    }

    protected ConcurrentMap<String, Set<PathTrackingEntry>> getPathTrackingEntries()
    {
        return pathTrackingEntries;
    }
}
