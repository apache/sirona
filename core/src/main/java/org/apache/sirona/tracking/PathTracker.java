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

import org.apache.sirona.configuration.Configuration;
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
    private static final String NODE =
            Configuration.getProperty(Configuration.CONFIG_PROPERTY_PREFIX + "javaagent.path.tracking.marker",
                    Configuration.getProperty("org.apache.sirona.cube.CubeBuilder.marker", "node"));

    private static final PathTrackingDataStore PATH_TRACKING_DATA_STORE =
            IoCs.findOrCreateInstance(DataStoreFactory.class).getPathTrackingDataStore();

    private static final ThreadLocal<Context> THREAD_LOCAL = new ThreadLocal<Context>() {
        @Override
        protected Context initialValue() {
            return new Context();
        }
    };

    private static class Context {
        private String uuid;
        private AtomicInteger level;
        private List<PathTrackingEntry> entries;
        private PathTrackingInformation trackingInformation;

        private Context() {
            this.uuid = "Sirona-" + UUID.randomUUID().toString();
            this.level = new AtomicInteger(0);
            this.entries = new ArrayList<PathTrackingEntry>();
        }
    }

    private static void cleanUp() {
        THREAD_LOCAL.remove();
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


    // An other solution could be using Thread.currentThread().getStackTrace() <- very slow

    public static PathTracker start( PathTrackingInformation pathTrackingInformation )
    {
        final Context context = THREAD_LOCAL.get();

        final int level;
        final PathTrackingInformation current = context.trackingInformation;
        if ( current  == null )
        {
            level = context.level.incrementAndGet();
            pathTrackingInformation.setLevel(level);
        }
        else
        {
            // same class so no inc
            if ( current != pathTrackingInformation )
            {
                level = context.level.incrementAndGet();
                pathTrackingInformation.setLevel(level);
                pathTrackingInformation.setParent( current );
            }


        }
        pathTrackingInformation.setStart(System.nanoTime());

        context.trackingInformation = pathTrackingInformation;

        //System.out.println("start level: " + level + " for key " + key);

        return new PathTracker(); // TODO: see if this shouldn't be it which should be in *the* ThreadLocal
    }

    public void stop( PathTrackingInformation pathTrackingInformation )
    {
        final long end = System.nanoTime();
        final long start = pathTrackingInformation.getStart();
        final Context context = THREAD_LOCAL.get();

        final String uuid = context.uuid;

        final PathTrackingInformation current = context.trackingInformation;
        // same invocation so no inc, class can do recursion so don't use classname/methodname
        if ( pathTrackingInformation != current )
        {
            context.level.decrementAndGet();
            context.trackingInformation = pathTrackingInformation.getParent();
        }

        final PathTrackingEntry pathTrackingEntry =
            new PathTrackingEntry( uuid, NODE, pathTrackingInformation.getClassName(), pathTrackingInformation.getMethodName(),
                                   start, ( end - start ), pathTrackingInformation.getLevel());

        context.entries.add(pathTrackingEntry);

        if (pathTrackingInformation.getLevel() == 1 && pathTrackingInformation.getParent() == null) { // 0 is never reached so 1 is first
            PATH_TRACKING_DATA_STORE.store( context.entries );
            PathTracker.cleanUp();
        }
    }


}
