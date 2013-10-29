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
package org.apache.sirona.web.servlet;

import org.apache.sirona.Role;
import org.apache.sirona.aop.AbstractPerformanceInterceptor;
import org.apache.sirona.configuration.Configuration;
import org.apache.sirona.counters.Counter;
import org.apache.sirona.repositories.Repository;
import org.apache.sirona.stopwatches.StopWatch;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.HttpURLConnection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class MonitoringFilter extends AbstractPerformanceInterceptor<MonitoringFilter.Invocation> implements Filter {
    public static final String MONITOR_STATUS = Configuration.CONFIG_PROPERTY_PREFIX + "web.monitored-status";

    private static final ConcurrentMap<Integer, Counter.Key> STATUS_KEYS = new ConcurrentHashMap<Integer, Counter.Key>();

    private boolean monitorStatus;

    @Override
    public void init(final FilterConfig filterConfig) throws ServletException {
        final String monStatus = filterConfig.getInitParameter(MONITOR_STATUS);
        monitorStatus = monStatus == null || "true".equalsIgnoreCase(monStatus);

        for (final Field f : HttpURLConnection.class.getDeclaredFields()) {
            final int modifiers = f.getModifiers();
            if (f.getName().startsWith("HTTP_")
                && Modifier.isStatic(modifiers) && Modifier.isPublic(modifiers) && Modifier.isFinal(modifiers)) {
                try {
                    final int status = (Integer) f.get(null);
                    STATUS_KEYS.put(status, statusKey((Integer) f.get(null)));
                } catch (final IllegalAccessException e) {
                    // no-op
                }
            }
        }
    }

    @Override
    public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain) throws IOException, ServletException {
        if (HttpServletRequest.class.isInstance(request)) {
            final HttpServletRequest httpRequest = HttpServletRequest.class.cast(request);
            final HttpServletResponse httpResponse = HttpServletResponse.class.cast(response);
            try {
                doInvoke(new Invocation(httpRequest, httpResponse, chain));
            } catch (final Throwable throwable) {
                if (IOException.class.isInstance(throwable)) {
                    throw IOException.class.cast(throwable);
                }
                if (ServletException.class.isInstance(throwable)) {
                    throw ServletException.class.cast(throwable);
                }
                throw new IOException(throwable);
            } finally {
                if (monitorStatus) {
                    final int status = httpResponse.getStatus();
                    Repository.INSTANCE.getCounter(statusKey(status)).add(1);
                }
            }
        } else {
            // Not an HTTP request...
            chain.doFilter(request, response);
        }
    }

    @Override
    protected Object proceed(final Invocation invocation) throws Throwable {
        invocation.proceed();
        return null;
    }

    @Override
    protected String getCounterName(final Invocation invocation) {
        return invocation.request.getRequestURI();
    }

    @Override
    protected Role getRole() {
        return Role.WEB;
    }

    @Override
    public void destroy() {
        // no-op
    }

    private static Counter.Key statusKey(final int status) {
        final Counter.Key key = STATUS_KEYS.get(status);
        if (key != null) {
            return key;
        }

        final Counter.Key newKey = new Counter.Key(Role.WEB, "HTTP-" + Integer.toString(status));
        final Counter.Key old = STATUS_KEYS.putIfAbsent(status, newKey);
        if (old != null) {
            return old;
        }
        return newKey;
    }

    protected static class Invocation {
        protected final HttpServletRequest request;
        protected final HttpServletResponse response;
        protected final FilterChain chain;

        public Invocation(final HttpServletRequest request, final HttpServletResponse response, final FilterChain chain) {
            this.request = request;
            this.response = response;
            this.chain = chain;
        }

        public void proceed() throws IOException, ServletException {
            chain.doFilter(request, response);
        }
    }
}