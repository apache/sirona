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

package org.apache.sirona.spring;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.sirona.aop.AbstractPerformanceInterceptor;

/**
 * Spring-aop implementation of PerformanceInterceptor.
 *
 *
 */
public class AopaliancePerformanceInterceptor extends AbstractPerformanceInterceptor<MethodInvocation> implements MethodInterceptor {
    @Override
    public Object invoke(final MethodInvocation invocation) throws Throwable {
        return doInvoke(invocation);
    }

    @Override
    protected String getCounterName(final MethodInvocation invocation) {
        return getCounterName(invocation.getThis(), invocation.getMethod());
    }

    @Override
    protected Object proceed(final MethodInvocation invocation) throws Throwable {
        return invocation.proceed();
    }

    @Override
    protected Object extractContextKey(final MethodInvocation invocation) {
        return new SerializableMethod(invocation.getMethod());
    }
}