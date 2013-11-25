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
package org.apache.sirona.jpa;

import org.apache.sirona.Role;
import org.apache.sirona.aop.AbstractPerformanceInterceptor;
import org.apache.sirona.util.ClassLoaders;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public final class JPAProxyFactory {
    // more designed as internal than user friendly, that's why it is not in aop module
    public static Object monitor(final Class<?>[] classes, final Object instance, final Role role, final boolean cascade) {
        return classes[0].cast(
            Proxy.newProxyInstance(ClassLoaders.current(), classes, new JSEMonitoringHandler(instance, role, cascade)));
    }

    private JPAProxyFactory() {
        // no-op
    }

    private static class JSEMonitoringHandler extends AbstractPerformanceInterceptor<Invocation> implements InvocationHandler {
        private final Object instance;
        private final Role role;
        private final boolean cascade;

        public JSEMonitoringHandler(final Object instance, final Role role, final boolean cascade) {
            this.instance = instance;
            this.role = role;
            this.cascade = cascade;
        }

        @Override
        public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
            if ("toString".equals(method.getName())) {
                return "MonitoringProxy[" + instance + "]";
            }

            final Object o = doInvoke(new Invocation(instance, method, args));
            final Class<?> returnType = method.getReturnType();
            if (cascade && returnType.isInterface()) { // not java.*
                return monitor(classes(returnType, o), o, role, true);
            }
            return o;
        }

        @Override
        protected Object proceed(final Invocation invocation) throws Throwable {
            try {
                return invocation.method.invoke(invocation.target, invocation.args);
            } catch (final InvocationTargetException ite) {
                throw ite.getCause();
            }
        }

        @Override
        protected String getCounterName(final Invocation invocation) {
            return getCounterName(invocation.target, invocation.method);
        }

        @Override
        protected Role getRole() {
            return role;
        }

        protected Class<?>[] classes(final Class<?> returnType, final Object o) {
            if (Serializable.class.isInstance(o)) {
                return new Class<?>[] { returnType, Serializable.class };
            }
            return new Class<?>[] { returnType };
        }
    }

    private static class Invocation {
        private final Object target;
        private final Method method;
        private final Object[] args;

        private Invocation(final Object target, final Method method, final Object[] args) {
            this.target = target;
            this.method = method;
            this.args = args;
        }
    }
}
