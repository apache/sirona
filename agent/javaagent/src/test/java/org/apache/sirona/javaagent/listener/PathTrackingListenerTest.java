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
package org.apache.sirona.javaagent.listener;

import org.apache.sirona.configuration.ioc.IoCs;
import org.apache.sirona.javaagent.AgentArgs;
import org.apache.sirona.javaagent.AgentContext;
import org.apache.sirona.javaagent.JavaAgentRunner;
import org.apache.sirona.javaagent.spi.InvocationListener;
import org.apache.sirona.pathtracking.PathTrackingEntry;
import org.apache.sirona.pathtracking.PathTrackingInvocationListener;
import org.apache.sirona.pathtracking.test.ExtendedInMemoryPathTrackingDataStore;
import org.apache.sirona.store.DataStoreFactory;
import org.apache.sirona.javaagent.tracking.PathTracker;
import org.apache.test.sirona.javaagent.App;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 */
@RunWith( JavaAgentRunner.class )
public class PathTrackingListenerTest
{

    @Test
    @AgentArgs( value = "debug=true|sirona.agent.debug=${sirona.agent.debug}",
        sysProps = "project.build.directory=${project.build.directory}|sirona.agent.debug=${sirona.agent.debug}|org.apache.sirona.configuration.sirona.properties=${project.build.directory}/test-classes/pathtracking/sirona.properties|java.io.tmpdir=${project.build.directory}" )
    public void simpleTest()
        throws Exception
    {

        App app = new App();
        app.beer();

        DataStoreFactory dataStoreFactory = IoCs.findOrCreateInstance( DataStoreFactory.class );

        ExtendedInMemoryPathTrackingDataStore ptds =
            ExtendedInMemoryPathTrackingDataStore.class.cast( dataStoreFactory.getPathTrackingDataStore() );

        Map<String, Set<PathTrackingEntry>> all = ptds.retrieveAll();

        Assert.assertTrue( !all.isEmpty() );

        // test only one Thread so only one trackingId
        Assert.assertEquals( 1, all.size() );

        List<PathTrackingEntry> entries = new ArrayList<PathTrackingEntry>( all.values().iterator().next() );

        // so we have 4 entries constructor is ignored!

        Assert.assertEquals( entries.toString(), 4, entries.size() );

        for ( PathTrackingEntry entry : entries )
        {
            System.out.println( "entry:" + entry );
        }

        PathTrackingEntry entry = entries.get( 0 );

        Assert.assertEquals( "beer()", entry.getMethodName() );

        Assert.assertEquals( "org.apache.test.sirona.javaagent.App", entry.getClassName() );

        Assert.assertEquals( "level should be 1", 1, entry.getLevel() );

        entry = entries.get( 1 );

        Assert.assertEquals( "foo()", entry.getMethodName() );

        Assert.assertEquals( "org.apache.test.sirona.javaagent.App", entry.getClassName() );

        Assert.assertEquals( "level should be 2", 2, entry.getLevel() );

        // there is Thread.sleep( 500 ) so we can be sure a minimum for that

        Assert.assertTrue( entry.getExecutionTime() >= 500 * 1000000 );

        entry = entries.get( 2 );

        Assert.assertEquals( "pub(java.lang.String,java.util.List,int)", entry.getMethodName() );

        Assert.assertEquals( "org.apache.test.sirona.javaagent.App", entry.getClassName() );

        Assert.assertEquals( "level should be 2", 2, entry.getLevel() );

        Assert.assertTrue( entry.getExecutionTime() >= 100 * 1000000 );

        entry = entries.get( 3 );

        Assert.assertEquals( "bar()", entry.getMethodName() );

        Assert.assertEquals( "org.apache.test.sirona.javaagent.App", entry.getClassName() );

        Assert.assertEquals( "level should be 2", 3, entry.getLevel() );

        Assert.assertTrue( entry.getExecutionTime() >= 300 * 1000000 );

        // we have only one here
        PathTrackingInvocationListener listener = PathTracker.getPathTrackingInvocationListeners()[0];

        MockPathTrackingInvocationListener mock = (MockPathTrackingInvocationListener) ( listener );

        System.out.println( "mock.startPathCallCount: " + mock.startPathCallCount );

        Assert.assertEquals( 1, mock.startPathCallCount );

        Assert.assertEquals( 1, mock.endPathCallCount );

        Assert.assertEquals( mock.entered.toString(), 4, mock.enterMethodCallCount );

        Assert.assertEquals( mock.exit.toString(), 4, mock.exitMethodCallCount );

        InvocationListener[] listeners =
            AgentContext.listeners( "org.apache.test.sirona.javaagent.App.pub(java.lang.String,java.util.List,int)", //
                                    null );

        mock = findInstance( listeners );

        AgentContext agentContext =
            mock.contextPerKey.get( "org.apache.test.sirona.javaagent.App.pub(java.lang.String,java.util.List,int)" );

        Object[] parameters = agentContext.getMethodParameters();

        // "blabla", Arrays.asList( "Mountain Goat", "Fatyak" ), 2

        Assert.assertEquals( 3, parameters.length );

        Assert.assertEquals( "blabla", parameters[0] );

        Assert.assertTrue( List.class.isAssignableFrom( parameters[1].getClass() ) );

        Assert.assertTrue( ( (List) parameters[1] ).get( 0 ).equals( "Mountain Goat" ) );

        Assert.assertTrue( ( (List) parameters[1] ).get( 1 ).equals( "Fatyak" ) );

        Assert.assertEquals( 2, parameters[2] );

    }

    private MockPathTrackingInvocationListener findInstance( InvocationListener[] listeners )
    {
        for ( InvocationListener invocationListener : listeners )
        {
            if ( invocationListener.getClass().isAssignableFrom( MockPathTrackingInvocationListener.class ) )
            {
                return (MockPathTrackingInvocationListener) invocationListener;
            }
        }
        return null;
    }


}
