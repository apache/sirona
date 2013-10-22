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
import org.apache.commons.monitoring.counters.Counter;
import org.apache.commons.monitoring.repositories.Repository;
import org.apache.commons.monitoring.stopwatches.StopWatch;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;

/**
 * A method interceptor that compute method invocation performances.
 * <p/>
 * Concrete implementation will adapt the method interception API to
 * this class requirement.
 *
 * @author <a href="mailto:nicolas@apache.org">Nicolas De Loof</a>
 */
public abstract class AbstractPerformanceInterceptor<T> {

    protected MonitorNameExtractor monitorNameExtractor;

    public AbstractPerformanceInterceptor() {
        setMonitorNameExtractor(DefaultMonitorNameExtractor.INSTANCE);
    }

    /**
     * API neutral method invocation
     */
    protected Object doInvoke(final T invocation) throws Throwable {
        final String name = getCounterName(invocation);
        if (name == null) {
            return proceed(invocation);
        }

        final Counter monitor = Repository.INSTANCE.getCounter(new Counter.Key(getRole(), name));
        final StopWatch stopwatch = Repository.INSTANCE.start(monitor);
        Throwable error = null;
        try {
            return proceed(invocation);
        } catch (final Throwable t) {
            error = t;
            throw t;
        } finally {
            stopwatch.stop();
            if (error != null) {
                final ByteArrayOutputStream writer = new ByteArrayOutputStream();
                error.printStackTrace(new PrintStream(writer));
                Repository.INSTANCE.getCounter(new Counter.Key(Role.FAILURES, writer.toString())).add(stopwatch.getElapsedTime());
            }
        }
    }

    protected Role getRole() {
        return Role.PERFORMANCES;
    }

    protected abstract Object proceed(T invocation) throws Throwable;

    protected abstract String getCounterName(T invocation);

    /**
     * Compute the counter name associated to this method invocation
     *
     * @param method method being invoked
     * @return counter name. If <code>null</code>, nothing will be monitored
     */
    protected String getCounterName(final Object instance, final Method method) {
        return monitorNameExtractor.getMonitorName(instance, method);
    }

    public void setMonitorNameExtractor(final MonitorNameExtractor monitorNameExtractor) {
        this.monitorNameExtractor = monitorNameExtractor;
    }
}