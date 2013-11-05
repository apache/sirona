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
package org.apache.sirona.aspectj;

import org.apache.sirona.aop.AbstractPerformanceInterceptor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

import java.lang.reflect.Method;

@Aspect
public abstract class SironaAspect extends AbstractPerformanceInterceptor<ProceedingJoinPoint> {
    @Pointcut
    protected abstract void pointcut();

    @Around("pointcut()")
    public Object monitor(final ProceedingJoinPoint pjp) throws Throwable {
        return doInvoke(pjp);
    }

    @Override
    protected Object proceed(final ProceedingJoinPoint invocation) throws Throwable {
        return invocation.proceed();
    }

    @Override
    protected String getCounterName(final ProceedingJoinPoint invocation) {
        final String monitorName = getCounterName(invocation.getTarget(), findMethod(invocation.getSignature()));
        if (monitorName != null) {
            return monitorName;
        }
        return invocation.getSignature().toLongString();
    }

    @Override
    protected Object extractContextKey(final ProceedingJoinPoint invocation) {
        return new SerializableMethod(findMethod(invocation.getSignature()));
    }

    private static Method findMethod(final Signature signature) {
        if ("org.aspectj.runtime.reflect.MethodSignatureImpl".equals(signature.getClass().getName())) {
            try {
                final Method mtd = signature.getClass().getMethod("getMethod");
                if (!mtd.isAccessible()) {
                    mtd.setAccessible(true);
                }
                return Method.class.cast(mtd.invoke(signature));
            } catch (final Exception e) {
                // no-op
            }
        }
        return null;
    }
}
