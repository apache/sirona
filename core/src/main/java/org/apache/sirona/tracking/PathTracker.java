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

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Olivier Lamy
 */
public class PathTracker
{

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
            return new AtomicInteger( 1 );
        }
    };

    public static class PathTrackingInformation
    {
        private String className;

        private String methodName;

        private PathTrackingInformation parent;

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
        new ThreadLocal<PathTrackingInformation>()
        {
            // no op
        };


    // Returns the current thread's unique ID, assigning it if necessary
    public static String get()
    {
        return THREAD_LOCAL_TX.get();
    }

    public static void set( String uuid )
    {
        THREAD_LOCAL_TX.set( uuid );
    }


    // An other solution could be using Thread.currentThread().getStackTrace()

    public static int start( PathTrackingInformation pathTrackingInformation )
    {
        final int level;
        PathTrackingInformation current = THREAD_LOCAL_LEVEL_INFO.get();
        if ( current  == null )
        {
            level = THREAD_LOCAL_LEVEL.get().incrementAndGet();
        }
        else
        {
            // same class so no inc
            if ( current.className.equals( pathTrackingInformation.className ) //
                && current.methodName.equals( pathTrackingInformation.methodName ) )
            {
                // yup sounds to be the same level so no level inc!
                level = THREAD_LOCAL_LEVEL.get().get();
            }
            else
            {
                level = THREAD_LOCAL_LEVEL.get().incrementAndGet();
            }

            pathTrackingInformation.setParent( current );
        }

        THREAD_LOCAL_LEVEL_INFO.set( pathTrackingInformation );

        //System.out.println("start level: " + level + " for key " + key);

        return level;
    }

    public static int stop( PathTrackingInformation pathTrackingInformation )
    {
        final int level;

        PathTrackingInformation current = THREAD_LOCAL_LEVEL_INFO.get();
        // same class so no inc
        if ( current.className.equals( pathTrackingInformation.className ) //
            && current.methodName.equals( pathTrackingInformation.methodName ) )
        {
            // yup sounds to be the same level so no level inc!
            level = THREAD_LOCAL_LEVEL.get().get();
        }
        else
        {
            level = THREAD_LOCAL_LEVEL.get().decrementAndGet();
        }

        THREAD_LOCAL_LEVEL_INFO.set( pathTrackingInformation );

        //System.out.println("start level: " + level + " for key " + key);

        return level;
    }


}
