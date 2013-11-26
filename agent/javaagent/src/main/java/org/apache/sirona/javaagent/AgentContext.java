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
package org.apache.sirona.javaagent;

import org.apache.sirona.Role;
import org.apache.sirona.configuration.ioc.IoCs;
import org.apache.sirona.counters.Counter;
import org.apache.sirona.javaagent.spi.InvocationListener;
import org.apache.sirona.javaagent.spi.InvocationListenerFactory;
import org.apache.sirona.javaagent.spi.Order;
import org.apache.sirona.spi.SPI;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

// just a helper to ease ASM work and reuse AbstractPerformanceInterceptor logic
public class AgentContext {
    private static final InvocationListener[] EXISTING_LISTENERS = loadAllListeners();

    private static final ConcurrentMap<Counter.Key, InvocationListener[]> LISTENERS_BY_KEY = new ConcurrentHashMap<Counter.Key, InvocationListener[]>();

    // called by agent
    public static AgentContext startOn(final Counter.Key key, final Object that) {
        return new AgentContext(key, that, listeners(key, that));
    }

    // helper to init keys in javaagent
    public static Counter.Key key(final String name) {
        return new Counter.Key(Role.PERFORMANCES, name);
    }

    private static InvocationListener[] loadAllListeners() {
        final Collection<InvocationListener> listeners = new LinkedList<InvocationListener>();
        final ClassLoader agentLoader = AgentContext.class.getClassLoader();
        for (final InvocationListener listener : SPI.INSTANCE.find(InvocationListener.class, agentLoader)) {
            addListener(listeners, null, listener);
        }
        for (final InvocationListenerFactory factory : SPI.INSTANCE.find(InvocationListenerFactory.class, agentLoader)) {
            final Map<String, InvocationListener> listenerMap = factory.listeners();
            if (listenerMap != null) {
                for (final Map.Entry<String, InvocationListener> listener : listenerMap.entrySet()) {
                    addListener(listeners, listener.getKey(), listener.getValue());
                }
            }
        }
        return listeners.toArray(new InvocationListener[listeners.size()]);
    }

    private static void addListener(final Collection<InvocationListener> listeners, final String key, final InvocationListener listener) {
        InvocationListener autoset;
        try {
            autoset = IoCs.autoSet(key, listener);
        } catch (final Exception e) {
            autoset = listener;
        }
        listeners.add(autoset);
    }

    private static InvocationListener[] listeners(final Counter.Key key, final Object that) {
        InvocationListener[] listeners = LISTENERS_BY_KEY.get(key);
        if (listeners == null) {
            listeners = findListeners(key, that);
            final InvocationListener[] old = LISTENERS_BY_KEY.putIfAbsent(key, listeners);
            if (old != null) {
                listeners = old;
            }
        }
        return listeners;
    }

    private static InvocationListener[] findListeners(final Counter.Key key, final Object that) {
        final List<InvocationListener> listeners = new LinkedList<InvocationListener>();
        for (final InvocationListener listener : EXISTING_LISTENERS) {
            if (listener.accept(key, that)) {
                listeners.add(listener);
            }
        }
        Collections.sort(listeners, ListenerComparator.INSTANCE);
        return listeners.toArray(new InvocationListener[listeners.size()]);
    }

    private final Counter.Key key;
    private final Object reference;
    private final InvocationListener[] listeners;
    private final boolean hasListeners;
    private final Map<Integer, Object> context = new HashMap<Integer, Object>();

    public AgentContext(final Counter.Key key, final Object that, final InvocationListener[] listeners) {
        this.key = key;
        this.reference = that;
        this.listeners = listeners;
        this.hasListeners = listeners.length > 0;
        if (this.hasListeners) {
            for (final InvocationListener listener : listeners) {
                listener.before(this);
            }
        }
    }

    public Object getReference() {
        return reference;
    }

    public Counter.Key getKey() {
        return key;
    }

    public <T> T get(final Integer key, final Class<T> clazz) {
        return clazz.cast(context.get(key));
    }

    public void put(final int key, Object data) {
        context.put(key, data);
    }

    public void stop() {
        stopListeners(null);
    }

    public void stopWithException(final Throwable error) {
        stopListeners(error);
    }

    private void stopListeners(final Throwable error) {
        if (hasListeners) {
            for (final InvocationListener listener : listeners) {
                listener.after(this, error);
            }
        }
    }

    static void touch() {
        // no-op
    }

    private static class ListenerComparator implements Comparator<InvocationListener> {
        private static final ListenerComparator INSTANCE = new ListenerComparator();

        private ListenerComparator() {
            // no-op
        }

        @Override
        public int compare(final InvocationListener o1, final InvocationListener o2) {
            final Order order1 = o1.getClass().getAnnotation(Order.class);
            final Order order2 = o2.getClass().getAnnotation(Order.class);
            if (order2 == null) {
                return -1;
            }
            if (order1 == null) {
                return 1;
            }
            return order1.value() - order2.value();
        }
    }
}
