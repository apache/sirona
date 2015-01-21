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

package org.apache.sirona.cassandra.local;

import me.prettyprint.cassandra.serializers.StringSerializer;
import me.prettyprint.hector.api.beans.ColumnSlice;
import me.prettyprint.hector.api.beans.OrderedRows;
import me.prettyprint.hector.api.beans.Row;
import me.prettyprint.hector.api.factory.HFactory;
import me.prettyprint.hector.api.query.QueryResult;
import org.apache.sirona.cassandra.DynamicDelegatedSerializer;
import org.apache.sirona.cassandra.collector.pathtracking.CassandraPathTrackingDataStore;
import org.apache.sirona.pathtracking.PathTrackingEntry;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 *
 */
public class ExtendedCassandraPathTrackingDataStore
    extends CassandraPathTrackingDataStore
{

    /**
     * <b>use with CAUTION as can return a lot of data</b>
     * <p>This method is use for testing purpose</p>
     *
     * @return {@link java.util.List} containing all {@link org.apache.sirona.pathtracking.PathTrackingEntry}
     */
    public Map<String, Set<PathTrackingEntry>> retrieveAll()
    {
        final QueryResult<OrderedRows<String, String, String>> cResult = //
            HFactory.createRangeSlicesQuery( getKeyspace(), //
                                             StringSerializer.get(), //
                                             StringSerializer.get(), //
                                             StringSerializer.get() ) //
                .setColumnFamily( getFamily() ) //
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

            long startTime = Long.parseLong( columnSlice.getColumnByName( "startTime" ).getValue() );
            //getOrDefault( serializer, //
            //              columnSlice.getColumnByName( "startTime" ), //
            //              LongSerializer.get() ).longValue();

            long executionTime = Long.parseLong( columnSlice.getColumnByName( "executionTime" ).getValue() );
            //getOrDefault( serializer, //
            //            columnSlice.getColumnByName( "executionTime" ), //
            //               LongSerializer.get() ).longValue();

            int level = Integer.parseInt( columnSlice.getColumnByName( "level" ).getValue() );
            //getOrDefault( serializer, //
            //          columnSlice.getColumnByName( "level" ), //
            //          LongSerializer.get() ).intValue();

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

}
