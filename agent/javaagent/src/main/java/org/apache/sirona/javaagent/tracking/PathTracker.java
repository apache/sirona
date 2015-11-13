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
package org.apache.sirona.javaagent.tracking;


import org.apache.sirona.configuration.Configuration;
import org.apache.sirona.configuration.ioc.Destroying;
import org.apache.sirona.configuration.ioc.IoCs;
import org.apache.sirona.pathtracking.Context;
import org.apache.sirona.pathtracking.PathTrackingEntry;
import org.apache.sirona.pathtracking.PathTrackingInformation;
import org.apache.sirona.pathtracking.PathTrackingInvocationListener;
import org.apache.sirona.pathtracking.UniqueIdGenerator;
import org.apache.sirona.spi.Order;
import org.apache.sirona.spi.SPI;
import org.apache.sirona.store.DataStoreFactory;
import org.apache.sirona.store.tracking.PathTrackingDataStore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Contains logic to track class#method invocation path
 */
public class PathTracker
{
    private static final String NODE =
        Configuration.getProperty( Configuration.CONFIG_PROPERTY_PREFIX + "javaagent.path.tracking.marker", //
                                   Configuration.getProperty( "org.apache.sirona.cube.CubeBuilder.marker", "node" ) );

    private static final PathTrackingDataStore PATH_TRACKING_DATA_STORE =
        IoCs.findOrCreateInstance( DataStoreFactory.class ).getPathTrackingDataStore();


    private static final UniqueIdGenerator ID_GENERATOR =
        IoCs.findOrCreateInstance( UniqueIdGenerator.class );


    private static final ThreadLocal<Context> THREAD_LOCAL = new ThreadLocal<Context>()
    {
        @Override
        protected Context initialValue()
        {
            return new Context(ID_GENERATOR.next());
        }
    };

    private final PathTrackingInformation currentPathTrackingInformation;

    private static final boolean USE_EXECUTORS = Boolean.parseBoolean(
        Configuration.getProperty( Configuration.CONFIG_PROPERTY_PREFIX + "pathtracking.useexecutors", "false" ) );

    private static boolean USE_SINGLE_STORE = Boolean.parseBoolean(
        Configuration.getProperty( Configuration.CONFIG_PROPERTY_PREFIX + "pathtracking.singlestore", "false" ) );

    private static boolean USE_STORE = Boolean.parseBoolean(
        Configuration.getProperty( Configuration.CONFIG_PROPERTY_PREFIX + "pathtracking.store", "true" ) );

    protected static ExecutorService EXECUTORSERVICE;

    static
    {

        if ( USE_EXECUTORS )
        {
            int threadsNumber =
                Configuration.getInteger( Configuration.CONFIG_PROPERTY_PREFIX + "pathtracking.executors", 5 );
            EXECUTORSERVICE = Executors.newFixedThreadPool( threadsNumber );
        }
    }

    private static PathTrackingInvocationListener[] LISTENERS;

    static
    {
        ClassLoader classLoader = PathTracker.class.getClassLoader();

        if ( classLoader == null )
        {
            classLoader = Thread.currentThread().getContextClassLoader();
        }

        List<PathTrackingInvocationListener> listeners = new ArrayList<PathTrackingInvocationListener>();

        Iterator<PathTrackingInvocationListener> iterator =
            SPI.INSTANCE.find( PathTrackingInvocationListener.class, classLoader ).iterator();

        while ( iterator.hasNext() )
        {
            try
            {
                listeners.add( IoCs.autoSet( iterator.next() ) );
            }
            catch ( Exception e )
            {
                throw new RuntimeException( e.getMessage(), e );
            }
        }

        Collections.sort( listeners, ListenerComparator.INSTANCE );
        LISTENERS = listeners.toArray( new PathTrackingInvocationListener[listeners.size()] );
    }


    public static PathTrackingInvocationListener[] getPathTrackingInvocationListeners()
    {
        return LISTENERS;
    }

    private PathTracker( final PathTrackingInformation pathTrackingInformation )
    {
        this.currentPathTrackingInformation = pathTrackingInformation;
    }


    private static void cleanUp()
    {
        THREAD_LOCAL.remove();
    }

    // An other solution could be using Thread.currentThread().getStackTrace() <- very slow

    public static PathTracker start( PathTrackingInformation currentPathTrackingInformation, final Object reference )
    {

        final Context context = THREAD_LOCAL.get();

        int level = 0;
        final PathTrackingInformation startPathTrackingInformation = context.getStartPathTrackingInformation();

        if ( startPathTrackingInformation == null )
        {
            level = context.getLevel().incrementAndGet();
            currentPathTrackingInformation.setLevel( level );
            context.setStartPathTrackingInformation( currentPathTrackingInformation );
        }
        else
        {
            // same class so no inc
            if ( currentPathTrackingInformation != startPathTrackingInformation )
            {
                level = context.getLevel().incrementAndGet();
                currentPathTrackingInformation.setLevel( level );
            }
        }

        if ( level == 1 )
        {
            context.setStartPathObject( reference );
        }
        for ( PathTrackingInvocationListener listener : LISTENERS )
        {
            if ( level == 1 )
            {
                listener.startPath( context );
            }
            listener.enterMethod( currentPathTrackingInformation );
        }

        return new PathTracker( currentPathTrackingInformation );
    }


    public void stop( final Object reference )
    {
        final long end = System.nanoTime();
        final Context context = THREAD_LOCAL.get();

        final String uuid = context.getUuid();

        final PathTrackingInformation startPathTrackingInformation = context.getStartPathTrackingInformation();

        // same invocation so no inc, class can do recursion so don't use classname/methodname
        if ( startPathTrackingInformation != this.currentPathTrackingInformation )
        {
            context.getLevel().decrementAndGet();
        }

        if ( this.currentPathTrackingInformation != null )
        {
            for ( PathTrackingInvocationListener listener : LISTENERS )
            {
                listener.exitMethod( this.currentPathTrackingInformation );

            }
        }

        final PathTrackingEntry pathTrackingEntry =
            new PathTrackingEntry( uuid, NODE, this.currentPathTrackingInformation.getClassName(), //
                                   this.currentPathTrackingInformation.getMethodName(), //
                                   currentPathTrackingInformation.getStartDateNs(), //
                                   ( end - currentPathTrackingInformation.getStart() ), //
                                   this.currentPathTrackingInformation.getLevel() );

        if ( USE_STORE )
        {
            if ( USE_SINGLE_STORE )
            {
                PATH_TRACKING_DATA_STORE.store( pathTrackingEntry );
            }
            else
            {
                context.getEntries().add( pathTrackingEntry );
            }
        }
        if ( this.currentPathTrackingInformation.getLevel() == 1 && //
            ( context.getStartPathObject() != null && context.getStartPathObject() == reference ) )
        { // 0 is never reached so 1 is first
            if ( USE_STORE && !USE_SINGLE_STORE )
            {
                try
                {
                    Runnable runnable = new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            PATH_TRACKING_DATA_STORE.store( context.getEntries() );
                            PathTracker.cleanUp();
                        }
                    };
                    if ( USE_EXECUTORS )
                    {
                        EXECUTORSERVICE.submit( runnable );
                    }
                    else
                    {
                        runnable.run();
                    }
                }
                catch ( Throwable e )
                {
                    // as implementations can generate exception we simply ignore all exception happening here!!
                }
            }

            for ( PathTrackingInvocationListener listener : LISTENERS )
            {
                try
                {
                    listener.endPath( context );
                }
                catch ( Throwable e )
                {
                    // as listener implementations can generate exception we simply ignore all exception happening here!!
                }
            }

            THREAD_LOCAL.remove();

        }
    }

    @Destroying
    public void destroy()
    {
        PathTracker.shutdown();
    }

    public static void shutdown()
    {
        EXECUTORSERVICE.shutdownNow();
    }


    private static class ListenerComparator
        implements Comparator<PathTrackingInvocationListener>
    {
        private static final ListenerComparator INSTANCE = new ListenerComparator();

        private ListenerComparator()
        {
            // no-op
        }

        @Override
        public int compare( final PathTrackingInvocationListener o1, final PathTrackingInvocationListener o2 )
        {
            final Order order1 = o1.getClass().getAnnotation( Order.class );
            final Order order2 = o2.getClass().getAnnotation( Order.class );
            if ( order2 == null )
            {
                return -1;
            }
            if ( order1 == null )
            {
                return 1;
            }
            return order1.value() - order2.value();
        }
    }

}
