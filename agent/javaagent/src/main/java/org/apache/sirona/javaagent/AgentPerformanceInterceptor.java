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
package org.apache.sirona.javaagent;

import org.apache.sirona.Role;
import org.apache.sirona.aop.AbstractPerformanceInterceptor;
import org.apache.sirona.counters.Counter;
import org.apache.sirona.stopwatches.StopWatch;

// just a helper to ease ASM work and reuse AbstractPerformanceInterceptor logic
public class AgentPerformanceInterceptor extends AbstractPerformanceInterceptor<Counter.Key> {
    // called by agent
    public static Context start(final Counter.Key key) {
        return new AgentPerformanceInterceptor().before(key, key.getName());
    }

    // helper to init keys in javaagent
    public static Counter.Key key(final String name) {
        return new Counter.Key(Role.PERFORMANCES, name);
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
    protected Context newContext(final ActivationContext context, final StopWatch stopwatch) {
        return new AgentContext(context, stopwatch);
    }

    @Override
    protected Object proceed(final Counter.Key invocation) throws Throwable {
        throw new UnsupportedOperationException("shouldn't be called directly");
    }

    protected static class AgentContext extends Context {
        protected AgentContext(final ActivationContext activationContext, final StopWatch stopWatch) {
            super(activationContext, stopWatch);
        }

        @Override
        public void stop() {
            super.stop();
        }

        @Override
        public void stopWithException(final Throwable error) {
            super.stopWithException(error);
        }
    }
}
