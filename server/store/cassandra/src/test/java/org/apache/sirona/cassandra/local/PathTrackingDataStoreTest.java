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

import org.apache.sirona.cassandra.framework.CassandraRunner;
import org.apache.sirona.cassandra.pathtracking.CassandraPathTrackingDataStore;
import org.apache.sirona.configuration.ioc.IoCs;
import org.apache.sirona.tracking.PathTracker;
import org.apache.sirona.tracking.PathTrackingEntry;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Date;

/**
 * @author Olivier Lamy
 */
@RunWith( CassandraRunner.class )
public class PathTrackingDataStoreTest
{

    @Test
    public void test_save()
        throws Exception
    {

        CassandraPathTrackingDataStore store = IoCs.processInstance( new CassandraPathTrackingDataStore() );

        PathTrackingEntry entry =
            new PathTrackingEntry( PathTracker.get(), "nodeId", "org.au.beer.TheBest", "littlecreatures",
                                   new Date().getTime(), 12, 1 );

        store.store( entry );

        //Assert.assertFalse( store.retrieveAll().isEmpty() );

    }
}
