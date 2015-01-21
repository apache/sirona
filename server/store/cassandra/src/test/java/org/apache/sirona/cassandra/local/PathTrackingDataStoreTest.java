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

import me.prettyprint.hector.api.Cluster;
import me.prettyprint.hector.api.ddl.KeyspaceDefinition;
import org.apache.sirona.cassandra.CassandraBuilder;
import org.apache.sirona.cassandra.collector.pathtracking.CassandraPathTrackingDataStore;
import org.apache.sirona.cassandra.framework.CassandraRunner;
import org.apache.sirona.cassandra.framework.CassandraTestInject;
import org.apache.sirona.configuration.ioc.IoCs;
import org.apache.sirona.pathtracking.PathCallInformation;
import org.apache.sirona.pathtracking.PathTrackingEntry;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 *
 */
@RunWith( CassandraRunner.class )
public class PathTrackingDataStoreTest
{

    @CassandraTestInject
    private Cluster cluster;


    private final CassandraBuilder builder = IoCs.findOrCreateInstance( CassandraBuilder.class );

    CassandraPathTrackingDataStore thestore;

    protected CassandraPathTrackingDataStore getStore()
    {
        if ( thestore != null )
        {
            return thestore;
        }
        return thestore = IoCs.findOrCreateInstance(
            CassandraPathTrackingDataStore.class );// .processInstance( new CassandraPathTrackingDataStore() );
    }

    @Test
    public void test_save()
        throws Exception
    {

        try
        {

            PathTrackingEntry entry = new PathTrackingEntry( UUID.randomUUID().toString(), //
                                                             "nodeId", //
                                                             "org.au.beer.TheBest", //
                                                             "littlecreatures", //
                                                             new Date().getTime(), //
                                                             12, //
                                                             1 );

            getStore().store( entry );

            Assert.assertFalse( getStore().retrieveAll().isEmpty() );

            Assert.assertEquals( 1, getStore().retrieveAll().size() );
        }
        finally
        {
            cluster.truncate( builder.getKeyspace(), builder.getPathTrackingColumFamily() );

            Assert.assertTrue( getStore().retrieveAll().isEmpty() );

            Assert.assertEquals( 0, getStore().retrieveAll().size() );

        }

    }

    @Test
    public void test_retrieve_trackingIds()
        throws Exception
    {
        List<String> ids = new ArrayList<String>();

        try
        {
            String uuid = UUID.randomUUID().toString();
            ids.add( uuid );
            PathTrackingEntry first = new PathTrackingEntry( uuid, //
                                                             "nodeId", //
                                                             "org.au.beer.TheBest", //
                                                             "littlecreatures", //
                                                             new Date().getTime(), //
                                                             12, //
                                                             1 );

            getStore().store( first );

            Calendar twoDaysAgo = Calendar.getInstance();
            twoDaysAgo.add( Calendar.DATE, -2 );

            uuid = UUID.randomUUID().toString();
            ids.add( uuid );
            PathTrackingEntry second = new PathTrackingEntry( uuid, //
                                                              "nodeId", //
                                                              "org.au.beer.TheBest", //
                                                              "littlecreatures", //
                                                              twoDaysAgo.getTime().getTime(), //
                                                              12, //
                                                              1 );

            getStore().store( second );

            Assert.assertFalse( getStore().retrieveAll().isEmpty() );

            Assert.assertEquals( 2, getStore().retrieveAll().size() );

            Calendar yesterday = Calendar.getInstance();
            yesterday.add( Calendar.DATE, -1 );

            Collection<PathCallInformation> trackingIds = getStore().retrieveTrackingIds( yesterday.getTime(), new Date() );

            Assert.assertEquals( 1, trackingIds.size() );

            Assert.assertEquals( first.getTrackingId(), trackingIds.iterator().next().getTrackingId() );


        }
        finally
        {
            cluster.truncate( builder.getKeyspace(), builder.getPathTrackingColumFamily() );

            Assert.assertTrue( getStore().retrieveAll().isEmpty() );

            Assert.assertEquals( 0, getStore().retrieveAll().size() );

        }
    }


    @Test
    public void test_retrieve_tracking_path()
        throws Exception
    {

        try
        {
            String firstuuid = UUID.randomUUID().toString();

            PathTrackingEntry first = new PathTrackingEntry( firstuuid, //
                                                             "nodeId", //
                                                             "org.au.beer.TheBest", //
                                                             "littlecreatures", //
                                                             new Date().getTime(), //
                                                             12, //
                                                             1 );

            getStore().store( first );

            Calendar twoDaysAgo = Calendar.getInstance();
            twoDaysAgo.add( Calendar.DATE, -2 );

            String seconduuid = UUID.randomUUID().toString();

            PathTrackingEntry second = new PathTrackingEntry( seconduuid, //
                                                              "nodeId", //
                                                              "org.au.beer.TheBest", //
                                                              "littlecreatures", //
                                                              twoDaysAgo.getTime().getTime(), //
                                                              12, //
                                                              1 );

            getStore().store( second );

            Assert.assertFalse( getStore().retrieveAll().isEmpty() );

            Assert.assertEquals( 2, getStore().retrieveAll().size() );

            PathTrackingEntry third = new PathTrackingEntry( firstuuid, //
                                                             "nodeId", //
                                                             "org.au.beer.TheBest", //
                                                             "littlecreatures", //
                                                             new Date().getTime(), //
                                                             23, //
                                                             2 );

            getStore().store( third );

            Assert.assertFalse( getStore().retrieveAll().isEmpty() );

            Assert.assertEquals( 2, getStore().retrieveAll().size() );

            Assert.assertEquals( 2, getStore().retrieveAll().get( firstuuid ).size() );

            Calendar yesterday = Calendar.getInstance();
            yesterday.add( Calendar.DATE, -1 );

            Collection<PathCallInformation> trackingIds = getStore().retrieveTrackingIds( yesterday.getTime(), new Date() );


            Assert.assertEquals( 1, trackingIds.size() );

            Assert.assertEquals( first.getTrackingId(), trackingIds.iterator().next().getTrackingId() );

            Collection<PathTrackingEntry> entries = getStore().retrieve( firstuuid );

            Assert.assertEquals( 2, entries.size() );


        }
        finally
        {
            cluster.truncate( builder.getKeyspace(), builder.getPathTrackingColumFamily() );

            Assert.assertTrue( getStore().retrieveAll().isEmpty() );

            Assert.assertEquals( 0, getStore().retrieveAll().size() );

        }
    }


}
