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

package org.apache.commons.monitoring.stopwatches;

import org.apache.commons.monitoring.Role;
import org.apache.commons.monitoring.counter.Counter;
import org.apache.commons.monitoring.counter.Unit;
import org.apache.commons.monitoring.monitors.Monitor;
import org.apache.commons.monitoring.util.ClassLoaders;
import org.junit.Test;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author <a href="mailto:nicolas@apache.org">Nicolas De Loof</a>
 */
public class DefaultStopWatchTest {
    private long time;

    /**
     * assert the StopWatch computes the time elapsed during the monitored
     * process execution. User a MockTimeWatch to make timing predictable
     *
     * @throws Exception
     */
    @Test
    public void computeTime()
        throws Exception {
        time = 0;
        StopWatch stopWatch = new MockTimeWatch();
        time++;
        stopWatch.pause();
        assertTrue(stopWatch.isPaused());
        System.out.println(stopWatch.toString());
        time++;
        stopWatch.resume();
        assertTrue(!stopWatch.isPaused());
        System.out.println(stopWatch.toString());
        time++;
        stopWatch.stop();
        assertEquals(2, stopWatch.getElapsedTime());
        assertTrue(stopWatch.isStoped());
        System.out.println(stopWatch.toString());
    }

    /**
     * Check that the elapsed time computed by the WtopWatch is not affected by
     * unexpected method calls.
     *
     * @throws Exception
     */
    @Test
    public void supportUnexpectedCalls()
        throws Exception {
        time = 0;
        StopWatch stopWatch = new MockTimeWatch();

        // resume the non-paused watch
        assertTrue(!stopWatch.isPaused());
        stopWatch.resume();
        assertTrue(!stopWatch.isPaused());

        // pause the watch multiple times
        time++;
        stopWatch.pause();
        assertEquals(1, stopWatch.getElapsedTime());
        assertTrue(stopWatch.isPaused());
        time++;
        stopWatch.pause();
        assertEquals(1, stopWatch.getElapsedTime());
        assertTrue(stopWatch.isPaused());

        stopWatch.stop();
        assertEquals(1, stopWatch.getElapsedTime());
        assertTrue(stopWatch.isStoped());

        // Unexpected use after stopped
        stopWatch.resume();
        assertEquals(1, stopWatch.getElapsedTime());
        assertTrue(stopWatch.isStoped());
        stopWatch.pause();
        assertEquals(1, stopWatch.getElapsedTime());
        assertTrue(stopWatch.isStoped());
        stopWatch.stop();
        assertEquals(1, stopWatch.getElapsedTime());
        assertTrue(stopWatch.isStoped());
    }


    private class MockTimeWatch extends CounterStopWatch {
        public MockTimeWatch() {
            super(new NullMonitor());
        }

        @Override
        protected long nanotime() {
            return time;
        }
    }

    public static class NullMonitor implements Monitor {
        private static final Unit NULL = new Unit("null") {
            @Override
            public boolean isCompatible(Unit unit) {
                return true;
            }
        };

        private static final Role NOP_COUNTER = new Role("NopCounter", NULL);

        private static final Role NOP_GAUGE = new Role("NopGauge", NULL);

        private static final Counter counter = Counter.class.cast(Proxy.newProxyInstance(ClassLoaders.current(), new Class<?>[] { Counter.class }, new InvocationHandler() {
            @Override
            public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
                return null;
            }
        }));

        private Collection<Counter> counters = Arrays.asList(counter);

        public Counter getCounter(String role) {
            return counter;
        }

        public Counter getCounter(Role role) {
            return counter;
        }

        public Key getKey() {
            return new Key("noOp", null);
        }

        public Counter getMetric(String role) {
            return counter;
        }

        public Counter getMetric(Role role) {
            return counter;
        }

        public Collection<Counter> getCounters() {
            return counters;
        }

        public Collection<Role> getRoles() {
            return Arrays.asList(NOP_COUNTER, NOP_GAUGE);
        }

        public void reset() {
            // NoOp
        }

        @Override
        public AtomicInteger currentConcurrency() {
            return new AtomicInteger();
        }

        @Override
        public void updateConcurrency(int concurrency) {
            // no-op
        }

        @Override
        public int getMaxConcurrency() {
            return 0;
        }

        @Override
        public int getConcurrency() {
            return 0;
        }
    }
}
