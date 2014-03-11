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
import org.apache.sirona.javaagent.logging.SironaAgentLogging;
import org.apache.sirona.javaagent.spi.Order;
import org.apache.sirona.tracking.PathTracker;
import org.apache.sirona.tracking.PathTrackingInformation;

@Order( 1 )
@AutoSet
/**
 * This listener is responsible to track/record class#method path using {@link org.apache.sirona.tracking.PathTracker}
 */
public class PathTrackingListener
    extends ConfigurableListener
{

    private static final Integer PATH_TRACKER_KEY = "Sirona-path-tracker-key".hashCode();

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

        SironaAgentLogging.debug(
            "PathTrackingListener#accept, TRACKING_ACTIVATED: {0}, key: {1}", TRACKING_ACTIVATED, key );

        return TRACKING_ACTIVATED;
    }

    /**
     * executed before method called to configure the start {@link org.apache.sirona.tracking.PathTrackingInformation}
     * and set various thread local variable as invocation level
     * will call {@link org.apache.sirona.tracking.PathTracker#start(org.apache.sirona.tracking.PathTrackingInformation)}
     *
     * @param context
     */
    @Override
    public void before( AgentContext context )
    {

        String key = context.getKey();
        SironaAgentLogging.debug( "PathTrackingListener#before: {0}", key );

        int lastDot = key.lastIndexOf( "." );

        String className = key.substring( 0, lastDot );
        String methodName = key.substring( lastDot + 1, key.length() );

        final PathTrackingInformation pathTrackingInformation =
            new PathTrackingInformation( className, methodName );

        SironaAgentLogging.debug( "call PathTracker#start with {0}", pathTrackingInformation );

        context.put( PATH_TRACKER_KEY, PathTracker.start( pathTrackingInformation ) );
    }

    /**
     * will call {@link org.apache.sirona.tracking.PathTracker#stop()}
     *
     * @param context
     * @param result
     * @param error
     */
    @Override
    public void after( AgentContext context, Object result, Throwable error )
    {

        SironaAgentLogging.debug( "PathTrackingListener#after: {0}", context.getKey() );

        context.get( PATH_TRACKER_KEY, PathTracker.class ).stop();
    }
}
