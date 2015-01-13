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
import org.apache.sirona.javaagent.AgentContext;
import org.apache.sirona.javaagent.spi.InvocationListener;

import java.util.concurrent.atomic.AtomicInteger;

public abstract class ConfigurableListener<I, R> implements InvocationListener {
    private static final AtomicInteger ID_GENERATOR = new AtomicInteger(1); // 0 is reserved for CounterListener

    public static final PredicateEvaluator DEFAULT_EXCLUDES = new PredicateEvaluator(
            // sirona itself
            "prefix:org.apache.sirona," +
            // io often generates exceptions
            "prefix:org.apache.velocity," +
            // the JVM
            "container:jvm," +
            // Apache Tomcat and TomEE
            "container:tomee", ",");
    public static final PredicateEvaluator DEFAULT_INCLUDES = new PredicateEvaluator("true:true", ",");

    private int id = ID_GENERATOR.incrementAndGet();

    private PredicateEvaluator includes = DEFAULT_INCLUDES;
    private PredicateEvaluator excludes = DEFAULT_EXCLUDES;

    protected void before(final String key, final I reference) {
        // no-op
    }

    protected void onSuccess(final String key, final I reference, final R result) {
        // no-op
    }

    protected void onError(final String key, final I reference, final Throwable error) {
        // no-op
    }

    @Override
    public void before(final AgentContext context) {
        context.put(id, this);
        before(context.getKey(), (I) context.getReference());
    }

    @Override
    public void after(final AgentContext context, final Object result, final Throwable error) {
        final ConfigurableListener<I, R> listener = context.get(id, ConfigurableListener.class);
        if (listener != null) {
            if (error != null) {
                listener.onSuccess(context.getKey(), (I) context.getReference(), (R) result);
            } else {
                listener.onError(context.getKey(), (I) context.getReference(), error);
            }
        }
    }

    @Override
    public boolean accept(final String name, final byte[] rawClassBuffer) {
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
