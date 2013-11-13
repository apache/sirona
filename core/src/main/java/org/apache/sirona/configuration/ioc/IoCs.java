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
package org.apache.sirona.configuration.ioc;

import org.apache.sirona.SironaException;
import org.apache.sirona.configuration.Configuration;
import org.apache.sirona.util.ClassLoaders;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

// very light "IoC" not respecting lazy init (so take care yourself of dependencies)
public final class IoCs {
    private static final Map<Class<?>, Object> SINGLETONS = new ConcurrentHashMap<Class<?>, Object>();
    private static final Collection<ToDestroy> INSTANCES = new ArrayList<ToDestroy>();
    private static Thread shutdownHook = null;

    public static <T> T[] newInstances(final Class<T> api) {
        final String names = Configuration.getProperty(api.getName(), null);
        if (names == null) {
            return (T[]) Array.newInstance(api, 0);
        }

        final String[] split = names.split(",");
        final T[] array = (T[]) Array.newInstance(api, split.length);
        for (int i = 0; i < array.length; i++) {
            try {
                array[i] = newInstance(api, split[i]);
            } catch (final Exception e) {
                throw new SironaException(e);
            }
        }
        return array;
    }

    public static synchronized <T> T findOrCreateInstance(final Class<T> clazz) {
        final T t = clazz.cast(SINGLETONS.get(clazz));
        if (t != null) {
            return t;
        }
        return newInstance(clazz);
    }

    public static synchronized <T> T newInstance(final Class<T> clazz) {
        try {
            String config = Configuration.getProperty(clazz.getName(), null);
            if (config == null) {
                if (clazz.isInterface()) {
                    config = clazz.getPackage().getName() + ".Default" + clazz.getSimpleName();
                } else {
                    config = clazz.getName();
                }
            }

            final T t = newInstance(clazz, config);
            SINGLETONS.put(clazz, t);
            return t;
        } catch (final Exception e) {
            throw new SironaException(e);
        }
    }

    private static <T> T newInstance(final Class<T> clazz, final String config) throws InstantiationException, IllegalAccessException, InvocationTargetException {
        Class<?> loadedClass;
        try {
            loadedClass = ClassLoaders.current().loadClass(config);
        } catch (final Throwable throwable) { // NoClassDefFoundError or Exception
            loadedClass = clazz;
        }
        return clazz.cast(internalProcessInstance(loadedClass.newInstance()));
    }

    public static <T> T processInstance(final T instance) {
        try {
            return internalProcessInstance(instance);
        } catch (final Exception e) {
            throw new SironaException(e);
        }
    }

    private static <T> T internalProcessInstance(final T instance) throws IllegalAccessException, InvocationTargetException {
        final Class<?> loadedClass = instance.getClass();
        for (final Method m : loadedClass.getMethods()) {
            if (m.getAnnotation(Created.class) != null) {
                m.invoke(instance);
            } else if (m.getAnnotation(Destroying.class) != null) {
                if (shutdownHook == null == Configuration.is(Configuration.CONFIG_PROPERTY_PREFIX + "shutdown.hook", true)) {
                    shutdownHook = new Thread() {
                        @Override
                        public void run() {
                            shutdown();
                        }
                    };
                    Runtime.getRuntime().addShutdownHook(shutdownHook);
                }
                INSTANCES.add(new ToDestroy(m, instance));
            }
        }

        if (loadedClass.getAnnotation(AutoSet.class) != null) {
            Class<?> current = loadedClass;
            while (current != null && !current.isInterface() && !Object.class.equals(current)) {
                for (final Field field : loadedClass.getDeclaredFields()) {
                    if (Modifier.isFinal(field.getModifiers())) {
                        continue;
                    }

                    final String value = Configuration.getProperty(loadedClass.getName() + "." + field.getName(), null);
                    if (value != null) {
                        final boolean acc = field.isAccessible();
                        if (!acc) {
                            field.setAccessible(true);
                        }
                        try {
                            field.set(instance, convertTo(field.getType(), value));
                        } finally {
                            if (!acc) {
                                field.setAccessible(false);
                            }
                        }
                    }
                }
                current = current.getSuperclass();
            }
        }

        return instance;
    }

    public static void setSingletonInstance(final Class<?> clazz, final Object instance) {
        SINGLETONS.put(clazz, instance);
    }

    public static <T> T getInstance(final Class<T> clazz) {
        return clazz.cast(SINGLETONS.get(clazz));
    }

    public static void shutdown() {
        for (final ToDestroy c : INSTANCES) {
            c.destroy();
        }
        INSTANCES.clear();
        SINGLETONS.clear();
    }

    private static Object convertTo(final Class<?> type, final String value) {
        if (String.class.equals(type)) {
            return value;
        }
        if (String[].class.equals(type)) {
            return value.split(",");
        }
        if (int.class.equals(type)) {
            return Integer.parseInt(value);
        }
        if (long.class.equals(type)) {
            return Long.parseLong(value);
        }
        throw new IllegalArgumentException("Type " + type.getName() + " not supported");
    }

    private static class ToDestroy {
        private final Method method;
        private final Object target;

        public ToDestroy(final Method m, final Object instance) {
            this.method = m;
            this.target = instance;
        }

        public void destroy() {
            try {
                method.invoke(target);
            } catch (final Exception e) {
                // no-op
            }
        }
    }

    private IoCs() {
        // no-op
    }
}
