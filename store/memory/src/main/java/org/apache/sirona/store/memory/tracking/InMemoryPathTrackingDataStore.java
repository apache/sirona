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
package org.apache.sirona.store.memory.tracking;

import org.apache.sirona.pathtracking.PathCallInformation;
import org.apache.sirona.pathtracking.PathTrackingEntry;
import org.apache.sirona.pathtracking.PathTrackingEntryComparator;
import org.apache.sirona.store.tracking.AbstractPathTrackingDataStore;
import org.apache.sirona.store.tracking.CollectorPathTrackingDataStore;
import org.apache.sirona.store.tracking.PathTrackingDataStore;
import org.apache.sirona.util.SerializeUtils;
import org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Very simple in memory storage for Path tracking feature
 * <b>MUST NOT be used in production</b>
 */
public class InMemoryPathTrackingDataStore
    extends AbstractPathTrackingDataStore
    implements PathTrackingDataStore, CollectorPathTrackingDataStore
{


    /**
     * store path track tracking entries list per path tracking id
     * the value is the memory address
     */
    private ConcurrentMap<String, List<Pointer>> pathTrackingEntries =
        new ConcurrentHashMap<String, List<Pointer>>( 50 );

    @Override
    public void store( PathTrackingEntry pathTrackingEntry )
    {
        store( Collections.singletonList( pathTrackingEntry ) );
    }

    @Override
    public void store( Collection<PathTrackingEntry> pathTrackingEntries )
    {
        if ( pathTrackingEntries == null )
        {
            return;
        }

        // possible different trackingId so get that
        Map<String, Set<PathTrackingEntry>> entries = new HashMap<String, Set<PathTrackingEntry>>();

        for ( PathTrackingEntry pathTrackingEntry : pathTrackingEntries )
        {
            Set<PathTrackingEntry> entriesList = entries.get( pathTrackingEntry.getTrackingId() );
            if ( entriesList == null )
            {
                entriesList = new HashSet<PathTrackingEntry>();
            }
            entriesList.add( pathTrackingEntry );
            entries.put( pathTrackingEntry.getTrackingId(), entriesList );
        }

        for ( Map.Entry<String, Set<PathTrackingEntry>> entry : entries.entrySet() )
        {
            List<Pointer> entriesList = this.pathTrackingEntries.get( entry.getKey() );

            if ( entriesList == null )
            {
                entriesList = new CopyOnWriteArrayList<Pointer>();
            }
            entriesList.addAll( serialize( entry.getValue() ) );
            this.pathTrackingEntries.put( entry.getKey(), entriesList );
        }

    }

    @Override
    public Collection<PathTrackingEntry> retrieve( String trackingId )
    {
        List<Pointer> buffers = this.pathTrackingEntries.get( trackingId );

        return deserialize( buffers );
    }

    @Override
    public Collection<PathCallInformation> retrieveTrackingIds( Date startTime, Date endTime )
    {
        Set<PathCallInformation> trackingIds = new TreeSet<PathCallInformation>( PathCallInformation.COMPARATOR );
        for ( List<Pointer> buffers : this.pathTrackingEntries.values() )
        {
            if ( pathTrackingEntries.isEmpty() )
            {
                continue;
            }

            PathTrackingEntry first =
                SerializeUtils.deserialize( readBytes( buffers.iterator().next() ), PathTrackingEntry.class );

            if ( first.getStartTime() / 1000000 > startTime.getTime() //
                && first.getStartTime() / 1000000 < endTime.getTime() )
            {
                trackingIds.add(
                    new PathCallInformation( first.getTrackingId(), new Date( startTime.getTime() / 1000000 ) ) );
            }
        }
        return trackingIds;
    }

    private Collection<PathTrackingEntry> deserialize( List<Pointer> buffers )
    {
        List<PathTrackingEntry> entries = new ArrayList<PathTrackingEntry>( buffers.size() );

        for ( Pointer pointer : buffers )
        {
            byte[] bytes = readBytes( pointer );

            PathTrackingEntry entry = SerializeUtils.deserialize( bytes, PathTrackingEntry.class );
            if ( entry != null )
            {
                entries.add( entry );
            }
        }

        return entries;
    }

    @IgnoreJRERequirement
    public byte[] readBytes( Pointer pointer )
    {
        byte[] bytes = new byte[pointer.size];
        int length = pointer.size;
        long offset = pointer.offheapPointer;
        for ( int pos = 0; pos < length; pos++ )
        {
            bytes[pos] = UnsafeUtils.getUnsafe().getByte( pos + offset );
        }
        return bytes;
    }

    public static class Pointer
    {
        int size;

        long offheapPointer;

        boolean free;

        public int getSize()
        {
            return size;
        }

        public long getOffheapPointer()
        {
            return offheapPointer;
        }

        @IgnoreJRERequirement
        public void freeMemory()
        {
            if ( !free )
            {
                UnsafeUtils.getUnsafe().freeMemory( offheapPointer );
                free = true;
            }
        }

        public boolean isFree()
        {
            return free;
        }
    }

    @IgnoreJRERequirement
    private List<Pointer> serialize( Collection<PathTrackingEntry> entries )
    {
        List<Pointer> buffers = new ArrayList<Pointer>( entries.size() );

        for ( PathTrackingEntry entry : entries )
        {
            byte[] bytes = SerializeUtils.serialize( entry );
            if ( bytes != null )
            {
                long offheapPointer = UnsafeUtils.getUnsafe().allocateMemory( bytes.length );
                Pointer pointer = new Pointer();
                pointer.offheapPointer = offheapPointer;
                pointer.size = bytes.length;
                for ( int i = 0, size = bytes.length; i < size; i++ )
                {
                    UnsafeUtils.getUnsafe().putByte( offheapPointer + i, bytes[i] );
                }
                buffers.add( pointer );

            }
        }

        return buffers;
    }

    @Override
    public void clearEntries()
    {
        List<String> entriesToRemove = new ArrayList<String>();
        for ( Map.Entry<String, List<Pointer>> entry : pathTrackingEntries.entrySet() )
        {
            boolean allFree = true;
            // clear entries to not wait gc
            for ( Pointer pointer : entry.getValue() )
            {
                if ( !pointer.isFree() )
                {
                    allFree = false;
                }
            }
            if ( allFree )
            {
                entriesToRemove.add( entry.getKey() );
            }
        }

        for ( String key : entriesToRemove )
        {
            pathTrackingEntries.remove( key );
        }

    }

    protected Map<String, Set<PathTrackingEntry>> getPathTrackingEntries()
    {

        Map<String, Set<PathTrackingEntry>> entries =
            new HashMap<String, Set<PathTrackingEntry>>( this.pathTrackingEntries.size() );

        for ( Map.Entry<String, List<Pointer>> entry : this.pathTrackingEntries.entrySet() )
        {
            Set<PathTrackingEntry> pathTrackingEntries =
                new TreeSet<PathTrackingEntry>( PathTrackingEntryComparator.INSTANCE );
            pathTrackingEntries.addAll( deserialize( entry.getValue() ) );
            entries.put( entry.getKey(), pathTrackingEntries );
        }

        return entries;
    }

    protected Map<String, List<Pointer>> getPointers()
    {
        return this.pathTrackingEntries;
    }


}
