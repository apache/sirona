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

package org.apache.commons.monitoring;

import org.apache.commons.monitoring.impl.DefaultRepository;
import org.apache.commons.monitoring.impl.DefaultStopWatch;

/**
 * Utility class for simpified application instrumentation
 *
 * @author <a href="mailto:nicolas@apache.org">Nicolas De Loof</a>
 */
public final class Monitoring
{
    private Monitoring()
    {
        super();
    }

    private static Repository repository = new DefaultRepository();

    public static Monitor getMonitor( String name, String category, String subsystem )
    {
        return repository.getMonitor( name, category, subsystem );
    }

    public static Monitor getMonitor( String name, String category )
    {
        return getMonitor( name, category, null );
    }

    public static Monitor getMonitor( String name )
    {
        return getMonitor( name, null, null );
    }

    public static StopWatch start( String name )
    {
        return start( name, null, null );
    }

    public static StopWatch start( String name, String category )
    {
        return start( name, category, null );
    }

    public static StopWatch start( String name, String category, String subsystem )
    {
        return repository.start( getMonitor( name, category, subsystem ) );
    }

    public static void setRepository( Repository repository )
    {
        Monitoring.repository = repository;
    }

    public static Repository getRepository()
    {
        return repository;
    }

}
