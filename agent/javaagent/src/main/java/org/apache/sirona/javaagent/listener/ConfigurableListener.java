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

import org.apache.sirona.configuration.predicate.PredicateEvaluator;
import org.apache.sirona.counters.Counter;
import org.apache.sirona.javaagent.AgentContext;
import org.apache.sirona.javaagent.spi.InvocationListener;

import java.util.concurrent.atomic.AtomicInteger;

public abstract class ConfigurableListener implements InvocationListener {
    private static final AtomicInteger ID_GENERATOR = new AtomicInteger(1); // 0 is reserved for CounterListener

    private int id = ID_GENERATOR.incrementAndGet();

    private PredicateEvaluator includes = new PredicateEvaluator("true:true", ",");
    private PredicateEvaluator excludes = new PredicateEvaluator(null, null);

    protected void before(final Counter.Key key, final Object reference) {
        // no-op
    }

    protected void onSuccess(final Counter.Key key, final Object reference, final Object result) {
        // no-op
    }

    protected void onError(final Counter.Key key, final Object reference, final Throwable error) {
        // no-op
    }

    @Override
    public void before(final AgentContext context) {
        context.put(id, this);
        before(context.getKey(), context.getReference());
    }

    @Override
    public void after(final AgentContext context, final Object result, final Throwable error) {
        final ConfigurableListener listener = context.get(id, ConfigurableListener.class);
        if (listener != null) {
            if (error != null) {
                listener.onSuccess(context.getKey(), context.getReference(), result);
            } else {
                listener.onError(context.getKey(), context.getReference(), error);
            }
        }
    }

    @Override
    public boolean accept(final Counter.Key key, final Object instance) {
        final String name = key.getName();
        return includes.matches(name) && !excludes.matches(name);
    }

    // @AutoSet should be added to children
    public void setIncludes(final String includes) {
        this.includes = new PredicateEvaluator(includes, ",");
    }

    // @AutoSet should be added to children
    public void setExcludes(final String excludes) {
        this.excludes = new PredicateEvaluator(excludes, ",");
    }
}
