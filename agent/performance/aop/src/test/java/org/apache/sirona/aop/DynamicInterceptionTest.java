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
package org.apache.sirona.aop;

import org.apache.sirona.counters.Counter;
import org.apache.sirona.repositories.Repository;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

public class DynamicInterceptionTest {

    public static final int MAX_IT = 5;
    public static final int THRESHOLD = 100;
    private Repository repository;

    @Before
    @After
    public void reset() {
        repository = Repository.INSTANCE;
        repository.clearCounters();
    }

    @Test
    public void playWithInterceptor() throws Throwable {
        final ConfigurablePerfInterceptor interceptor = new ConfigurablePerfInterceptor();
        assertFalse(repository.counters().iterator().hasNext());

        interceptor.invoke(THRESHOLD * 5);
        Counter counter = repository.counters().iterator().next();
        assertNotNull( counter );
        assertEquals(1, counter.getHits());
        assertEquals("dynamic", repository.counters().iterator().next().getKey().getName());

        interceptor.invoke(THRESHOLD / 20);
        assertEquals(2, repository.counters().iterator().next().getHits());

        for (int i = 0; i < MAX_IT; i++) {
            interceptor.invoke(THRESHOLD * 5);
        }
        assertEquals(2, repository.counters().iterator().next().getHits());

        interceptor.invoke(THRESHOLD / 5);
        assertEquals(3, repository.counters().iterator().next().getHits());

        for (int i = 0; i < MAX_IT; i++) {
            interceptor.invoke(THRESHOLD * 2);
        }
        assertEquals(3, repository.counters().iterator().next().getHits());

        interceptor.invoke(THRESHOLD * 5);
        interceptor.invoke(THRESHOLD * 5);
        assertEquals(5, repository.counters().iterator().next().getHits());
    }

    public static class ConfigurablePerfInterceptor extends AbstractPerformanceInterceptor<Long> {
        public void invoke(final long duration) throws Throwable {
            doInvoke(duration);
        }

        @Override
        protected Object proceed(final Long duration) throws Throwable {
            Thread.sleep(duration);
            return null;
        }

        @Override
        protected String getCounterName(final Long invocation) {
            return "dynamic";
        }

        @Override
        protected Method extractContextKey(final Long invocation) {
            try {
                return getClass().getMethod("invoke", long.class);
            } catch (final NoSuchMethodException e) {
                throw new IllegalStateException(e);
            }
        }

        @Override
        protected boolean isAdaptive() {
            return true;
        }

        @Override
        protected ActivationContext doFindContext(final Long invocation) {
            return putAndGetActivationContext(extractContextKey(null), //
                                              new ActivationContext(true, //
                                                                    TimeUnit.MILLISECONDS.toNanos(THRESHOLD), //
                                                                    MAX_IT));
        }
    }
}
