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

import org.apache.commons.monitoring.reporting.web.handler.FilteringHandler;
import org.apache.commons.monitoring.reporting.web.handler.Handler;
import org.apache.commons.monitoring.reporting.web.handler.HomeHandler;
import org.apache.commons.monitoring.reporting.web.handler.Renderer;
import org.apache.commons.monitoring.reporting.web.plugin.PluginRepository;
import org.apache.commons.monitoring.reporting.web.template.MapBuilder;
import org.apache.commons.monitoring.reporting.web.template.Templates;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

public class MonitoringController implements Filter {
    private final Map<String, byte[]> cachedResources = new HashMap<String, byte[]>();
    private final Map<String, Handler> handlers = new HashMap<String, Handler>();
    private Handler defaultHandler;
    private String mapping;
    private ClassLoader classloader;

    @Override
    public void init(final FilterConfig config) throws ServletException {
        classloader = Thread.currentThread().getContextClassLoader();
        initMapping(config);
        initHandlers();
        Templates.init(config.getServletContext().getContextPath(), mapping);
    }

    private void initHandlers() {
        defaultHandler = new HomeHandler();

        handlers.put("/", defaultHandler);
        handlers.put("/home", defaultHandler);
        handlers.put("/resources/css/monitoring.css", FilteringHandler.INSTANCE); // filtered to get the right base for pictures

        for (final PluginRepository.PluginInfo plugin : PluginRepository.PLUGIN_INFO) {
            if (plugin.getHandler() != null && plugin.getUrl() != null) {
                handlers.put("/" + plugin.getUrl(), plugin.getHandler());
            }
        }
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

        String path = httpRequest.getRequestURI().substring(httpRequest.getContextPath().length() + mapping.length());
        if (!path.startsWith("/")) {
            path = "/" + path;
        }

        final Handler handler = findHandler(path);
        if (handler == defaultHandler && !"/".equals(path)){ // resource, they are in the classloader and not in the webapp for embedded case
            byte[] bytes = cachedResources.get(path);
            if (bytes == null) {
                final InputStream is = classloader.getResourceAsStream(path.substring(1));
                if (is != null) {
                    final ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    int i;
                    while ((i = is.read()) != -1) {
                        baos.write(i);
                    }

                    bytes = baos.toByteArray();
                    cachedResources.put(path, bytes);
                }
            }
            if (bytes != null) {
                response.getOutputStream().write(bytes);
                return;
            }
        }

        try {
            final Renderer renderer = handler.handle(httpRequest, HttpServletResponse.class.cast(response), path);
            if (renderer != null) {
                renderer.render(response.getWriter(), request.getParameterMap());
            }
        } catch (final Exception e) {
            final ByteArrayOutputStream err = new ByteArrayOutputStream();
            e.printStackTrace(new PrintStream(err));
            Templates.htmlRender(response.getWriter(), "error.vm", new MapBuilder<String, Object>().set("exception", new String(err.toByteArray())).build());
        }
    }

    private Handler findHandler(final String path) {
        final Handler handler = handlers.get(path);
        if (handler != null) {
            return handler;
        }

        for (final String mapping : handlers.keySet()) {
            if (mapping.endsWith("/*") && path.startsWith(mapping.substring(0, mapping.length() - "/*".length()))) {
                return handlers.get(mapping);
            }
            if (mapping.endsWith("*") && path.startsWith(mapping.substring(0, mapping.length() - "*".length()))) {
                return handlers.get(mapping);
            }
        }

        return defaultHandler;
    }

    @Override
    public void destroy() {
        handlers.clear();
    }
}
