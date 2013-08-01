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
package org.apache.commons.monitoring.web.servlet;

import org.apache.commons.monitoring.counter.Role;
import org.apache.commons.monitoring.counter.Counter;
import org.apache.commons.monitoring.repositories.Repository;
import org.apache.commons.monitoring.stopwatches.StopWatch;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class MonitoringFilter implements Filter {
    @Override
    public void init(final FilterConfig filterConfig) throws ServletException {
        // no-op
    }

    @Override
    public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain) throws IOException, ServletException {
        if (HttpServletRequest.class.isInstance(request)) {
            final HttpServletRequest httpRequest = HttpServletRequest.class.cast(request);
            final HttpServletResponse httpResponse = HttpServletResponse.class.cast(response);
            doFilter(httpRequest, httpResponse, chain);
        } else {
            // Not an HTTP request...
            chain.doFilter(request, response);
        }
    }

    @Override
    public void destroy() {
        // no-op
    }

    protected void doFilter(final HttpServletRequest request, final HttpServletResponse response, final FilterChain chain) throws IOException, ServletException {
        final String uri = getRequestedUri(request);
        final StopWatch stopWatch = Repository.INSTANCE.start(Repository.INSTANCE.getCounter(new Counter.Key(Role.WEB, uri)));
        try {
            chain.doFilter(request, response);
        } finally {
            stopWatch.stop();
        }
    }

    protected String getRequestedUri(final HttpServletRequest request) {
        final String uri = request.getRequestURI();
        final String context = request.getContextPath();
        return uri.substring(context.length());
    }
}