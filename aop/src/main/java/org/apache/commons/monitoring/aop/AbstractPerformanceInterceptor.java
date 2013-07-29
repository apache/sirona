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

import org.apache.commons.monitoring.Role;
import org.apache.commons.monitoring.monitors.Monitor;
import org.apache.commons.monitoring.repositories.Repository;
import org.apache.commons.monitoring.stopwatches.StopWatch;

import java.lang.reflect.Method;

import static org.apache.commons.monitoring.counter.Unit.UNARY;

/**
 * A method interceptor that compute method invocation performances.
 * <p/>
 * Concrete implementation will adapt the method interception API to
 * this class requirement.
 *
 * @author <a href="mailto:nicolas@apache.org">Nicolas De Loof</a>
 */
public abstract class AbstractPerformanceInterceptor<T> {

    protected String category;

    protected MonitorNameExtractor monitorNameExtractor;

    public AbstractPerformanceInterceptor() {
        setMonitorNameExtractor(DefaultMonitorNameExtractor.INSTANCE);
    }

    /**
     * API neutral method invocation
     */
    protected Object doInvoke(final T invocation) throws Throwable {
        final String name = getMonitorName(invocation);
        if (name == null) {
            return proceed(invocation);
        }

        final Monitor monitor = Repository.INSTANCE.getMonitor(name, category);
        final StopWatch stopwatch = Repository.INSTANCE.start(monitor);
        Throwable error = null;
        try {
            return proceed(invocation);
        } catch (final Throwable t) {
            error = t;
            throw t;
        } finally {
            stopwatch.stop();
            beforeReturning(monitor, error, stopwatch.getElapsedTime());
        }
    }

    protected abstract Object proceed(T invocation) throws Throwable;

    protected abstract String getMonitorName(T invocation);

    /**
     * Compute the monitor name associated to this method invocation
     *
     * @param method method being invoked
     * @return monitor name. If <code>null</code>, nothing will be monitored
     */
    protected String getMonitorName(final Object instance, final Method method) {
        return monitorNameExtractor.getMonitorName(instance, method);
    }

    protected void beforeReturning(final Monitor monitor, final Throwable error, final long duration) {
        if (error != null) {
            monitor.getCounter(Role.FAILURES).add(duration);
        }
    }

    public void setCategory(final String category) {
        this.category = category;
    }

    public void setMonitorNameExtractor(final MonitorNameExtractor monitorNameExtractor) {
        this.monitorNameExtractor = monitorNameExtractor;
    }
}