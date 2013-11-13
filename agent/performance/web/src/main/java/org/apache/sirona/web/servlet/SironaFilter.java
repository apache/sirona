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

import org.apache.sirona.SironaException;
import org.apache.sirona.Role;
import org.apache.sirona.aop.AbstractPerformanceInterceptor;
import org.apache.sirona.configuration.Configuration;
import org.apache.sirona.web.discovery.GaugeDiscoveryListener;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

public class SironaFilter extends AbstractPerformanceInterceptor<SironaFilter.Invocation> implements Filter {
    public static final String MONITOR_STATUS = Configuration.CONFIG_PROPERTY_PREFIX + "web.monitored-status";
    public static final String IGNORED_URLS = Configuration.CONFIG_PROPERTY_PREFIX + "web.ignored-urls";

    private String[] ignored = new String[0];
    private Map<Integer, StatusGauge> statusGauges = null;

    @Override
    public void init(final FilterConfig filterConfig) throws ServletException {
        final String ignoredUrls = filterConfig.getInitParameter(IGNORED_URLS);
        if (ignoredUrls != null) {
            ignored = ignoredUrls.split(",");
        }

        final String monStatus = filterConfig.getInitParameter(MONITOR_STATUS);
        if ((monStatus == null || "true".equalsIgnoreCase(monStatus))
                && filterConfig.getServletContext().getAttribute(GaugeDiscoveryListener.STATUS_GAUGES_ATTRIBUTE) == null) {
            throw new SironaException("To monitor status activate " + GaugeDiscoveryListener.class.getName());
        }

        statusGauges = (Map<Integer, StatusGauge>) filterConfig.getServletContext().getAttribute(GaugeDiscoveryListener.STATUS_GAUGES_ATTRIBUTE);
    }

    @Override
    public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain) throws IOException, ServletException {
        if (HttpServletRequest.class.isInstance(request)) {
            final HttpServletRequest httpRequest = HttpServletRequest.class.cast(request);

            final String uri = getRequestedUri(httpRequest);
            for (final String ignorable : ignored) {
                if (uri.startsWith(ignorable)) {
                    chain.doFilter(request, response);
                    return;
                }
            }

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
                if (statusGauges != null) {
                    final int status = httpResponse.getStatus();
                    final StatusGauge statusGauge = statusGauges.get(status);
                    if (statusGauge != null) {
                        statusGauge.incr();
                    }
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

    protected static String getRequestedUri(final HttpServletRequest request) {
        final String uri = request.getRequestURI();
        final String context = request.getContextPath();
        if (uri.length() <= context.length()) {
            return uri;
        }
        return uri.substring(context.length());
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