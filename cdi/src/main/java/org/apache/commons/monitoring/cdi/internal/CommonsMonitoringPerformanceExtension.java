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
package org.apache.commons.monitoring.cdi.internal;

import org.apache.commons.monitoring.cdi.Monitored;
import org.apache.commons.monitoring.configuration.Configuration;
import org.apache.commons.monitoring.util.ClassLoaders;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CommonsMonitoringPerformanceExtension implements Extension {
    private static final Logger LOGGER = Logger.getLogger(CommonsMonitoringPerformanceExtension.class.getName());

    private static final String PERFORMANCE_MARKER = "performance";

    private final boolean enabled = Configuration.is(Configuration.COMMONS_MONITORING_PREFIX + "cdi.enabled", true);
    private final Monitored binding = newAnnotation(Monitored.class);
    private final Map<Class<? extends Annotation>, Annotation> otherBindings = new HashMap<Class<? extends Annotation>, Annotation>();

    <A> void processAnnotatedType(final @Observes ProcessAnnotatedType<A> pat) {
        if (!enabled) {
            return;
        }

        final String beanClassName = pat.getAnnotatedType().getJavaClass().getName();
        final String configuration = findConfiguration(beanClassName);
        if (configuration == null) {
            return;
        }

        final Collection<String> configForThisBean = Arrays.asList(configuration.split(","));
        if (!configForThisBean.isEmpty()) {
            final WrappedAnnotatedType<A> wrapper = new WrappedAnnotatedType<A>(pat.getAnnotatedType());
            for (final String rawConfig : configForThisBean) {
                final String config = rawConfig.trim();

                if (PERFORMANCE_MARKER.equals(config)) {
                    wrapper.getAnnotations().add(binding);
                } else { // convention is from <name> the binding is org.apache.commons.monitoring.<lowercase(name)>.<uppercase(name)>Monitored, ex: jta
                    final String deducedName = "org.apache.commons.monitoring." + config.toLowerCase(Locale.ENGLISH) + "." + config.toUpperCase(Locale.ENGLISH) + "Monitored";
                    try {
                        final Class<? extends Annotation> annotationType = Class.class.cast(ClassLoaders.current().loadClass(deducedName));
                        Annotation instance = otherBindings.get(annotationType);
                        if (instance == null) {
                            instance = newAnnotation(annotationType);
                            otherBindings.put(annotationType, instance);
                        }
                        wrapper.getAnnotations().add(instance);
                    } catch (final ClassNotFoundException e) {
                        LOGGER.log(Level.SEVERE, e.getMessage(), e);
                    }
                }
            }
            pat.setAnnotatedType(wrapper);
        }
    }

    private static String findConfiguration(final String name) {
        String current = name;
        String property;
        do {
            property = Configuration.getProperty(current + ".cdi", null);

            final int endIndex = current.lastIndexOf('.');
            if (endIndex > 0) {
                current = current.substring(0, endIndex);
            } else {
                current = null;
            }
        } while (property == null && current != null);
        return property;
    }

    private static <T extends Annotation> T newAnnotation(final Class<T> clazz) {
        return clazz.cast(
                Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),
                new Class<?>[]{ Annotation.class, clazz },
                new AnnotationHandler(clazz)));
    }

    // Note: for annotations without any members
    private static class AnnotationHandler implements InvocationHandler, Annotation, Serializable {
        private final Class<? extends Annotation> annotationClass;

        private AnnotationHandler(final Class<? extends Annotation> annotationClass) {
            this.annotationClass = annotationClass;
        }

        @Override
        public Object invoke(final Object proxy, final Method method, final Object[] args) throws Exception {
            if ("hashCode".equals(method.getName())) {
                return hashCode();
            } else if ("equals".equals(method.getName())) {
                if (Proxy.isProxyClass(args[0].getClass()) && AnnotationHandler.class.isInstance(Proxy.getInvocationHandler(args[0]))) {
                    return equals(Proxy.getInvocationHandler(args[0]));
                }
                return equals(args[0]);
            } else if ("annotationType".equals(method.getName())) {
                return annotationType();
            } else if ("toString".equals(method.getName())) {
                return toString();
            }
            return method.getDefaultValue();
        }

        @Override
        public Class<? extends Annotation> annotationType() {
            return annotationClass;
        }

        @Override
        public String toString() {
            return "@" + annotationClass.getName();
        }

        @Override
        public boolean equals(final Object o) {
            return this == o
                || Annotation.class.isInstance(o) && Annotation.class.cast(o).annotationType().equals(annotationClass);
        }

        @Override
        public int hashCode() {
            return 0;
        }
    }
}
