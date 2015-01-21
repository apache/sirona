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

package org.apache.sirona.aop;

import org.apache.sirona.Role;
import org.apache.sirona.SironaException;
import org.apache.sirona.configuration.Configuration;
import org.apache.sirona.counters.Counter;
import org.apache.sirona.repositories.Repository;
import org.apache.sirona.stopwatches.StopWatch;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A method interceptor that compute method invocation performances.
 * <p/>
 * Concrete implementation will adapt the method interception API to
 * this class requirement.
 *
 */
public abstract class AbstractPerformanceInterceptor<T> implements Serializable {
    // static for performances reasons, all these values are read through getXXX so it is overridable
    private static final boolean ADAPTIVE = Configuration.is(Configuration.CONFIG_PROPERTY_PREFIX + "performance.adaptive", false);
    private static final long FORCED_ITERATION = Configuration.getInteger(Configuration.CONFIG_PROPERTY_PREFIX + "performance.forced-iteration", 0);
    private static final long THRESHOLD = duration(Configuration.getProperty(Configuration.CONFIG_PROPERTY_PREFIX + "performance.threshold", null));
    private static final ActivationContext ALWAYS_ACTIVE_CONTEXT = new ActivationContext(true, 0, 0);

    protected static final ConcurrentMap<Object, ActivationContext> CONTEXTS = new ConcurrentHashMap<Object, ActivationContext>();

    private static long duration(final String duration) {
        if (duration == null) {
            return 0;
        }
        final String[] parts = duration.split(" ");
        if (parts.length == 1) {
            return Long.parseLong(duration.trim());
        } else if (parts.length == 2) {
            return TimeUnit.valueOf(parts[2].trim().toUpperCase(Locale.ENGLISH)).toNanos(Long.parseLong(parts[0].trim()));
        }
        return 0;
    }

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

        final Context ctx = before(invocation, name);
        Throwable error = null;
        try {
            return proceed(invocation);
        } catch (final Throwable t) {
            error = t;
            throw t;
        } finally {
            if (error == null) {
                ctx.stop();
            } else {
                ctx.stopWithException(error);
            }
        }
    }

    protected Context before(final T invocation, final String name) {
        final ActivationContext context = doFindContext(invocation);

        try
        {
            final StopWatch stopwatch;
            if (context.shouldExecute()) {
                Repository repository =  Repository.INSTANCE;
                if (repository==null){
                    System.out.println("repository is null");
                }
                final Counter monitor = repository.getCounter(getKey(invocation, name));
                if ( monitor == null){
                    System.out.println("monitor is null");
                }
                stopwatch = Repository.INSTANCE.start(monitor);
            } else {
                stopwatch = null;
            }

            return newContext(invocation, context, stopwatch);
        }
        catch ( Exception e )
        {
            //e.printStackTrace();
            // ignore and return a fake context can happen on start when intercepting some classes
            // and all agent classes not really loaded
            return newContext( invocation, context, new StopWatch()
            {
                @Override
                public long getElapsedTime()
                {
                    return 0;
                }

                @Override
                public StopWatch stop()
                {
                    return this;
                }
            }  );
        }
    }

    protected Context newContext(final T invocation, final ActivationContext context, final StopWatch stopwatch) {
        return new Context(context, stopwatch);
    }

    protected Counter.Key getKey(final T invocation, final String name) {
        return new Counter.Key(getRole(), name);
    }

    protected boolean isAdaptive() {
        return ADAPTIVE;
    }

    protected Object extractContextKey(final T invocation) {
        return null;
    }

    protected ActivationContext getOrCreateContext(final Object m) {
        final ActivationContext c = CONTEXTS.get(m);
        if (c == null) {
            final String counterName;
            if (SerializableMethod.class.isInstance(m)) {
                counterName = getCounterName(null, SerializableMethod.class.cast(m).method());
            } else {
                counterName = m.toString();
            }
            return putAndGetActivationContext(m, new ActivationContext(true, counterName));
        }
        return c;
    }

    protected ActivationContext putAndGetActivationContext(Object m, ActivationContext newCtx) {
        final ActivationContext old = CONTEXTS.putIfAbsent(m, newCtx);
        if (old != null) {
            newCtx = old;
        }
        return newCtx;
    }

    protected ActivationContext doFindContext(final T invocation) {
        if (!isAdaptive()) {
            return ALWAYS_ACTIVE_CONTEXT;
        }

        final Object m = extractContextKey(invocation);
        if (m != null) {
            return getOrCreateContext(m);
        }

        return ALWAYS_ACTIVE_CONTEXT;
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

    /**
     * The handler for cases where interception is not possible and you need to pass the "before"object to be able to monitor.
     */
    public static class Context {
        private static final int MAX_LENGTH = Configuration.getInteger(Configuration.CONFIG_PROPERTY_PREFIX + "performance.exception.max-length", 100);

        protected final ActivationContext activationContext;
        protected final StopWatch stopWatch;

        public Context(final ActivationContext activationContext, final StopWatch stopWatch) {
            this.activationContext = activationContext;
            this.stopWatch = stopWatch;
        }

        public void stop() {
            if (stopWatch != null) {
                final long elapsedTime = stopWatch.stop().getElapsedTime();
                activationContext.elapsedTime(elapsedTime);
            }
        }

        public void stopWithException(final Throwable error) {
            if (stopWatch != null) {
                stopWatch.stop();

                final long elapsedTime = stopWatch.getElapsedTime();

                if (error != null) {
                    /*
                    final ByteArrayOutputStream writer = new ByteArrayOutputStream();
                    error.printStackTrace(new PrintStream(writer));
                    final String toString = writer.toString();
                    */
                    Repository.INSTANCE.getCounter(
                            new Counter.Key(Role.FAILURES, error.getClass().getName() + ":" + (error.getMessage() != null ? error.getMessage() : ""))).add(elapsedTime);
                }

                activationContext.elapsedTime(elapsedTime);
            }
        }
    }

    protected static class SerializableMethod implements Serializable {
        protected final String clazz;
        protected final String method;
        protected transient Method realMethod;
        protected final int hashCode;

        public SerializableMethod(final String clazz, final String method, final Method reflectMethod) {
            this.clazz = clazz;
            this.method = method;
            this.realMethod = reflectMethod;
            this.hashCode = reflectMethod.hashCode();
        }

        public SerializableMethod(final Method m) {
            this(m.getDeclaringClass().getName(), m.getName(), m);
        }

        public Method method() {
            if (realMethod == null) { // try to find it
                try {
                    Class<?> declaring = Thread.currentThread().getContextClassLoader().loadClass(clazz);
                    while (declaring != null) {
                        for (final Method m : declaring.getDeclaredMethods()) {
                            if (m.getName().equals(method)) {
                                realMethod = m;
                                return realMethod;
                            }
                        }
                        declaring = declaring.getSuperclass();
                    }
                } catch (final ClassNotFoundException e) {
                    throw new SironaException(e.getMessage(),e);
                }
            }
            return realMethod;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            final SerializableMethod that = SerializableMethod.class.cast(o);
            if (method != null && that.method != null) {
                return method.equals(that.method);
            }
            return hashCode == that.hashCode;
        }

        @Override
        public int hashCode() {
            return hashCode;
        }
    }

    /**
     * This class contains the activation/deactivation logic.
     */
    protected static class ActivationContext implements Serializable {
        protected final long forceIteration;
        protected final long threshold;
        protected final boolean thresholdActive;

        protected volatile boolean active = true;
        protected volatile AtomicInteger iteration = new AtomicInteger(0);

        public ActivationContext(final boolean active, final long th, final long it) {
            this.active = active;

            if (it >= 0) {
                forceIteration = it;
            } else {
                forceIteration = FORCED_ITERATION;
            }

            if (th >= 0) {
                threshold = th;
            } else {
                threshold = THRESHOLD;
            }

            this.thresholdActive = this.threshold > 0;
        }

        public ActivationContext(final boolean active, final String name) {
            this(active,
                duration(Configuration.getProperty(Configuration.CONFIG_PROPERTY_PREFIX + "performance." + name + ".threshold", null)),
                Configuration.getInteger(Configuration.CONFIG_PROPERTY_PREFIX + "performance." + name + ".forced-iteration", -1));
        }

        public boolean isForcedIteration() {
            return iteration.incrementAndGet() > forceIteration;
        }

        protected long getThreshold() {
            return threshold;
        }

        protected boolean isThresholdActive() {
            return thresholdActive;
        }

        public boolean isActive() {
            return active;
        }

        public void reset() {
            active = false;
            iteration.set(0);
        }

        public boolean shouldExecute() {
            return isActive() || isForcedIteration();
        }

        public void elapsedTime(final long elapsedTime) {
            if (isThresholdActive() && elapsedTime < getThreshold()) {
                reset();
            }
        }
    }
}