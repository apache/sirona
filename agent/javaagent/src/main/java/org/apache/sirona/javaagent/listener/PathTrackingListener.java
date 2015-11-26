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
import org.apache.sirona.javaagent.tracking.PathTracker;
import org.apache.sirona.pathtracking.PathTrackingInformation;
import org.apache.sirona.spi.Order;

@Order( 1 )
@AutoSet
/**
 * This listener is responsible to track/record class#method path using {@link org.apache.sirona.javaagent.tracking.PathTracker}
 */ public class PathTrackingListener
    extends ConfigurableListener
{

    private static final Integer PATH_TRACKER_KEY = -2;// "Sirona-path-tracker-key".hashCode();

    private static final boolean TRACKING_ACTIVATED =
        Configuration.is( Configuration.CONFIG_PROPERTY_PREFIX + "javaagent.path.tracking.activate", false );


    @Override
    public boolean accept( String key, byte[] rawClassBuffer )
    {
        boolean include = super.accept( key, rawClassBuffer );
        if ( !include )
        {
            return false;
        }

        return TRACKING_ACTIVATED;
    }

    /**
     * executed before method called to configure the start {@link org.apache.sirona.pathtracking.PathTrackingInformation}
     * and set various thread local variable as invocation level
     * will call {@link org.apache.sirona.javaagent.tracking.PathTracker#start(PathTrackingInformation, Object)}
     *
     * @param context
     */
    @Override
    public void before( AgentContext context )
    {

        String key = context.getKey();

        String className = extractClassName( key );
        String methodName = extractMethodName( key );

        final PathTrackingInformation pathTrackingInformation = new PathTrackingInformation( className, methodName );

        context.put( PATH_TRACKER_KEY, PathTracker.start( pathTrackingInformation, context.getReference() ) );
    }

    /**
     * @param key format: org.apache.test.sirona.javaagent.App.foo() or org.apache.test.sirona.javaagent.App.pub(java.lang.String)
     * @return
     */
    static String extractClassName( String key )
    {
        if ( key == null )
        {
            return null;
        }

        int firstParenthesis = key.indexOf( '(' );

        while ( firstParenthesis > 0 )
        {
            if ( key.charAt( firstParenthesis ) == '.' )
            {
                return key.substring( 0, firstParenthesis );
            }
            firstParenthesis--;
        }

        return key;
    }


    static String extractMethodName( String key )
    {
        if ( key == null )
        {
            return null;
        }

        int firstParenthesis = key.indexOf( '(' );

        int j = firstParenthesis;

        while ( j > 0 )
        {
            if ( key.charAt( j ) == '.' )
            {
                return key.substring( j + 1, key.length() );
            }
            j--;
        }

        return key;
    }

    /**
     * will call {@link org.apache.sirona.javaagent.tracking.PathTracker#stop(Object reference)}
     *
     * @param context
     * @param result
     * @param error
     */
    @Override
    public void after( AgentContext context, Object result, Throwable error )
    {
        context.get( PATH_TRACKER_KEY, PathTracker.class ).stop( context.getReference() );
    }
}
