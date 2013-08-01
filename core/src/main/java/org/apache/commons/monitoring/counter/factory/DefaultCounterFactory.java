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
package org.apache.commons.monitoring.counter.factory;

import org.apache.commons.monitoring.MonitoringException;
import org.apache.commons.monitoring.Role;
import org.apache.commons.monitoring.configuration.Configuration;
import org.apache.commons.monitoring.counter.Counter;
import org.apache.commons.monitoring.counter.DefaultCounter;
import org.apache.commons.monitoring.monitors.Monitor;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.DynamicMBean;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanConstructorInfo;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import java.io.Closeable;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class DefaultCounterFactory implements CounterFactory, Closeable {
    private static final boolean JMX = Configuration.isActivated(Configuration.Keys.JMX);
    private static final MBeanServer SERVER = ManagementFactory.getPlatformMBeanServer();

    private final Collection<ObjectName> toUnregister = Collections.synchronizedList(new ArrayList<ObjectName>());

    @Override
    public Counter newCounter(final Role role) {
        return new DefaultCounter(role);
    }

    @Override
    public void counterCreated(final Counter counter) {
        if (!JMX) {
            return;
        }

        try {
            final ObjectName name = new ObjectName(
                        "org.apache.commons.monitoring:" +
                            "type=counter," +
                            "monitorName=" + counter.getMonitor().getKey().getName().replace(",", " ").replace("=", " ").replace(";", " ").replace("*", " ").replace("?", " ") + "," +
                            "monitorCategory=" + counter.getMonitor().getKey().getCategory() + "," +
                            "role=" + counter.getRole().getName());
            if (!SERVER.isRegistered(name)) {
                SERVER.registerMBean(new CounterMBean(counter), name);
                toUnregister.add(name);
            }
        } catch (final Exception e) {
            throw new MonitoringException(e);
        }
    }

    @Override
    public synchronized void close() throws IOException {
        for (final ObjectName name : toUnregister) {
            try {
                SERVER.unregisterMBean(name);
            } catch (final Exception e) {
                // no-op
            }
        }
    }

    public static class CounterMBean implements DynamicMBean {
        private static final Map<String, Method> METHODS = new ConcurrentHashMap<String, Method>();
        private static final List<String> VIRTUAL_FIELDS = new CopyOnWriteArrayList<String>() {{
            add("Role");
            add("Monitor");
            add("Unit");
        }};
        private static final MBeanInfo INFO = computeCounterMBeanInfo();

        private static MBeanInfo computeCounterMBeanInfo() {
            final Collection<MBeanAttributeInfo> attributes = new LinkedList<MBeanAttributeInfo>();
            final Collection<MBeanOperationInfo> operations = new LinkedList<MBeanOperationInfo>();

            for (final Method m : Counter.class.getMethods()) {
                final String name = m.getName();
                if ("setMonitor".equals(name)) {
                    continue;
                }

                if (name.startsWith("get")) {
                    final String attributeName = name.substring("get".length());
                    if (VIRTUAL_FIELDS.contains(attributeName)) {
                        attributes.add(new MBeanAttributeInfo(attributeName, String.class.getName(), attributeName + " value", true, false, false));
                    } else {
                        attributes.add(new MBeanAttributeInfo(attributeName, m.getReturnType().getName(), attributeName + " value", true, false, false));
                    }
                } else {
                    METHODS.put(name, m);
                    operations.add(new MBeanOperationInfo(name + " method", m));
                }
            }

            return new MBeanInfo(
                CounterMBean.class.getName(),
                "Counter MBean",
                attributes.toArray(new MBeanAttributeInfo[attributes.size()]),
                new MBeanConstructorInfo[0],
                operations.toArray(new MBeanOperationInfo[operations.size()]),
                new MBeanNotificationInfo[0]);
        }

        private final Counter delegate;

        public CounterMBean(final Counter counter) {
            delegate = counter;
        }

        @Override
        public Object getAttribute(final String attribute) throws AttributeNotFoundException, MBeanException, ReflectionException {
            if (VIRTUAL_FIELDS.contains(attribute)) {
                if ("Role".equals(attribute)) {
                    final Role role = delegate.getRole();
                    return role.getName() + " (" + role.getUnit().getName() + ")";
                }
                if ("Unit".equals(attribute)) {
                    return delegate.getUnit().getName();
                }
                if ("Monitor".equals(attribute)) {
                    final Monitor.Key key = delegate.getMonitor().getKey();
                    return key.getName() + " (" + key.getCategory() + ")";
                }
            }
            try {
                return Counter.class.getMethod("get" + attribute).invoke(delegate);
            } catch (final Exception e) {
                throw new AttributeNotFoundException(e.getMessage());
            }
        }

        @Override
        public void setAttribute(final Attribute attribute) throws AttributeNotFoundException, InvalidAttributeValueException, MBeanException, ReflectionException {
            throw new UnsupportedOperationException("Read Only MBean");
        }

        @Override
        public AttributeList getAttributes(final String[] attributes) {
            final AttributeList list = new AttributeList();
            for (final String attribute : attributes) {
                try {
                    list.add(new Attribute(attribute, getAttribute(attribute)));
                } catch (final Exception e) {
                    // no-op
                }
            }
            return list;
        }

        @Override
        public AttributeList setAttributes(final AttributeList attributes) {
            throw new UnsupportedOperationException("Read Only MBean");
        }

        @Override
        public Object invoke(final String actionName, final Object[] params, final String[] signature) throws MBeanException, ReflectionException {
            final Method m = METHODS.get(actionName);
            if (m == null) {
                throw new MBeanException(new NullPointerException(), "method " + actionName + " doesn't exist");
            }
            try {
                return m.invoke(delegate, params);
            } catch (final Exception e) {
                throw new MBeanException(e, e.getMessage());
            }
        }

        @Override
        public MBeanInfo getMBeanInfo() {
            return INFO;
        }
    }
}
