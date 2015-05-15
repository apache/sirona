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
package org.apache.sirona.reporting.web;

import org.apache.sirona.configuration.ioc.IoCs;
import org.apache.sirona.reporting.web.handler.FilteringEndpoints;
import org.apache.sirona.reporting.web.handler.HomeEndpoint;
import org.apache.sirona.reporting.web.handler.internal.EndpointInfo;
import org.apache.sirona.reporting.web.handler.internal.Invoker;
import org.apache.sirona.reporting.web.plugin.PluginRepository;
import org.apache.sirona.reporting.web.plugin.api.MapBuilder;
import org.apache.sirona.reporting.web.template.Templates;
import org.apache.sirona.repositories.Repository;

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
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class SironaController implements Filter {
    public static final String CONTENT_TYPE = "Content-Type";
    private final Map<String, byte[]> cachedResources = new ConcurrentHashMap<String, byte[]>();
    private final Map<Pattern, Invoker> invokers = new HashMap<Pattern, Invoker>();
    private String mapping = null;
    private ClassLoader classloader;
    private Invoker defaultInvoker;

    @Override
    public void init(final FilterConfig config) throws ServletException {
        IoCs.findOrCreateInstance(Repository.class); // ensure datastore are loaded

        classloader = Thread.currentThread().getContextClassLoader();
        initMapping(config.getInitParameter("monitoring-mapping"));
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

    public void setMapping(final String mapping) {
        initMapping(mapping);
    }

    private void initMapping(final String value) {
        if (mapping != null) { // already done, surely the initializer
            return;
        }

        if (value == null) {
            mapping = "";
        } else if (!value.startsWith("/")) {
            mapping = "/" + value;
        } else {
            mapping = value;
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
        final String path = buildMatchablePath(httpRequest, baseUri, requestURI, true);
        final String pathWithoutParams = buildMatchablePath(httpRequest, baseUri, requestURI, false);

        // find the matching invoker
        Invoker invoker = defaultInvoker;
        Matcher matcher = null;

        for (final Map.Entry<Pattern, Invoker> entry : invokers.entrySet()) {
            final Pattern pattern = entry.getKey();
            if ((matcher = pattern.matcher(path)).matches()) {
                invoker = entry.getValue();
                if (!entry.getKey().pattern().endsWith(".*")) {
                    break;
                }
            } else if ((matcher = pattern.matcher(pathWithoutParams)).matches()) {
                invoker = entry.getValue(); // continue since that's a not perfect matching
            }
        }

        // handle Content-Type, we could use a map but this is more efficient ATM and can still be overriden
        boolean skipFiltering = false;
        if (requestURI.endsWith(".css")) {
            httpResponse.setHeader(CONTENT_TYPE, "text/css");
        } else if (requestURI.endsWith(".js")) {
            httpResponse.setHeader(CONTENT_TYPE, "application/javascript");
        } else if (requestURI.endsWith(".png")) {
            httpResponse.setHeader(CONTENT_TYPE, "image/png");
            skipFiltering = true;
        } else if (requestURI.endsWith(".gif")) {
            httpResponse.setHeader(CONTENT_TYPE, "image/gif");
            skipFiltering = true;
        } else if (requestURI.endsWith(".jpg")) {
            httpResponse.setHeader(CONTENT_TYPE, "image/jpeg");
            skipFiltering = true;
        } else if (requestURI.endsWith(".svg")) {
            httpResponse.setHeader(CONTENT_TYPE, "image/svg+xml");
            skipFiltering = true;
        } else if (requestURI.endsWith(".eot")) {
            httpResponse.setHeader(CONTENT_TYPE, "application/vnd.ms-fontobject");
            skipFiltering = true;
        } else if (requestURI.endsWith(".woff")) {
            httpResponse.setHeader(CONTENT_TYPE, "application/font-woff");
            skipFiltering = true;
        } else if (requestURI.endsWith(".ttf") || requestURI.endsWith(".itf")) {
            httpResponse.setHeader(CONTENT_TYPE, "application/octet-stream");
            skipFiltering = true;
        } else if (mapping.isEmpty() && path.startsWith("/restServices/")) {
            chain.doFilter(request, response);
            return;
        }

        // resource, they are in the classloader and not in the webapp to ease the embedded case
        if (pathWithoutParams.startsWith("/resources/")) {
            byte[] bytes = cachedResources.get(pathWithoutParams);
            if (bytes == null) {
                final InputStream is;
                if (!skipFiltering && invoker != defaultInvoker) { // resource is filtered so filtering it before caching it
                    final StringWriter writer = new StringWriter();
                    final PrintWriter printWriter = new PrintWriter(writer);
                    invoker.invoke(httpRequest, HttpServletResponse.class.cast(Proxy.newProxyInstance(classloader, new Class<?>[]{HttpServletResponse.class}, new InvocationHandler() {
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
                    is = classloader.getResourceAsStream(pathWithoutParams.substring(1));
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
                    cachedResources.put(pathWithoutParams, bytes);
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

    private static String buildMatchablePath(final HttpServletRequest httpRequest, final String baseUri, final String requestURI, final boolean withParams) {
        String path = requestURI.substring(Math.min(baseUri.length() + 1, requestURI.length()));
        if (!path.startsWith("/")) {
            path = "/" + path;
        }

        if (!withParams) {
            return path;
        }

        // sort keys to be able to match it deterministicly
        final Map<String, String[]> params = new TreeMap<String, String[]>(httpRequest.getParameterMap());
        boolean first = true;
        for (final Map.Entry<String, String[]> param : params.entrySet()) {
            final String[] value = param.getValue();
            if (value != null && value.length >= 1) {
                if (first) {
                    path += "?";
                    first = false;
                } else {
                    path += "&";
                }
                path += param.getKey() + "=" + value[0];
            }
        }
        return path;
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
