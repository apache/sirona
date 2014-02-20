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
import org.apache.sirona.configuration.ioc.AutoSet;
import org.apache.sirona.javaagent.AgentContext;
import org.apache.sirona.javaagent.SironaAgent;
import org.apache.sirona.javaagent.spi.Order;
import org.apache.sirona.tracking.PathTracker;

@Order(1)
@AutoSet
/**
 * This listener is responsible to track/record class#method path using {@link org.apache.sirona.tracking.PathTracker}
 */
public class PathTrackingInvocationListener extends ConfigurableListener {

    private static final Integer PATH_TRACKER_KEY = "Sirona-path-tracker-key".hashCode();
    private static final Integer PATH_TRACKING_INFO_LEVEL_KEY = "Sirona-path-tracking-info-key".hashCode();

    private static final boolean TRACKING_ACTIVATED =
        Configuration.is( Configuration.CONFIG_PROPERTY_PREFIX + "javaagent.path.tracking.activate", false );

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

        return TRACKING_ACTIVATED;

    }

    /**
     * executed before method called to configure the start {@link org.apache.sirona.tracking.PathTracker.PathTrackingInformation}
     * and set various thread local variable as invocation level
     * will call {@link org.apache.sirona.tracking.PathTracker#start(org.apache.sirona.tracking.PathTracker.PathTrackingInformation)}
     * @param context
     */
    @Override
    public void before( AgentContext context )
    {
        if ( SironaAgent.AGENT_DEBUG )
        {
            System.out.println( "PathTrackingInvocationListener#before:" + context.getKey() );
        }

        String key = context.getKey();

        int lastDot = key.lastIndexOf( "." );

        String className = key.substring( 0, lastDot );
        String methodName = key.substring(lastDot + 1, key.length());

        final PathTracker.PathTrackingInformation pathTrackingInformation = new PathTracker.PathTrackingInformation(className, methodName);
        context.put(PATH_TRACKING_INFO_LEVEL_KEY, pathTrackingInformation);
        context.put(PATH_TRACKER_KEY, PathTracker.start(pathTrackingInformation));
    }

    /**
     *
     * will call {@link org.apache.sirona.tracking.PathTracker#stop(org.apache.sirona.tracking.PathTracker.PathTrackingInformation)}
     * @param context
     * @param result
     * @param error
     */
    @Override
    public void after( AgentContext context, Object result, Throwable error )
    {

        if ( SironaAgent.AGENT_DEBUG )
        {
            System.out.println( "PathTrackingInvocationListener#after: " + context.getKey() );
        }

        context.get(PATH_TRACKER_KEY, PathTracker.class)
                .stop(context.get(PATH_TRACKING_INFO_LEVEL_KEY, PathTracker.PathTrackingInformation.class));
    }
}
