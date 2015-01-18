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
package org.apache.sirona.cdi;

import org.apache.sirona.SironaException;
import org.apache.sirona.aop.AbstractPerformanceInterceptor;

import javax.interceptor.AroundInvoke;
import javax.interceptor.AroundTimeout;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

@Interceptor
@Monitored
public class SironaInterceptor extends AbstractPerformanceInterceptor<InvocationContext> {
    @AroundInvoke
    @AroundTimeout
    public Object monitor(final InvocationContext invocationContext) throws Exception {
        try {
            return doInvoke(invocationContext);
        } catch (final Exception e) {
            throw e;
        } catch (final Throwable t) {
            throw new SironaException(t);
        }
    }

    @Override
    protected Object proceed(final InvocationContext invocation) throws Throwable {
        return invocation.proceed();
    }

    @Override
    protected String getCounterName(final InvocationContext invocation) {
        return getCounterName(invocation.getTarget(), invocation.getMethod());
    }

    @Override
    protected Object extractContextKey(final InvocationContext invocation) {
        return new SerializableMethod(invocation.getMethod());
    }
}
