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
package org.apache.sirona.tracking;

import org.apache.sirona.configuration.ioc.IoCs;
import org.apache.sirona.store.DataStoreFactory;
import org.apache.sirona.store.tracking.PathTrackingDataStore;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Contains logic to track class#method invocation path
 */
public class PathTracker
{
    private static final PathTrackingDataStore PATH_TRACKING_DATA_STORE =
            IoCs.findOrCreateInstance(DataStoreFactory.class).getPathTrackingDataStore();

    // FIXME olamy: so not using InheritableThreadLocal will create a new uuid in case of thread creation
    // whereas it's technically the same "transaction" (ie jvm call path)
    //private static final InheritableThreadLocal<String> THREAD_LOCAL_TX = new InheritableThreadLocal<String>()

    /**
     * Thread local to store a "transaction id" (i.e a java call)
     */
    private static final ThreadLocal<String> THREAD_LOCAL_TX = new ThreadLocal<String>()
    {
        @Override
        protected String initialValue()
        {
            return "Sirona-" + UUID.randomUUID().toString();// + System.nanoTime();//
        }

        /*
        @Override
        protected String childValue( String s )
        {
            return this.get();
        }
        */

    };

    // FIXME olamy: AtomicInteger because of starting multiple level but that's not supported with the THREAD_LOCAL_TX
    // because it doesn't use an InheritableThreadLocal so really sure :-)

    /**
     * Thread Local to store current call tree level
     */
    private static final ThreadLocal<AtomicInteger> THREAD_LOCAL_LEVEL = new ThreadLocal<AtomicInteger>()
    {
        @Override
        protected AtomicInteger initialValue()
        {
            return new AtomicInteger( 0 );
        }
    };

    private static final ThreadLocal<List<PathTrackingEntry>> THREAD_LOCAL_ENTRIES = new ThreadLocal<List<PathTrackingEntry>>(){
        @Override
        protected List<PathTrackingEntry> initialValue()
        {
            return new ArrayList<PathTrackingEntry>(  );
        }
    };

    // TODO: we should use a single threadlocal  (PatTracker itself?, other info are in it normally) and not 3
    private static void cleanUp() {
        THREAD_LOCAL_TX.remove();
        THREAD_LOCAL_LEVEL_INFO.remove();
        THREAD_LOCAL_LEVEL.remove();
        THREAD_LOCAL_ENTRIES.remove();
    }

    public static class PathTrackingInformation
    {
        private String className;

        private String methodName;

        private PathTrackingInformation parent;

        private long start;
        private long end;

        private int level;

        public PathTrackingInformation( String className, String methodName )
        {
            this.className = className;
            this.methodName = methodName;
        }

        public String getClassName()
        {
            return className;
        }

        public String getMethodName()
        {
            return methodName;
        }

        public PathTrackingInformation getParent()
        {
            return parent;
        }

        public void setParent( PathTrackingInformation parent )
        {
            this.parent = parent;
        }

        public void setStart(final long start) {
            this.start = start;
        }

        public long getStart() {
            return start;
        }

        public long getEnd() {
            return end;
        }

        public void setEnd(final long end) {
            this.end = end;
        }

        public int getLevel() {
            return level;
        }

        public void setLevel(final int level) {
            this.level = level;
        }

        @Override
        public String toString()
        {
            return "PathTrackingInformation{className='" + className
                    + "', methodName='" + methodName
                    + "\', parent=" + parent + '}';
        }
    }

    /**
     * Thread local to store the current Class#Method used
     * prevent to inc level in same class used
     */
    private static final ThreadLocal<PathTrackingInformation> THREAD_LOCAL_LEVEL_INFO =
        new ThreadLocal<PathTrackingInformation>();


    // Returns the current thread's unique ID, assigning it if necessary
    public static String get()
    {
        return THREAD_LOCAL_TX.get();
    }

    public static void set( String uuid )
    {
        THREAD_LOCAL_TX.set( uuid );
    }


    // An other solution could be using Thread.currentThread().getStackTrace() <- very slow

    public static PathTracker start( PathTrackingInformation pathTrackingInformation )
    {
        final int level;
        PathTrackingInformation current = THREAD_LOCAL_LEVEL_INFO.get();
        if ( current  == null )
        {
            level = THREAD_LOCAL_LEVEL.get().incrementAndGet();
            pathTrackingInformation.setLevel(level);
        }
        else
        {
            // same class so no inc
            if ( current != pathTrackingInformation )
            {
                level = THREAD_LOCAL_LEVEL.get().incrementAndGet();
                pathTrackingInformation.setLevel(level);
                pathTrackingInformation.setParent( current );
            }


        }
        pathTrackingInformation.setStart(System.nanoTime());

        THREAD_LOCAL_LEVEL_INFO.set( pathTrackingInformation );

        //System.out.println("start level: " + level + " for key " + key);

        return new PathTracker(); // TODO: see if this shouldn't be it which should be in *the* ThreadLocal
    }

    public void stop( PathTrackingInformation pathTrackingInformation )
    {
        final long end = System.nanoTime();
        final long start = pathTrackingInformation.getStart();

        final String uuid = PathTracker.get();

        final PathTrackingInformation current = THREAD_LOCAL_LEVEL_INFO.get();
        // same invocation so no inc, class can do recursion so don't use classname/methodname
        if ( pathTrackingInformation != current )
        {
            THREAD_LOCAL_LEVEL.get().decrementAndGet();
            THREAD_LOCAL_LEVEL_INFO.set( pathTrackingInformation.getParent() );
        }

        // FIXME get node from configuration!
        final PathTrackingEntry pathTrackingEntry =
            new PathTrackingEntry( uuid, "node", pathTrackingInformation.getClassName(), pathTrackingInformation.getMethodName(),
                                   start, ( end - start ), pathTrackingInformation.getLevel());

        THREAD_LOCAL_ENTRIES.get().add( pathTrackingEntry );

        if (pathTrackingInformation.getLevel() == 1 && pathTrackingInformation.getParent() == null) { // 0 is never reached so 1 is first
            List<PathTrackingEntry> pathTrackingEntries = THREAD_LOCAL_ENTRIES.get();
            PATH_TRACKING_DATA_STORE.store( pathTrackingEntries );
            PathTracker.cleanUp();
        }
    }


}
