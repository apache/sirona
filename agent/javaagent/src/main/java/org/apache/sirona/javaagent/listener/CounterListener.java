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
package org.apache.sirona.javaagent.listener;

import org.apache.sirona.aop.AbstractPerformanceInterceptor;
import org.apache.sirona.counters.Counter;
import org.apache.sirona.javaagent.AgentContext;
import org.apache.sirona.javaagent.spi.InvocationListener;

public class CounterListener extends AbstractPerformanceInterceptor<Counter.Key> implements InvocationListener {
    private static final int KEY = 0;

    @Override // TODO: add config here?
    public boolean accept(final Counter.Key key, final Object instance) {
        return true;
    }

    @Override
    public void before(final AgentContext ctx) {
        final Counter.Key key = ctx.getKey();
        ctx.put(KEY, new CounterListener().before(key, key.getName()));
    }

    @Override
    public void after(final AgentContext context, final Throwable error) {
        final Context perfCtx = context.get(KEY, Context.class);
        if (error == null) {
            perfCtx.stop();
        } else {
            perfCtx.stopWithException(error);
        }
    }

    @Override
    protected Counter.Key getKey(final Counter.Key key, final String name) {
        return key;
    }

    @Override
    protected String getCounterName(final Counter.Key invocation) {
        return invocation.getName();
    }

    @Override
    protected Object extractContextKey(final Counter.Key invocation) {
        return invocation;
    }

    @Override
    protected ActivationContext getOrCreateContext(final Object m) {
        final ActivationContext c = CONTEXTS.get(m);
        if (c == null) {
            return putAndGetActivationContext(m, new ActivationContext(true, Counter.Key.class.cast(m).getName()));
        }
        return c;
    }

    @Override
    protected Object proceed(final Counter.Key invocation) throws Throwable {
        return unsupportedOperation();
    }

    private static <T> T unsupportedOperation() {
        throw new UnsupportedOperationException("shouldn't be called directly");
    }
}
