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

import org.apache.commons.monitoring.reporting.web.handler.FilteringEndpoints;
import org.apache.commons.monitoring.reporting.web.handler.HomeEndpoint;
import org.apache.commons.monitoring.reporting.web.handler.internal.EndpointInfo;
import org.apache.commons.monitoring.reporting.web.handler.internal.Invoker;
import org.apache.commons.monitoring.reporting.web.plugin.PluginRepository;
import org.apache.commons.monitoring.reporting.web.template.MapBuilder;
import org.apache.commons.monitoring.reporting.web.template.Templates;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MonitoringController implements Filter {
    private final Map<String, byte[]> cachedResources = new ConcurrentHashMap<String, byte[]>();
    private final Map<Pattern, Invoker> invokers = new HashMap<Pattern, Invoker>();
    private String mapping;
    private ClassLoader classloader;
    private Invoker defaultInvoker;

    @Override
    public void init(final FilterConfig config) throws ServletException {
        classloader = Thread.currentThread().getContextClassLoader();
        initMapping(config);
        Templates.init(config.getServletContext().getContextPath(), mapping);
        initHandlers();
    }

    private void initHandlers() {
        // home page
        invokers.putAll(EndpointInfo.build(HomeEndpoint.class, null, "").getInvokers());
        defaultInvoker = invokers.values().iterator().next();

        // filtered to get the right base for pictures
        invokers.putAll(EndpointInfo.build(FilteringEndpoints.class, null, "").getInvokers());

        // plugins
        for (final PluginRepository.PluginInfo plugin : PluginRepository.PLUGIN_INFO) {
            for (final Map.Entry<Pattern, Invoker> invoker : plugin.getInvokers().entrySet()) {
                invokers.put(invoker.getKey(), invoker.getValue());
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
        final HttpServletResponse httpResponse = HttpServletResponse.class.cast(response);

        final String baseUri = httpRequest.getContextPath() + mapping;
        request.setAttribute("baseUri", baseUri);

        final String requestURI = httpRequest.getRequestURI();
        String path = requestURI.substring(Math.max(baseUri.length() + 1, requestURI.length()));
        if (!path.startsWith("/")) {
            path = "/" + path;
        }

        // find the matching invoker
        Invoker invoker = defaultInvoker;
        Matcher matcher = null;
        for (final Map.Entry<Pattern, Invoker> entry : invokers.entrySet()) {
            matcher = entry.getKey().matcher(path);
            if (matcher.matches()) {
                invoker = entry.getValue();
                if (!entry.getKey().pattern().endsWith(".*")) {
                    break;
                }
            }
        }

        // resource, they are in the classloader and not in the webapp to ease the embedded case
        if (path.startsWith("/resources/")) {
            byte[] bytes = cachedResources.get(path);
            if (bytes == null) {
                final InputStream is;
                if (invoker != defaultInvoker) { // resource is filtered so filtering it before caching it
                    final StringWriter writer = new StringWriter();
                    final PrintWriter printWriter = new PrintWriter(writer);
                    invoker.invoke(httpRequest, HttpServletResponse.class.cast(Proxy.newProxyInstance(classloader, new Class<?>[] { HttpServletResponse.class }, new InvocationHandler() {
                        @Override
                        public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
                            if ("getWriter".equals(method.getName())) {
                                return printWriter;
                            }
                            return method.invoke(httpResponse, args);
                        }
                    })), null);
                    is = new ByteArrayInputStream(writer.toString().getBytes());
                } else {
                    is = classloader.getResourceAsStream(path.substring(1));
                }

                if (is != null) {
                    ByteArrayOutputStream baos = ByteArrayOutputStream.class.cast(request.getAttribute("resourceCache"));
                    if (baos == null) {
                        baos = new ByteArrayOutputStream();
                        int i;
                        while ((i = is.read()) != -1) {
                            baos.write(i);
                        }
                    }

                    bytes = baos.toByteArray();
                    cachedResources.put(path, bytes);
                }
            }
            if (bytes != null) {
                if (bytes.length == 0) {
                    httpResponse.setStatus(404);
                } else {
                    httpResponse.getOutputStream().write(bytes);
                }
                return;
            }
        }

        // delegate handling to the invoker if request is not a resource
        if (invoker == null) {
            error(response, null);
        } else {
            try {
                invoker.invoke(httpRequest, httpResponse, matcher);
            } catch (final Exception e) {
                error(response, e);
            }
        }
    }

    private void error(final ServletResponse response, final Exception e) throws IOException {
        final String exception;
        if (e != null) {
            final ByteArrayOutputStream err = new ByteArrayOutputStream();
            e.printStackTrace(new PrintStream(err));
            exception = new String(err.toByteArray());
        } else {
            exception = "No matcher found";
        }
        Templates.htmlRender(response.getWriter(), "error.vm", new MapBuilder<String, Object>().set("exception", exception).build());
    }

    @Override
    public void destroy() {
        invokers.clear();
    }
}
