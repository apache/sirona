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


import org.apache.sirona.javaagent.AgentContext;
import org.apache.sirona.javaagent.spi.InvocationListener;
import org.apache.sirona.pathtracking.Context;
import org.apache.sirona.pathtracking.PathTrackingInformation;
import org.apache.sirona.pathtracking.PathTrackingInvocationListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class MockPathTrackingInvocationListener
    implements PathTrackingInvocationListener, InvocationListener
{

    public static boolean START_PATH_CALLED = false;

    public static boolean END_PATH_CALLED = false;

    int startPathCallCount = 0;

    int enterMethodCallCount = 0;

    List<PathTrackingInformation> entered = new ArrayList<PathTrackingInformation>();

    int exitMethodCallCount = 0;

    List<PathTrackingInformation> exit = new ArrayList<PathTrackingInformation>();

    int endPathCallCount = 0;

    Map<String, AgentContext> contextPerKey = new HashMap<String, AgentContext>();


    @Override
    public void startPath( Context context )
    {
        START_PATH_CALLED = true;
        startPathCallCount++;
    }

    @Override
    public void enterMethod( PathTrackingInformation pathTrackingInformation  )
    {
        entered.add( pathTrackingInformation );
        enterMethodCallCount++;
    }

    @Override
    public void exitMethod(  PathTrackingInformation pathTrackingInformation  )
    {
        exit.add( pathTrackingInformation );
        exitMethodCallCount++;
    }

    @Override
    public void endPath( Context context )
    {
        END_PATH_CALLED = true;
        endPathCallCount++;
    }


    public boolean accept( String key, byte[] rawClassBuffer )
    {
        boolean accept = key.startsWith( "org.apache.test.sirona.javaagent.App" );
        return accept;
    }

    public void before( AgentContext context )
    {
        contextPerKey.put( context.getKey(), context );
    }

    public void after( AgentContext context, Object result, Throwable error )
    {

    }
}
