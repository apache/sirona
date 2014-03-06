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
import org.apache.sirona.util.SerializeUtils;
import org.apache.sirona.util.UnsafeUtils;

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

/**
 * Very simple in memory storage for Path tracking feature
 * <b>MUST NOT be used in production</b>
 */
public class InMemoryPathTrackingDataStore
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
                entriesList = new ArrayList<Pointer>();
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
    public Collection<String> retrieveTrackingIds( Date startTime, Date endTime )
    {
        List<String> trackingIds = new ArrayList<String>();
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
                trackingIds.add( first.getTrackingId() );
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

        public int getSize()
        {
            return size;
        }

        public long getOffheapPointer()
        {
            return offheapPointer;
        }
    }

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
        for ( Map.Entry<String, List<Pointer>> entry : pathTrackingEntries.entrySet() )
        {
            // clear entries to not wait gc
            for ( Pointer pointer : entry.getValue() )
            {
                UnsafeUtils.getUnsafe().freeMemory( pointer.offheapPointer );
            }
        }
        pathTrackingEntries = new ConcurrentHashMap<String, List<Pointer>>( 50 );
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

    /**
     * direct access to datas not a copy
     *
     * @return
     */
    protected Map<String, List<Pointer>> getPointers()
    {
        return this.pathTrackingEntries;
    }


}
