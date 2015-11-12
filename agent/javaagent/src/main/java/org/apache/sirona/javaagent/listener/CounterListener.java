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
import org.apache.sirona.configuration.ioc.AutoSet;
import org.apache.sirona.configuration.predicate.PredicateEvaluator;
import org.apache.sirona.counters.Counter;
import org.apache.sirona.javaagent.AgentContext;
import org.apache.sirona.javaagent.spi.InvocationListener;
import org.apache.sirona.spi.Order;

@Order(0)
@AutoSet
public class CounterListener extends AbstractPerformanceInterceptor<String> implements InvocationListener {

    public static final String DISABLE_PARAMETER_KEY = "disable-counter-listener";

    private static final int KEY = -1;

    private PredicateEvaluator includes = ConfigurableListener.DEFAULT_INCLUDES;
    private PredicateEvaluator excludes = ConfigurableListener.DEFAULT_EXCLUDES;

    private boolean disabled;

    @Override
    public boolean accept(final String key, final byte[] rawClassBuffer) {
        return !AgentContext.getAgentParameters().containsKey( DISABLE_PARAMETER_KEY ) //
                && includes.matches(key) //
                && !excludes.matches(key) //
                && !disabled;
    }

    // @AutoSet
    public void setIncludes(final String includes) {
        this.includes = new PredicateEvaluator(includes, ",");
    }

    // @AutoSet
    public void setExcludes(final String excludes) {
        this.excludes = new PredicateEvaluator(excludes, ",");
    }

    @Override
    public void before(final AgentContext ctx) {
        final String key = ctx.getKey();
        ctx.put(KEY, new CounterListener().before(key, key));
    }

    @Override
    public void after(final AgentContext context, final Object result, final Throwable error) {
        final Context perfCtx = context.get(KEY, Context.class);
        if (error == null) {
            perfCtx.stop();
        } else {
            perfCtx.stopWithException(error);
        }
    }

    @Override
    protected Counter.Key getKey(final String key, final String name) {
        return AgentContext.key(key);
    }

    @Override
    protected String getCounterName(final String invocation) {
        return invocation;
    }

    @Override
    protected Object extractContextKey(final String invocation) {
        return AgentContext.key(invocation);
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
    protected Object proceed(final String invocation) throws Throwable {
        return unsupportedOperation();
    }

    private static <T> T unsupportedOperation() {
        throw new UnsupportedOperationException("shouldn't be called directly");
    }

    public void setDisabled( boolean disabled )
    {
        this.disabled = disabled;
    }
}
