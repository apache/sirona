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
import org.apache.commons.monitoring.util.ClassLoaders;

import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.TabularData;
import java.io.PrintWriter;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class JMXHandler extends HandlerRendererAdapter {
    private static final MBeanServer SERVER = ManagementFactory.getPlatformMBeanServer();

    private static final Map<String, Class<?>> WRAPPERS = new HashMap<String, Class<?>>();

    static {
        for (final Class<?> c : Arrays.<Class<?>>asList(Byte.class, Short.class, Integer.class, Long.class, Float.class, Double.class, Character.class, Boolean.class )) {
            try {
                final Field f = c.getField("TYPE");
                Class<?> p = (Class<?>) f.get(null);
                WRAPPERS.put(p.getName(), c);
            } catch (Exception e) {
                throw new AssertionError(e);
            }
        }
    }


    @Override
    protected Renderer rendererFor(final String path) {
        if ("/jmx".endsWith(path) || "/jmx/".equals(path)) {
            return this;
        }

        String subPath = path.substring("/jmx/".length());
        if (subPath.startsWith("operation/")) {
            subPath = subPath.substring("operation/".length());
            final String[] parts = subPath.split("/");

            final Collection<String> params = new ArrayList<String>(parts.length - 2);
            { // remove object name and operation name to keep only parameters
                params.addAll(Arrays.asList(parts));
                final Iterator<String> it = params.iterator();
                it.next(); it.remove();
                it.next(); it.remove();
            }

            try {
                return new InvokeRenderer(new ObjectName(new String(Base64.decodeBase64(parts[0]))), parts[1], params.toArray(new String[params.size()]));
            } catch (final MalformedObjectNameException e) {
                throw new MonitoringException(e);
            }
        }

        try {
            return new MBeanRenderer(new ObjectName(new String(Base64.decodeBase64(subPath))));
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

    private static class InvokeRenderer implements Renderer {
        private final ObjectName name;
        private final String operation;
        private final String[] params;

        public InvokeRenderer(final ObjectName objectName, final String method, final String[] parameters) {
            name = objectName;
            operation = method;
            params = parameters;
        }

        @Override
        public void render(final PrintWriter writer, final Map<String, ?> ignored) {
            try {
                final MBeanInfo info = SERVER.getMBeanInfo(name);
                for (final MBeanOperationInfo op : info.getOperations()) {
                    if (op.getName().equals(operation)) {
                        final MBeanParameterInfo[] signature = op.getSignature();
                        final String[] sign = new String[signature.length];
                        for (int i = 0; i < sign.length; i++) {
                            sign[i] = signature[i].getType();
                        }
                        final Object result = SERVER.invoke(name, operation, convertParams(signature, params), sign);
                        writer.write("<div>Method was invoked and returned:</div>" + value(result));
                        return;
                    }
                }
            } catch (final Exception e) {
                writer.write("<div class=\"alert alert-error\">\n" +
                    "\n" + e.getMessage() + "\n" +
                    "</div>");
                return;
            }

            writer.write("<div class=\"alert alert-error\">Operation" + operation + " not found.</div>");
        }

        private Object[] convertParams(final MBeanParameterInfo[] signature, final String[] params) {
            if (params == null) {
                return null;
            }

            final Object[] convertedParams = new Object[signature.length];
            for (int i = 0; i < signature.length; i++) {
                if (i < params.length) {
                    convertedParams[i] = convert(signature[i].getType(), params[i]);
                } else {
                    convertedParams[i] = null;
                }
            }
            return convertedParams;
        }

        public static Object convert(final String type, final String value) {
            try {
                if (WRAPPERS.containsKey(type)) {
                    if (type.equals(Character.TYPE.getName())) {
                        return value.charAt(0);
                    } else {
                        return tryStringConstructor(type, value);
                    }
                }

                if (type.equals(Character.class.getName())) {
                    return value.charAt(0);
                }

                if (Number.class.isAssignableFrom(ClassLoaders.current().loadClass(type))) {
                    return toNumber(value);
                }

                if (value == null || value.equals("null")) {
                    return null;
                }

                return tryStringConstructor(type, value);
            } catch (final Exception e) {
                throw new MonitoringException(e);
            }
        }

        private static Number toNumber(final String value) throws NumberFormatException {
            // first the user can force the conversion
            final char lastChar = Character.toLowerCase(value.charAt(value.length() - 1));
            if (lastChar == 'd') {
                return Double.valueOf(value.substring(0, value.length() - 1));
            }
            if (lastChar == 'l') {
                return Long.valueOf(value.substring(0, value.length() - 1));
            }
            if (lastChar == 'f') {
                return Float.valueOf(value.substring(0, value.length() - 1));
            }

            // try all conversions in cascade until it works
            for (final Class<?> clazz : new Class<?>[] { Integer.class, Long.class, Double.class }) {
                try {
                    return Number.class.cast(clazz.getMethod("valueOf").invoke(null, value));
                } catch (final Exception e) {
                    // no-op
                }
            }

            throw new MonitoringException(value + " is not a number");
        }

        private static Object tryStringConstructor(String type, final String value) throws Exception {
            return ClassLoaders.current().loadClass(type).getConstructor(String.class).newInstance(value);
        }
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
                        .set("objectnameHash", Base64.encodeBase64String(name.toString().getBytes()))
                        .set("classname", info.getClassName())
                        .set("description", value(info.getDescription()))
                        .set("attributes", attributes(info))
                        .set("operations", operations(info))
                        .build());
            } catch (final Exception e) {
                throw new MonitoringException(e);
            }
        }

        private Collection<MBeanOperation> operations(final MBeanInfo info) {
            final Collection<MBeanOperation> operations = new LinkedList<MBeanOperation>();
            for (final MBeanOperationInfo operationInfo : info.getOperations()) {
                final MBeanOperation mBeanOperation = new MBeanOperation(operationInfo.getName(), operationInfo.getReturnType());
                for (final MBeanParameterInfo param : operationInfo.getSignature()) {
                    mBeanOperation.getParameters().add(new MBeanParameter(param.getName(), param.getType()));
                }
                operations.add(mBeanOperation);
            }
            return operations;
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

    public static class MBeanAttribute {
        private final String name;
        private final String type;
        private final String description;
        private final String value;

        public MBeanAttribute(final String name, final String type, final String description, final String value) {
            this.name = name;
            this.type = type;
            this.value = value;
            if (description != null) {
                this.description = description;
            } else {
                this.description = "No description";
            }
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

    public static class MBeanOperation {
        private final String name;
        private final String returnType;
        private final Collection<MBeanParameter> parameters = new LinkedList<MBeanParameter>();

        public MBeanOperation(final String name, final String returnType) {
            this.name = name;
            this.returnType = returnType;
        }

        public String getName() {
            return name;
        }

        public String getReturnType() {
            return returnType;
        }

        public Collection<MBeanParameter> getParameters() {
            return parameters;
        }
    }

    public static class MBeanParameter {
        private final String name;
        private final String type;

        public MBeanParameter(final String name, final String type) {
            this.name = name;
            this.type = type.replace("java.lang.", "");
        }

        public String getName() {
            return name;
        }

        public String getType() {
            return type;
        }
    }
}
