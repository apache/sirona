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

import java.lang.reflect.Method;
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

    private static final ConcurrentMap<String, InvocationListener[]> LISTENERS_BY_KEY = new ConcurrentHashMap<String, InvocationListener[]>();
    private static final ConcurrentMap<String, Counter.Key> KEYS_CACHE = new ConcurrentHashMap<String, Counter.Key>();
	private static final AgentContext FAKE_CONTEXT = new AgentContext("init", null, new InvocationListener[0]);

	private static Map<String, String> agentParameters = new HashMap<String, String>( );

    public static void addAgentParameter( String key, String value){
        agentParameters.put( key, value );
    }

    /**
     *
     * @return a copy of the Agent parameters
     */
    public static Map<String,String> getAgentParameters(){
        return new HashMap<String, String>( agentParameters );
    }

    // called by agent
    public static AgentContext startOn(final String key, final Object that) {
		if (key == null) { // possible in static inits, the best would be to ignore it in instrumentation
			return FAKE_CONTEXT;
		}
        return new AgentContext(key, that, listeners(key));
    }

    // helper to init keys in javaagent
    public static Counter.Key key(final String name) {
        Counter.Key key = KEYS_CACHE.get(name);
        if (key == null) {
            key = new Counter.Key(Role.PERFORMANCES, name);
            KEYS_CACHE.putIfAbsent(name, key);
        }
        return key;
    }

    private static InvocationListener[] loadAllListeners() {
        final Collection<InvocationListener> listeners = new LinkedList<InvocationListener>();

        ClassLoader agentLoader = AgentContext.class.getClassLoader();
        if (agentLoader == null) {
            agentLoader = ClassLoader.getSystemClassLoader();
        }

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

    public static InvocationListener[] listeners(final String key) {
        InvocationListener[] listeners = LISTENERS_BY_KEY.get(key);
        if (listeners == null) {
            listeners = findListeners(key);
            if (listeners.length == 0) {
                return null;
            }

            final InvocationListener[] old = LISTENERS_BY_KEY.putIfAbsent(key, listeners);
			if (old != null) {
				listeners = old;
            }
        }
        return listeners;
    }

    private static InvocationListener[] findListeners(final String key) {
        final List<InvocationListener> listeners = new LinkedList<InvocationListener>();
        for (final InvocationListener listener : EXISTING_LISTENERS) {
            if (listener.accept(key)) {
                listeners.add(listener);
            }
        }
        Collections.sort( listeners, ListenerComparator.INSTANCE );
        return listeners.toArray(new InvocationListener[listeners.size()]);
    }

    private final String key;
    private final Object reference;
    private final InvocationListener[] listeners;
    private final Map<Integer, Object> context = new HashMap<Integer, Object>();
    private Method method = null;

    public AgentContext(final String key, final Object that, final InvocationListener[] listeners) {
        this.key = key;
        this.reference = that;
        this.listeners = listeners;
        for (final InvocationListener listener : this.listeners) {
            listener.before(this);
        }
    }

    public Object getReference() {
        return reference;
    }

    public String getKey() {
        return key;
    }

    public Class<?> keyAsClass() {
        try {
            return keyAsMethod().getDeclaringClass();
        } catch (final Throwable th) {
            return null;
        }
    }

    public Method keyAsMethod() {
        if (method != null) {
            return method;
        }

        final int lastDot = key.lastIndexOf('.');
        try {
            method = tccl().loadClass(key.substring(0, lastDot)).getDeclaredMethod(key.substring(lastDot + 1));
        } catch (final Throwable th) {
            return null;
        }
        return method;
    }

    private static ClassLoader tccl() {
        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        if (contextClassLoader == null) {
            contextClassLoader = ClassLoader.getSystemClassLoader();
        }
        return contextClassLoader;
    }

    public <T> T get(final Integer key, final Class<T> clazz) {
        return clazz.cast(context.get(key));
    }

    public void put(final int key, Object data) {
        context.put(key, data);
    }

    public void stop(final Object result) {
        stopListeners(result, null);
    }

    public void stopWithException(final Throwable error) {
        stopListeners(null, error);
    }

    private void stopListeners(final Object result, final Throwable error) {
        for (final InvocationListener listener : listeners) {
            listener.after(this, result, error);
        }
    }

    private static class ListenerComparator implements Comparator<InvocationListener>
    {
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
