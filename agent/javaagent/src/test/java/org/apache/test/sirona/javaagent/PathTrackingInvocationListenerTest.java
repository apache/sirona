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
package org.apache.test.sirona.javaagent;

import org.apache.sirona.configuration.ioc.IoCs;
import org.apache.sirona.javaagent.AgentArgs;
import org.apache.sirona.javaagent.JavaAgentRunner;
import org.apache.sirona.pathtracking.test.ExtendedInMemoryPathTrackingDataStore;
import org.apache.sirona.store.DataStoreFactory;
import org.apache.sirona.store.tracking.InMemoryPathTrackingDataStore;
import org.apache.sirona.tracking.PathTrackingEntry;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 */
@RunWith(JavaAgentRunner.class)
public class PathTrackingInvocationListenerTest
{

    @Test
    @AgentArgs(value = "",
               sysProps = "project.build.directory=${project.build.directory}|sirona.agent.debug=${sirona.agent.debug}|org.apache.sirona.configuration.sirona.properties=${project.build.directory}/test-classes/pathtracking/sirona.properties")
    public void simpleTest()
        throws Exception
    {

        App app = new App();
        app.beer();

        DataStoreFactory dataStoreFactory = IoCs.findOrCreateInstance( DataStoreFactory.class );

        ExtendedInMemoryPathTrackingDataStore ptds =
            ExtendedInMemoryPathTrackingDataStore.class.cast( dataStoreFactory.getPathTrackingDataStore() );

        Map<String, Set<PathTrackingEntry>> all = ptds.retrieveAll();

        //Assert.assertTrue( !all.isEmpty() );

/* FIXME: fix stop() to get consistent storage

        // test only one Thread so only one trackingId
/*
        Assert.assertEquals( 1, all.size() );

        List<PathTrackingEntry> entries = new ArrayList<PathTrackingEntry>( all.values().iterator().next() );

        PathTrackingEntry first = entries.get( 0 );

        System.out.println( "first entry: " + first );

        PathTrackingEntry second = entries.get( 1 );

        System.out.println( "second entry: " + second );

        PathTrackingEntry last = entries.get( entries.size() - 1 );

        System.out.println( "last entry: " + last );

        for ( PathTrackingEntry entry : entries )
        {
            System.out.println( "entry:" + entry );
        }
*/
    }


    public static class App
    {
        public void foo()
            throws Exception
        {
            Thread.sleep( 1000 );
        }

        public void beer()
            throws Exception
        {
            this.foo();
        }
    }

}
