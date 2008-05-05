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

package org.apache.commons.monitoring.aop;

import java.lang.reflect.Method;

import org.apache.commons.monitoring.Monitor;
import org.apache.commons.monitoring.Repository;
import org.apache.commons.monitoring.StopWatch;
import org.apache.commons.monitoring.Unit;

/**
 * A method interceptor that compute method invocation performances.
 * <p>
 * Concrete implementation will adapt the method interception API to
 * this class requirement.
 *
 * @author <a href="mailto:nicolas@apache.org">Nicolas De Loof</a>
 */
public abstract class AbstractPerformanceInterceptor<T>
{

    protected Repository repository;

    protected String category;

    protected String subsystem;

    protected MonitorNameExtractor monitorNameExtractor;

    public AbstractPerformanceInterceptor()
    {
        super();
    }

    /**
     * API neutral method invocation
     */
    protected Object doInvoke( T invocation )
        throws Throwable
    {
        String name = getMonitorName( invocation );
        if ( name == null )
        {
            return proceed( invocation );
        }
        Monitor monitor = repository.getMonitor( name, category, subsystem );
        StopWatch stopwatch = repository.start( monitor );
        Throwable error = null;
        try
        {
            return proceed( invocation );
        }
        catch ( Throwable t )
        {
            error = t;
            throw t;
        }
        finally
        {
            stopwatch.stop();
            beforeReturning( monitor, error, stopwatch.getElapsedTime() );
        }
    }

    /**
     * @param invocation
     * @return
     */
    protected abstract Object proceed( T invocation )
        throws Throwable;

    /**
     * @param invocation
     * @return
     */
    protected abstract String getMonitorName( T invocation );

    /**
     * Compute the monitor name associated to this method invocation
     *
     * @param method method being invoked
     * @return monitor name. If <code>null</code>, nothing will be monitored
     */
    protected String getMonitorName( Method method )
    {
        return monitorNameExtractor.getMonitorName( method );
    }

    /**
     * @param monitor the monitor associated to the method invocation
     * @param error Throwable thrown by the method invocation if any
     * @param duration the duration of the method invocation
     */
    protected void beforeReturning( Monitor monitor, Throwable error, long duration )
    {
        if ( error != null )
        {
            monitor.getCounter( Monitor.FAILURES ).add( duration, Unit.NANOS );
        }
    }

    /**
     * Set a custom application-defined repository
     *
     * @param repository
     */
    public void setRepository( Repository repository )
    {
        this.repository = repository;
    }

    public void setCategory( String category )
    {
        this.category = category;
    }

    public void setSubsystem( String subsystem )
    {
        this.subsystem = subsystem;
    }

    /**
     * @param monitorNameExtractor the monitorNameExtractor to set
     */
    public void setMonitorNameExtractor( MonitorNameExtractor monitorNameExtractor )
    {
        this.monitorNameExtractor = monitorNameExtractor;
    }
}