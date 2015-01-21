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

import org.apache.sirona.pathtracking.PathTrackingEntry;
import org.apache.sirona.store.memory.tracking.InMemoryPathTrackingDataStore;
import org.junit.Assert;
import org.junit.Test;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 *
 */
public class InMemoryPathTrackingTest
{
    @Test
    public void testStoreRetrieveAll()
        throws Exception
    {
        InMemoryPathTrackingDataStore store = new InMemoryPathTrackingDataStore();

        long now = System.nanoTime();

        PathTrackingEntry entry =
            new PathTrackingEntry( UUID.randomUUID().toString(), "nodeId", "className", "methodName", now, now + 1, 1 );

        store.store( entry );

        Map<String, Set<PathTrackingEntry>> all = store.getPathTrackingEntries();

        Assert.assertNotNull( all );
        Assert.assertEquals( 1, all.size() );
        Assert.assertEquals( 1, all.entrySet().iterator().next().getValue().size() );

        entry = all.entrySet().iterator().next().getValue().iterator().next();

        Assert.assertEquals( "nodeId", entry.getNodeId() );

        /*
        we do not clear anymore entries pointer are freeing once sended
        store.clearEntries();

        all = store.getPathTrackingEntries();

        Assert.assertNotNull( all );
        Assert.assertEquals( 0, all.size() );
        */
    }
}
