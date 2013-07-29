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
import org.apache.commons.monitoring.util.ClassLoaders;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.io.Closeable;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

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
            final ObjectName name = new ObjectName("org.apache.commons.monitoring:type=counter,name=" + counter.getMonitor().getKey().getName());
            SERVER.registerMBean(CounterMBean.class.cast(Proxy.newProxyInstance(ClassLoaders.current(), new Class<?>[] { CounterMBean.class }, new InvocationHandler() {
                @Override
                public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
                    return method.invoke(counter, args);
                }
            })), name);
            toUnregister.add(name);
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

    public static interface CounterMBean extends Counter {
    }
}
