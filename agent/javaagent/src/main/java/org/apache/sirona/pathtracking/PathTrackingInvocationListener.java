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

import org.apache.sirona.configuration.Configuration;
import org.apache.sirona.configuration.ioc.IoCs;
import org.apache.sirona.javaagent.AgentContext;
import org.apache.sirona.javaagent.listener.ConfigurableListener;
import org.apache.sirona.javaagent.spi.InvocationListener;
import org.apache.sirona.store.tracking.PathTrackingDataStore;
import org.apache.sirona.tracking.PathTrackingEntry;

/**
 * @author Olivier Lamy
 */
public class PathTrackingInvocationListener
    extends ConfigurableListener
    implements InvocationListener
{

    private static final Integer TIMESTAMP_KEY = "Sirona-path-tracking-key".hashCode();

    private static final boolean TRACKING_ACTIVATED =
        Configuration.is( Configuration.CONFIG_PROPERTY_PREFIX + "javaagent.path.tracking.activate", false );

    private static final boolean DEBUG = Boolean.getBoolean( "sirona.agent.debug" );

    /**
     * fqcn.methodName
     */
    private String key;

    @Override
    public boolean accept( String key )
    {

        if ( DEBUG )
        {
            System.out.println(
                "PathTrackingInvocationListener#accept, TRACKING_ACTIVATED:" + TRACKING_ACTIVATED + ", key: " + key );
            //+ "super accept:" + accept );
        }

        if ( !TRACKING_ACTIVATED )
        {
            return false;
        }

        this.key = key;
        return true;
    }

    @Override
    public void before( AgentContext context )
    {
        if ( DEBUG )
        {
            System.out.println( "PathTrackingInvocationListener#before:" + context.getKey() );
        }
        context.put( TIMESTAMP_KEY, Long.valueOf( System.nanoTime() ) );
        context.getKey();
    }

    @Override
    public void after( AgentContext context, Object result, Throwable error )
    {

        if ( DEBUG )
        {
            System.out.println( "PathTrackingInvocationListener#after: " + context.getKey() );
        }

        Long end = System.nanoTime();
        Long start = Long.class.cast( context.get( TIMESTAMP_KEY, Long.class ) );
        int lastDot = this.key.lastIndexOf( "." );

        String className = this.key.substring( 0, lastDot );
        String methodName = this.key.substring( lastDot + 1, this.key.length() );

        // FIXME get node from configuration
        // PathTrackingThreadLocal.get()
        PathTrackingEntry pathTrackingEntry =
            new PathTrackingEntry( "1", "node", className, methodName, start, ( end - start ) );

        if ( DEBUG )
        {
            System.out.println( "PathTrackingInvocationListener: after: " + pathTrackingEntry.toString() );
        }

        IoCs.getInstance( PathTrackingDataStore.class ).store( pathTrackingEntry );

    }
}
