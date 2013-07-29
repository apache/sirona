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
package org.apache.commons.monitoring.aop;

import org.apache.commons.monitoring.configuration.Configuration;
import org.apache.commons.monitoring.util.ClassLoaders;
import org.apache.commons.proxy.Invoker;
import org.apache.commons.proxy.ProxyFactory;

import java.lang.reflect.Method;

public final class MonitoringProxyFactory {
    private static final ProxyFactory PROXY_FACTORY = Configuration.newInstance(ProxyFactory.class);

    public static <T> T monitor(final Class<T> clazz, final Object instance) {
        return clazz.cast(PROXY_FACTORY.createInvokerProxy(ClassLoaders.current(), new MonitoringHandler(instance), new Class<?>[]{clazz}));
    }

    private MonitoringProxyFactory() {
        // no-op
    }

    private static class MonitoringHandler extends AbstractPerformanceInterceptor<Invocation> implements Invoker {
        private final Object instance;

        public MonitoringHandler(final Object instance) {
            this.instance = instance;
        }

        @Override
        public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
            return doInvoke(new Invocation(instance, method, args));
        }

        @Override
        protected Object proceed(final Invocation invocation) throws Throwable {
            return invocation.method.invoke(invocation.target, invocation.args);
        }

        @Override
        protected String getMonitorName(final Invocation invocation) {
            return getMonitorName(invocation.target, invocation.method);
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
