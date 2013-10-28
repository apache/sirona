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
package org.apache.sirona.cdi.internal;

import org.apache.sirona.cdi.Monitored;
import org.apache.sirona.configuration.Configuration;
import org.apache.sirona.configuration.predicate.PredicateEvaluator;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.BeforeBeanDiscovery;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class SironaPerformanceExtension implements Extension {
    private final boolean enabled = Configuration.is(Configuration.CONFIG_PROPERTY_PREFIX + "cdi.enabled", true);
    private final Monitored performanceBinding = newAnnotation(Monitored.class);
    private final Annotation jtaBinding = tryNewAnnotation("org.apache.sirona.jta.JTAMonitored");

    private PredicateEvaluator performaceEvaluator;
    private PredicateEvaluator jtaEvaluator;

    void init(final @Observes BeforeBeanDiscovery beforeBeanDiscovery) {
        if (!enabled) {
            return;
        }

        performaceEvaluator = new PredicateEvaluator(Configuration.getProperty(Configuration.CONFIG_PROPERTY_PREFIX + "cdi.performance", null), ",");
        jtaEvaluator = new PredicateEvaluator(Configuration.getProperty(Configuration.CONFIG_PROPERTY_PREFIX + "cdi.jta", null), ",");
    }

    <A> void processAnnotatedType(final @Observes ProcessAnnotatedType<A> pat) {
        if (!enabled) {
            return;
        }

        final String beanClassName = pat.getAnnotatedType().getJavaClass().getName();
        final boolean addPerf = performaceEvaluator.matches(beanClassName);
        final boolean addJta = jtaEvaluator.matches(beanClassName);

        final WrappedAnnotatedType<A> wrapper;
        if (addPerf || addJta) {
            wrapper = new WrappedAnnotatedType<A>(pat.getAnnotatedType());
            if (addPerf) {
                wrapper.getAnnotations().add(performanceBinding);
            }
            if (addJta) {
                wrapper.getAnnotations().add(jtaBinding);
            }
        } else {
            wrapper = null;
        }
        if (wrapper != null) {
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

    private static Annotation tryNewAnnotation(final String clazz) {
        try {
            return newAnnotation(Class.class.cast(Thread.currentThread().getContextClassLoader().loadClass(clazz)));
        } catch (final ClassNotFoundException e) {
            return null;
        } catch (final NoClassDefFoundError e) {
            return null;
        }
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
