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
package org.apache.commons.monitoring.reporting.web.plugin.jmx;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.monitoring.MonitoringException;
import org.apache.commons.monitoring.reporting.web.handler.HandlerRendererAdapter;
import org.apache.commons.monitoring.reporting.web.handler.Renderer;
import org.apache.commons.monitoring.reporting.web.template.MapBuilder;
import org.apache.commons.monitoring.reporting.web.template.Templates;

import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.TabularData;
import java.io.PrintWriter;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Array;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class JMXHandler extends HandlerRendererAdapter {
    private static final MBeanServer SERVER = ManagementFactory.getPlatformMBeanServer();

    @Override
    protected Renderer rendererFor(final String path) {
        if ("/jmx".endsWith(path) || "/jmx/".equals(path)) {
            return this;
        }

        try {
            return new MBeanRenderer(new ObjectName(new String(Base64.decodeBase64(path.substring("/jmx/".length())))));
        } catch (final MalformedObjectNameException e) {
            throw new MonitoringException(e);
        }
    }

    @Override
    protected String getTemplate() {
        return "jmx/main.vm";
    }

    @Override
    protected Map<String, ?> getVariables() {
        return new MapBuilder<String, Object>().set("jmxTree", buildJmxTree()).build();
    }

    private static JMXNode buildJmxTree() {
        final JMXNode root = new JMXNode("/");

        for (final ObjectInstance instance : SERVER.queryMBeans(null, null)) {
            final ObjectName objectName = instance.getObjectName();
            JMXNode.addNode(root, objectName.getDomain(), objectName.getKeyPropertyListString());
        }

        return root;
    }

    private static class MBeanRenderer implements Renderer {
        private final ObjectName name;

        private MBeanRenderer(final ObjectName objectName) {
            this.name = objectName;
        }

        @Override
        public void render(final PrintWriter writer, final Map<String, ?> params) {
            try {
                final MBeanInfo info = SERVER.getMBeanInfo(name);
                Templates.render(writer, "templates/jmx/mbean.vm",
                    new MapBuilder<String, Object>()
                        .set("objectname", name.toString())
                        .set("classname", info.getClassName())
                        .set("description", info.getDescription())
                        .set("attributes", attributes(info))
                        .build());
            } catch (final Exception e) {
                throw new MonitoringException(e);
            }
        }

        private Collection<MBeanAttribute> attributes(final MBeanInfo info) {
            final Collection<MBeanAttribute> list = new LinkedList<MBeanAttribute>();
            for (final MBeanAttributeInfo attribute : info.getAttributes()) {
                Object value;
                try {
                    value = SERVER.getAttribute(name, attribute.getName());
                } catch (final Exception e) {
                    value = "<div class=\"alert-error\">" + e.getMessage() + "</div>";
                }
                list.add(new MBeanAttribute(attribute.getName(), attribute.getType(), attribute.getDescription(), value(value)));
            }
            return list;
        }

        private static String value(final Object value) {
            try {
                if (value == null) {
                    return "";
                }

                if (value.getClass().isArray()) {
                    final int length = Array.getLength(value);
                    if (length == 0) {
                        return "";
                    }

                    final StringBuilder builder = new StringBuilder().append("<ul>");
                    for (int i = 0; i < length; i++) {
                        builder.append("<li>").append(value(Array.get(value, i))).append("</li>");
                    }
                    builder.append("</ul>");
                    return builder.toString();
                }

                if (Collection.class.isInstance(value)) {
                    final StringBuilder builder = new StringBuilder().append("<ul>");
                    for (final Object o : Collection.class.cast(value)) {
                        builder.append("<li>").append(value(o)).append("</li>");
                    }
                    builder.append("</ul>");
                    return builder.toString();
                }

                if (TabularData.class.isInstance(value)) {
                    final TabularData td = TabularData.class.cast(value);
                    final List<String> keys = td.getTabularType().getIndexNames();
                    final int number = keys.size();

                    final StringBuilder builder = new StringBuilder().append("<table class=\"table table-condensed\">");
                    for (final Object type : td.keySet()) {
                        final List<?> values = (List<?>) type;
                        for (int i = 0; i < number; i++) {
                            builder.append("<tr>")
                                    .append("<td>").append(value(keys.get(i))).append("</td>")
                                    .append("<td>").append(value(values.get(i))).append("</td>")
                                .append("</tr>");
                        }

                    }
                    builder.append("</table>");

                    return builder.toString();
                }

                if (CompositeData.class.isInstance(value)) {
                    final CompositeData cd = CompositeData.class.cast(value);
                    final Set<String> keys = cd.getCompositeType().keySet();

                    final StringBuilder builder = new StringBuilder().append("<table class=\"table table-condensed\">");
                    for (final String type : keys) {
                        builder.append("<tr><td>").append(type).append("</td><td>").append(value(cd.get(type))).append("</td></tr>");
                    }
                    builder.append("</table>");

                    return builder.toString();

                }

                if (Map.class.isInstance(value)) {
                    final Map<?, ?> map = Map.class.cast(value);

                    final StringBuilder builder = new StringBuilder().append("<table class=\"table table-condensed\">");
                    for (final Map.Entry<?, ?> entry : map.entrySet()) {
                        builder.append("<tr><tr>").append(value(entry.getKey())).append("</td><td>").append(value(entry.getValue())).append("</td></tr>");
                    }
                    builder.append("</table>");

                    return builder.toString();

                }

                return value.toString();
            } catch (final Exception e) {
                throw new MonitoringException(e);
            }
        }
    }

    public static class MBeanAttribute {
        private final String name;
        private final String type;
        private final String description;
        private final String value;

        public MBeanAttribute(final String name, final String type, final String description, final String value) {
            this.name = name;
            this.type = type;
            this.description = description;
            this.value = value;
        }

        public String getName() {
            return name;
        }

        public String getType() {
            return type;
        }

        public String getDescription() {
            return description;
        }

        public String getValue() {
            return value;
        }
    }
}
