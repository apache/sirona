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

package org.apache.sirona.cassandra.pathtracking;

import me.prettyprint.cassandra.serializers.LongSerializer;
import me.prettyprint.cassandra.serializers.StringSerializer;
import me.prettyprint.hector.api.Keyspace;
import me.prettyprint.hector.api.Serializer;
import me.prettyprint.hector.api.beans.ColumnSlice;
import me.prettyprint.hector.api.beans.OrderedRows;
import me.prettyprint.hector.api.beans.Row;
import me.prettyprint.hector.api.factory.HFactory;
import me.prettyprint.hector.api.query.QueryResult;
import org.apache.sirona.cassandra.DynamicDelegatedSerializer;
import org.apache.sirona.cassandra.collector.CassandraSirona;
import org.apache.sirona.configuration.ioc.IoCs;
import org.apache.sirona.store.tracking.BatchPathTrackingDataStore;
import org.apache.sirona.store.tracking.CollectorPathTrackingDataStore;
import org.apache.sirona.store.tracking.PathTrackingDataStore;
import org.apache.sirona.tracking.PathTrackingEntry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentMap;

import static org.apache.sirona.cassandra.collector.CassandraSirona.*;

/**
 *
 */
public class CassandraPathTrackingDataStore
    extends BatchPathTrackingDataStore
    implements PathTrackingDataStore, CollectorPathTrackingDataStore
{

    private final CassandraSirona cassandra;

    private final Keyspace keyspace;

    private final String family;

    private final String markerFamilly;


    public CassandraPathTrackingDataStore()
    {
        this.cassandra = IoCs.findOrCreateInstance( CassandraSirona.class );
        this.keyspace = cassandra.getKeyspace();
        this.family = cassandra.getPathTrackingColumFamily();
        this.markerFamilly = cassandra.getMarkerPathTrackingColumFamily();
    }

    @Override
    public void store( PathTrackingEntry pathTrackingEntry )
    {
        store( Collections.singletonList( pathTrackingEntry ) );
    }

    @Override
    public void store( Collection<PathTrackingEntry> pathTrackingEntries )
    {

        // FIXME find a more efficient way to store such batch of datas

        for ( PathTrackingEntry pathTrackingEntry : pathTrackingEntries )

        {
            final String id = id( pathTrackingEntry );

            HFactory.createMutator( keyspace, StringSerializer.get() )
                //  values
                .addInsertion( id, family, column( "trackingId", pathTrackingEntry.getTrackingId() ) ) //
                .addInsertion( id, family, column( "nodeId", pathTrackingEntry.getNodeId() ) ) //
                .addInsertion( id, family, column( "className", pathTrackingEntry.getClassName() ) ) //
                .addInsertion( id, family, column( "methodName", pathTrackingEntry.getMethodName() ) ) //
                .addInsertion( id, family, column( "startTime", pathTrackingEntry.getStartTime() ) ) //
                .addInsertion( id, family, column( "executionTime", pathTrackingEntry.getExecutionTime() ) ) //
                .addInsertion( id, family, column( "level", pathTrackingEntry.getLevel() ) ) //
                .addInsertion( "PATH_TRACKING", markerFamilly, emptyColumn( id ) ) //
                .execute();
        }
    }

    protected String id( PathTrackingEntry pathTrackingEntry )
    {
        return cassandra.generateKey( pathTrackingEntry.getTrackingId(), //
                                      pathTrackingEntry.getClassName(),//
                                      pathTrackingEntry.getMethodName(), //
                                      Long.toString( pathTrackingEntry.getStartTime() ),//
                                      pathTrackingEntry.getNodeId() );
    }

    @Override
    public Collection<PathTrackingEntry> retrieve( String trackingId )
    {
        return null;
    }

    @Override
    public Collection<String> retrieveTrackingIds( Date startTime, Date endTime )
    {
        return null;
    }

    /**
     * <b>use with CAUTION as can return a lot of data</b>
     * <p>This method is use for testing purpose</p>
     *
     * @return {@link java.util.List} containing all {@link PathTrackingEntry}
     */
    public Map<String, Set<PathTrackingEntry>> retrieveAll()
    {
        final QueryResult<OrderedRows<String, String, String>> cResult = //
            HFactory.createRangeSlicesQuery( keyspace, //
                                             StringSerializer.get(), //
                                             StringSerializer.get(), //
                                             StringSerializer.get() ) //
                .setColumnFamily( family ) //
                .setRange( null, null, false, Integer.MAX_VALUE ) //
                .execute();

        Map<String, Set<PathTrackingEntry>> entries = new TreeMap<String, Set<PathTrackingEntry>>();

        final DynamicDelegatedSerializer<Object> serializer = new DynamicDelegatedSerializer<Object>();

        for ( Row<String, String, String> row : cResult.get().getList() )
        {
            ColumnSlice<String, String> columnSlice = row.getColumnSlice();
            String trackingId = columnSlice.getColumnByName( "trackingId" ).getValue();
            String nodeId = columnSlice.getColumnByName( "nodeId" ).getValue();
            String className = columnSlice.getColumnByName( "className" ).getValue();
            String methodName = columnSlice.getColumnByName( "methodName" ).getValue();

            Serializer<String> stringSerializer = columnSlice.getColumnByName( "startTime" ).getValueSerializer();

            String foo = columnSlice.getColumnByName( "startTime" ).getValue();
            long startTime = getOrDefault( serializer, //
                                           columnSlice.getColumnByName( "startTime" ), //
                                           LongSerializer.get() ).longValue();

            long executionTime = getOrDefault( serializer, //
                                               columnSlice.getColumnByName( "executionTime" ), //
                                               LongSerializer.get() ).longValue();

            int level = getOrDefault( serializer, //
                                      columnSlice.getColumnByName( "level" ), //
                                      LongSerializer.get() ).intValue();

            Set<PathTrackingEntry> pathTrackingEntries = entries.get( trackingId );
            if ( pathTrackingEntries == null )
            {
                pathTrackingEntries = new TreeSet<PathTrackingEntry>();
            }
            pathTrackingEntries.add( new PathTrackingEntry( trackingId, //
                                                            nodeId, //
                                                            className, //
                                                            methodName, //
                                                            startTime, //
                                                            executionTime, //
                                                            level ) );

            entries.put( trackingId, pathTrackingEntries );

        }

        return entries;
    }

    @Override
    protected void pushEntriesByBatch( ConcurrentMap<String, Set<PathTrackingEntry>> pathTrackingEntries )
    {
        List<PathTrackingEntry> entries = new ArrayList<PathTrackingEntry>(  );

        for ( Map.Entry<String, Set<PathTrackingEntry>> entry : pathTrackingEntries.entrySet()) {
            entries.addAll( entry.getValue() );
        }


        store( entries );
    }

    protected Keyspace getKeyspace()
    {
        return keyspace;
    }

    protected String getFamily()
    {
        return family;
    }
}
