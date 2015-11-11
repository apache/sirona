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
import org.apache.sirona.spi.Order;
import org.apache.sirona.spi.SPI;

import java.lang.reflect.Array;
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
	private static final AgentContext FAKE_CONTEXT = new AgentContext("init", null, new InvocationListener[0],new Object[0]);

	private static final Map<String, String> AGENT_PARAMETERS = new ConcurrentHashMap<String, String>();
    private static final Map<String, Class<?>> PRIMITIVES = new HashMap<String, Class<?>>();
    static {
        PRIMITIVES.put("int", int.class);
        PRIMITIVES.put("short", short.class);
        PRIMITIVES.put("byte", byte.class);
        PRIMITIVES.put("long", long.class);
        PRIMITIVES.put("char", char.class);
        PRIMITIVES.put("double", double.class);
        PRIMITIVES.put("float", float.class);
        PRIMITIVES.put("boolean", boolean.class);
    }

    private static final Object[] EMPTY_ARRAY = new Object[0];

    private static final InvocationListener[] EMPTY_LISTENERS_ARRAY = new InvocationListener[0];

    public static void addAgentParameter( String key, String value){
        AGENT_PARAMETERS.put(key, value);
    }

    /**
     *
     * @return a copy of the Agent parameters
     */
    public static Map<String,String> getAgentParameters(){
        return AGENT_PARAMETERS;
    }


    /**
     * called by agent. <b>It's not part of the public api!!</b>
     * @param that
     * @param key
     * @param methodParameters
     * @return
     */
    public static AgentContext startOn(final Object that, final String key, final Object[] methodParameters) {
        if (key == null) { // possible in static inits, the best would be to ignore it in instrumentation
			return FAKE_CONTEXT;
		}
        return new AgentContext(key, that, listeners(key, null),methodParameters);
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
        final List<InvocationListener> listeners = new LinkedList<InvocationListener>();

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
        Collections.sort(listeners, ListenerComparator.INSTANCE); // sort them here to avoid to resort the list each time then
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

    public static InvocationListener[] listeners(final String key, final byte[] buffer) {
        if (key == null){
            return EMPTY_LISTENERS_ARRAY;
        }
        InvocationListener[] listeners = LISTENERS_BY_KEY.get(key);
        if (listeners == null && buffer != null) {
            listeners = findListeners(key, buffer);
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

    private static InvocationListener[] findListeners(final String key, final byte[] buffer) {
        final List<InvocationListener> listeners = new LinkedList<InvocationListener>();
        for (final InvocationListener listener : EXISTING_LISTENERS) {
            if (listener.accept(key, buffer)) {
                listeners.add(listener);
            }
        }
        return listeners.toArray(new InvocationListener[listeners.size()]);
    }

    private final String key;
    private final Object reference;
    private final InvocationListener[] listeners;

    /**
     * @since 0.3
     */
    private final Object[] methodParameters;
    private final Map<Integer, Object> context = new HashMap<Integer, Object>();
    private Method method = null;

    public AgentContext(final String key, final Object that, final InvocationListener[] listeners,final Object[] methodParameters) {
        this.key = key;
        this.reference = that;
        this.listeners = listeners;
        this.methodParameters = methodParameters;

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

    /**
     * <b>Those values won't be available if you don't activate this feature</b>
     * see {@link SironaClassVisitor#TRACE_METHOD_PARAMETERS_KEY}
     * @since 0.3
     * @return array of the parameters passed to the method on empty array but never <code>null</code>
     */
    public Object[] getMethodParameters()
    {
        return methodParameters == null ? EMPTY_ARRAY : methodParameters;
    }

    public Class<?> keyAsClass() {
        final int length = key.length();
        final int parenthesis = key.lastIndexOf('(');
        final int lastDot = key.substring(0, Math.min(parenthesis, length)).lastIndexOf('.');
        try {
            return tccl().loadClass(key.substring(0, lastDot < 0 ? parenthesis : lastDot));
        } catch (final Throwable th) {
            return null;
        }
    }

    public Method keyAsMethod() { // TODO: fixed size cache here?
        if (method != null) {
            return method;
        }

        final int length = key.length();
        final int parenthesis = key.lastIndexOf('(');
        final int lastDot = key.substring(0, Math.min(parenthesis, length)).lastIndexOf('.');
        try {
            final ClassLoader tccl = tccl();
            final Class<?> loadClass = tccl.loadClass(key.substring(0, lastDot < 0 ? parenthesis : lastDot));
            final String methodName = key.substring(lastDot + 1, parenthesis);
            if (parenthesis == length - 2) { // no param
                method = loadClass.getDeclaredMethod(methodName);
            } else {
                final Collection<Class<?>> params = new LinkedList<Class<?>>();
                for (final String paramType : key.substring(parenthesis + 1, length - 1).split(",")) {
                    params.add(load(tccl, paramType));
                }
                method = loadClass.getDeclaredMethod(methodName, params.toArray(new Class<?>[params.size()]));
            }
        } catch (final Throwable th) {
            return null;
        }
        return method;
    }

    // TODO: enhance it
    private Class<?> load(final ClassLoader tccl, final String paramType) throws ClassNotFoundException {
        final int diamond = paramType.indexOf('<');
        if (diamond > 0) { // skip generics
            return tccl.loadClass(paramType.substring(0, diamond));
        }
        if (paramType.endsWith("[]")) { // array, TODO: use ASM to not instantiate it
            return Array.newInstance(tccl.loadClass(paramType.substring(0, paramType.length() - "[]".length())), 0).getClass();
        }
        final Class<?> primitive = PRIMITIVES.get(paramType);
        if (primitive != null) {
            return primitive;
        }
        return tccl.loadClass(paramType);
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
