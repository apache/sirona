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

import java.util.UUID;

/**
 * @author Olivier Lamy
 */
public class PathTrackingThreadLocal
{
    //private static final InheritableThreadLocal<String> THREAD_LOCAL = new InheritableThreadLocal<String>()
    private static final ThreadLocal<String> THREAD_LOCAL = new ThreadLocal<String>()
    {
        @Override
        protected String initialValue()
        {
            return "Sirona-" + System.nanoTime();// + UUID.randomUUID().toString();
        }

        /*
        @Override
        protected String childValue( String s )
        {
            return this.get();
        }
        */

    };

    // Returns the current thread's unique ID, assigning it if necessary
    public static String get()
    {
        return THREAD_LOCAL.get();
    }

    public static void set(String uuid)
    {
        THREAD_LOCAL.set( uuid );
    }
}
