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
package org.apache.commons.monitoring.reporting.web;

import org.apache.commons.monitoring.reporting.template.Templates;
import org.apache.commons.monitoring.reporting.web.handler.ClearHandler;
import org.apache.commons.monitoring.reporting.web.handler.FilteringHandler;
import org.apache.commons.monitoring.reporting.web.handler.Handler;
import org.apache.commons.monitoring.reporting.web.handler.HtmlHandler;
import org.apache.commons.monitoring.reporting.web.handler.Renderer;
import org.apache.commons.monitoring.reporting.web.handler.ReportHandler;
import org.apache.commons.monitoring.reporting.web.handler.ResetHandler;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MonitoringController implements Filter {
    private final Map<String, Handler> handlers = new HashMap<String, Handler>();
    private Handler defaultHandler;
    private String mapping;
    private ServletContext servletContext;

    @Override
    public void init(final FilterConfig config) throws ServletException {
        servletContext = config.getServletContext();
        initMapping(config);
        initHandlers();
        initTemplates();
    }

    private void initHandlers() {
        defaultHandler = new HtmlHandler("home.vm");
        handlers.put("/", defaultHandler);
        handlers.put("/home", defaultHandler);
        handlers.put("/report", new ReportHandler());
        handlers.put("/clear", new ClearHandler());
        handlers.put("/reset", new ResetHandler());
        handlers.put("/resources/css/monitoring.css", FilteringHandler.INSTANCE); // filtered to get the right base for pictures
    }

    private void initTemplates() {
        Templates.init(servletContext, mapping);
    }

    private void initMapping(FilterConfig config) {
        mapping = config.getInitParameter("monitoring-mapping");
        if (mapping == null) {
            mapping = "";
        } else if (!mapping.startsWith("/")) {
            mapping = "/" + mapping;
        }
        if (mapping.endsWith("/")) {
            mapping = mapping.substring(0, mapping.length() - 1);
        }
    }

    @Override
    public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain) throws IOException, ServletException {
        if (!HttpServletRequest.class.isInstance(request)) {
            chain.doFilter(request, response);
            return;
        }

        final HttpServletRequest httpRequest = HttpServletRequest.class.cast(request);

        final Handler handler = findHandler(httpRequest);
        final String path = httpRequest.getRequestURI().substring(httpRequest.getContextPath().length());
        if (handler == defaultHandler && servletContext.getResourceAsStream(path) != null) {
            chain.doFilter(request, response);
            return;
        }

        final Renderer renderer = handler.handle(httpRequest, HttpServletResponse.class.cast(response));
        if (renderer != null) {
            renderer.render(response.getWriter(), request.getParameterMap());
        }
    }

    private Handler findHandler(final HttpServletRequest request) {
        String path = request.getRequestURI().substring(request.getContextPath().length() + mapping.length());
        if (!path.startsWith("/")) {
            path = "/" + path;
        }

        final Handler handler = handlers.get(path);
        if (handler != null) {
            return handler;
        }

        return defaultHandler;
    }

    @Override
    public void destroy() {
        handlers.clear();
    }
}
