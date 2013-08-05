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
package org.apache.commons.monitoring.reporting.web.handler.internal;

import org.apache.commons.monitoring.MonitoringException;
import org.apache.commons.monitoring.reporting.web.handler.api.Template;
import org.apache.commons.monitoring.reporting.web.handler.api.TemplateHelper;
import org.apache.commons.monitoring.reporting.web.template.MapBuilder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Matcher;

public class Invoker {
    private final Object instance;
    private final Method method;
    private final String name;
    private final List<ParameterExtractor<?>> parameters = new CopyOnWriteArrayList<ParameterExtractor<?>>();

    public Invoker(final Object instance, final Method method, final String name) {
        this.instance = instance;
        this.method = method;
        this.name = name;
    }

    public void invoke(final HttpServletRequest request, final HttpServletResponse response, final Matcher matcher) {
        final Object[] params = new Object[parameters.size()];
        for (int i = 0; i < params.length; i++) {
            params[i] = parameters.get(i).extract(request, response, matcher);
        }
        try {
            final Object result = method.invoke(instance, params);
            if (Template.class.isInstance(result)) {
                final Template template = Template.class.cast(result);
                final TemplateHelper helper = new TemplateHelperExtractor(name).extract(request, response, matcher);
                if (template.isHtml()) {
                    helper.renderHtml(template.getTemplate(), template.getUserParams());
                } else {
                    helper.renderPlain(template.getTemplate(), template.getUserParams());
                }
            } else if (result != null) {
                response.getWriter().write(result.toString());
            }
        } catch (final InvocationTargetException e) {
            throw new MonitoringException(e.getCause());
        } catch (final Exception e) {
            throw new MonitoringException(e);
        }
    }

    public void addRequestParameter() {
        parameters.add(new RequestExtractor());
    }

    public void addResponseParameter() {
        parameters.add(new ResponseExtractor());
    }

    public void addTemplateHelper(final String plugin) {
        parameters.add(new TemplateHelperExtractor(plugin));
    }

    public void addSegmentParameter(final Class<?> clazz, final int partIdx) {
        if (String.class.equals(clazz)) {
            parameters.add(new StringSegmentExtractor(partIdx));
        } else if (Long.TYPE.equals(clazz)) {
            parameters.add(new LongSegmentExtractor(partIdx));
        } else if (Integer.TYPE.equals(clazz)) {
            parameters.add(new IntSegmentExtractor(partIdx));
        } else if (String[].class.equals(clazz)) {
            parameters.add(new StringArraySegmentExtractor(partIdx));
        } else {
            throw new IllegalArgumentException(clazz.getName() + " not handled");
        }
    }

    protected static interface ParameterExtractor<T> {
        T extract(HttpServletRequest request, HttpServletResponse response, Matcher matcher);
    }

    protected static class TemplateHelperExtractor implements ParameterExtractor<TemplateHelper> {
        private final String plugin;

        public TemplateHelperExtractor(final String plugin) {
            this.plugin = plugin;
        }

        @Override
        public TemplateHelper extract(final HttpServletRequest request, final HttpServletResponse response, final Matcher matcher) {
            try {
                return new TemplateHelper(response.getWriter(), new MapBuilder<String, Object>().set("templateId", plugin).build());
            } catch (final IOException e) {
                throw new MonitoringException(e);
            }
        }
    }

    protected static class RequestExtractor implements ParameterExtractor<HttpServletRequest> {
        @Override
        public HttpServletRequest extract(final HttpServletRequest request, final HttpServletResponse response, final Matcher matcher) {
            return request;
        }
    }

    protected static class ResponseExtractor implements ParameterExtractor<HttpServletResponse> {
        @Override
        public HttpServletResponse extract(final HttpServletRequest request, final HttpServletResponse response, final Matcher matcher) {
            return response;
        }
    }

    protected static class StringSegmentExtractor implements ParameterExtractor<String> {
        private final int index;

        public StringSegmentExtractor(final int index) {
            this.index = index;
        }

        @Override
        public String extract(final HttpServletRequest request, final HttpServletResponse response, final Matcher matcher) {
            return matcher.group(index);
        }
    }

    protected static class StringArraySegmentExtractor implements ParameterExtractor<String[]> {
        private final int index;

        public StringArraySegmentExtractor(final int index) {
            this.index = index;
        }

        @Override
        public String[] extract(final HttpServletRequest request, final HttpServletResponse response, final Matcher matcher) {
            return matcher.group(index).split("/");
        }
    }

    protected static class LongSegmentExtractor implements ParameterExtractor<Long> {
        private final int index;

        public LongSegmentExtractor(final int index) {
            this.index = index;
        }

        @Override
        public Long extract(final HttpServletRequest request, final HttpServletResponse response, final Matcher matcher) {
            return Long.parseLong(matcher.group(index));
        }
    }

    protected static class IntSegmentExtractor implements ParameterExtractor<Integer> {
        private final int index;

        public IntSegmentExtractor(final int index) {
            this.index = index;
        }

        @Override
        public Integer extract(final HttpServletRequest request, final HttpServletResponse response, final Matcher matcher) {
            return Integer.parseInt(matcher.group(index));
        }
    }
}
