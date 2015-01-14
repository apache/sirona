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
package org.apache.sirona.reporting.web.plugin.jmx;

import org.apache.commons.codec.binary.Base64;
import org.apache.sirona.SironaException;
import org.apache.sirona.configuration.Configuration;
import org.apache.sirona.reporting.web.jmx.JMXNode;
import org.apache.sirona.reporting.web.jmx.MBeanAttribute;
import org.apache.sirona.reporting.web.jmx.MBeanOperation;
import org.apache.sirona.reporting.web.jmx.MBeanParameter;
import org.apache.sirona.reporting.web.plugin.api.MapBuilder;
import org.apache.sirona.reporting.web.plugin.api.Regex;
import org.apache.sirona.reporting.web.plugin.api.Template;
import org.apache.sirona.util.ClassLoaders;

import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;
import javax.management.MBeanServerConnection;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.TabularData;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.apache.sirona.reporting.web.handler.TemplateHelper.nullProtection;

public class JMXEndpoints {
    private static final boolean METHOD_INVOCATION_ALLOWED = Configuration.is(Configuration.CONFIG_PROPERTY_PREFIX + "jmx.method.allowed", true);

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

    protected final MBeanServerConnection server;

    public JMXEndpoints() {
        this.server = ManagementFactory.getPlatformMBeanServer();
    }

    @Regex("")
    public Template home() throws IOException {
        return new Template("jmx/main.vm", new MapBuilder<String, Object>().set("jmxTree", buildJmxTree()).build());
    }

    @Regex("/operation/([^/]*)/([^/]*)/(.*)")
    public String invokeOperation(final String objectNameBase64, final String method, final String[] parameters) {
        if (!METHOD_INVOCATION_ALLOWED) {
            throw new SironaException("Method invocation not allowed");
        }

        try {
            final ObjectName name = new ObjectName(new String(Base64.decodeBase64(objectNameBase64)));
            final MBeanInfo info = server.getMBeanInfo(name);
            for (final MBeanOperationInfo op : info.getOperations()) {
                if (op.getName().equals(method)) {
                    final MBeanParameterInfo[] signature = op.getSignature();
                    final String[] sign = new String[signature.length];
                    for (int i = 0; i < sign.length; i++) {
                        sign[i] = signature[i].getType();
                    }
                    final Object result = server.invoke(name, method, convertParams(signature, parameters), sign);
                    return "<div>Method was invoked and returned:</div>" + value(result);
                }
            }
        } catch (final Exception e) {
            return "<div class=\"alert alert-error\">\n" +
                "\n" + e.getMessage() + "\n" +
                "</div>";
        }

        return "<div class=\"alert alert-error\">Operation" + method + " not found.</div>";
    }

    @Regex("/([^/]*)")
    public Template mbean(final String objectNameBase64) {
        try {
            final ObjectName name = new ObjectName(new String(Base64.decodeBase64(objectNameBase64)));
            final MBeanInfo info = server.getMBeanInfo(name);
            return new Template("templates/jmx/mbean.vm",
                new MapBuilder<String, Object>()
                    .set("objectname", name.toString())
                    .set("objectnameHash", Base64.encodeBase64URLSafeString(name.toString().getBytes()))
                    .set("classname", info.getClassName())
                    .set("description", value(info.getDescription()))
                    .set("attributes", attributes(name, info))
                    .set("operations", operations(info))
                    .build(), false);
        } catch (final Exception e) {
            throw new SironaException(e);
        }
    }

    private JMXNode buildJmxTree() throws IOException {
        final JMXNode root = new JMXNode("/");

        for (final ObjectInstance instance : server.queryMBeans(null, null)) {
            final ObjectName objectName = instance.getObjectName();
            JMXNode.addNode(root, objectName.getDomain(), objectName.getKeyPropertyListString());
        }

        return root;
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
                }
                return tryStringConstructor(WRAPPERS.get(type).getName(), value);
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
            throw new SironaException(e);
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
                return Number.class.cast(clazz.getMethod("valueOf", String.class).invoke(null, value));
            } catch (final Exception e) {
                // no-op
            }
        }

        throw new SironaException(value + " is not a number");
    }

    private static Object tryStringConstructor(String type, final String value) throws Exception {
        return ClassLoaders.current().loadClass(type).getConstructor(String.class).newInstance(value);
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

    private Collection<MBeanAttribute> attributes(final ObjectName name, final MBeanInfo info) {
        final Collection<MBeanAttribute> list = new LinkedList<MBeanAttribute>();
        for (final MBeanAttributeInfo attribute : info.getAttributes()) {
            Object value;
            try {
                value = server.getAttribute(name, attribute.getName());
            } catch (final Exception e) {
                value = "<div class=\"alert-error\">" + e.getMessage() + "</div>";
            }
            list.add(new MBeanAttribute(attribute.getName(), nullProtection(attribute.getType()), nullProtection(attribute.getDescription()), value(value)));
        }
        return list;
    }

    private static String value(final Object value) {
        try {
            if (value == null) {
                return nullProtection(null);
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
                final StringBuilder builder = new StringBuilder().append("<table class=\"table table-condensed\">");
                for (final Object type : td.keySet()) {
                    final List<?> values = (List<?>) type;
                    final CompositeData data = td.get(values.toArray(new Object[values.size()]));
                    builder.append("<tr>");
                    final Set<String> dataKeys = data.getCompositeType().keySet();
                    for (final String k : data.getCompositeType().keySet()) {
                        builder.append("<td>").append(value(data.get(k))).append("</td>");
                    }
                    builder.append("</tr>");
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
            throw new SironaException(e);
        }
    }

}
