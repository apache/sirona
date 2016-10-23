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
import org.apache.sirona.javaagent.JavaAgentRunner;
import org.apache.sirona.pathtracking.PathTrackingEntry;
import org.apache.sirona.pathtracking.test.ExtendedInMemoryPathTrackingDataStore;
import org.apache.sirona.store.DataStoreFactory;
import org.apache.test.sirona.javaagent.App;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * this test validate we don't in StackOverflow when redirecting System.out and using debug mode
 */
@RunWith(JavaAgentRunner.class)
public class PathTrackingInvocationRecursiveTest
{

    @Test
    @AgentArgs(value = "",
               sysProps = "project.build.directory=${project.build.directory}|sirona.agent.debug=true|org.apache.sirona.configuration.sirona.properties=${project.build.directory}/test-classes/pathtracking/sirona.properties")
    public void simpleTest()
        throws Exception
    {

        App app = new App().redirectStreamout();
        app.beer();

        DataStoreFactory dataStoreFactory = IoCs.findOrCreateInstance( DataStoreFactory.class );

        ExtendedInMemoryPathTrackingDataStore ptds =
            ExtendedInMemoryPathTrackingDataStore.class.cast( dataStoreFactory.getPathTrackingDataStore() );

        Map<String, Set<PathTrackingEntry>> all = ptds.retrieveAll();

        System.out.println( all );

        boolean called = MockPathTrackingInvocationListener.START_PATH_CALLED;

        Assert.assertTrue( called );

        called = MockPathTrackingInvocationListener.END_PATH_CALLED;

        Assert.assertTrue( called );

        Assert.assertTrue( !all.isEmpty() );

        Assert.assertEquals(2, all.size() );

    }

}
