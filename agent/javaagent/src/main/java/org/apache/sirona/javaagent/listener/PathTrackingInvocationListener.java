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

import org.apache.sirona.configuration.Configuration;
import org.apache.sirona.configuration.ioc.IoCs;
import org.apache.sirona.javaagent.AgentContext;
import org.apache.sirona.javaagent.SironaAgent;
import org.apache.sirona.javaagent.listener.ConfigurableListener;
import org.apache.sirona.store.DataStoreFactory;
import org.apache.sirona.store.tracking.PathTrackingDataStore;
import org.apache.sirona.tracking.PathTracker;
import org.apache.sirona.tracking.PathTrackingEntry;

/**
 *
 */
public class PathTrackingInvocationListener extends ConfigurableListener {

    private static final Integer TIMESTAMP_KEY = "Sirona-path-tracking-key".hashCode();

    private static final Integer PATH_TRACKING_LEVEL_KEY = "Sirona-path-tracking-level-key".hashCode();

    private static final boolean TRACKING_ACTIVATED =
        Configuration.is( Configuration.CONFIG_PROPERTY_PREFIX + "javaagent.path.tracking.activate", false );


    private PathTracker.PathTrackingInformation pathTrackingInformation;

    @Override
    public boolean accept( String key )
    {

        boolean include = super.accept( key );
        if ( !include )
        {
            return false;
        }

        if ( SironaAgent.AGENT_DEBUG )
        {
            System.out.println(
                "PathTrackingInvocationListener#accept, TRACKING_ACTIVATED:" + TRACKING_ACTIVATED + ", key: " + key );
        }

        if ( !TRACKING_ACTIVATED )
        {
            return false;
        }

        int lastDot = key.lastIndexOf( "." );

        String className = key.substring( 0, lastDot );
        String methodName = key.substring(lastDot + 1, key.length());

        this.pathTrackingInformation = new PathTracker.PathTrackingInformation( className, methodName );

        return true;
    }

    @Override
    public void before( AgentContext context )
    {
        if ( SironaAgent.AGENT_DEBUG )
        {
            System.out.println( "PathTrackingInvocationListener#before:" + context.getKey() );
        }
        context.put( PATH_TRACKING_LEVEL_KEY, PathTracker.start( this.pathTrackingInformation ) );
    }

    @Override
    public void after( AgentContext context, Object result, Throwable error )
    {

        if ( SironaAgent.AGENT_DEBUG )
        {
            System.out.println( "PathTrackingInvocationListener#after: " + context.getKey() );
        }

        context.get(PATH_TRACKING_LEVEL_KEY, PathTracker.class).stop(pathTrackingInformation);
    }
}
