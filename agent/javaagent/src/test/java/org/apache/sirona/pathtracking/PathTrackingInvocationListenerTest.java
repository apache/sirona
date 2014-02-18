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
package org.apache.sirona.pathtracking;

import org.apache.sirona.configuration.ioc.IoCs;
import org.apache.sirona.javaagent.AgentArgs;
import org.apache.sirona.javaagent.JavaAgentRunner;
import org.apache.sirona.store.tracking.PathTrackingDataStore;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Olivier Lamy
 */
@RunWith( JavaAgentRunner.class )
public class PathTrackingInvocationListenerTest
{

    @Test
    @AgentArgs( value ="libs=${project.build.directory}/lib",
    sysProps = "project.build.directory=${project.build.directory}|sirona.agent.debug=${sirona.agent.debug}|org.apache.sirona.configuration.sirona.properties=${project.build.directory}/test-classes/pathtracking/sirona.properties")
    public void simpleTest()
    throws Exception
    {

        App app = new App();
        app.beer();

        PathTrackingDataStore ptds = IoCs.getInstance( PathTrackingDataStore.class );

        System.out.println ("simpleTest end");

    }


    public static class App
    {
        public void foo() throws Exception
        {
            Thread.sleep( 1000 );
        }

        public void beer() throws Exception
        {
            this.foo();
        }
    }

}
