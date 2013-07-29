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
package org.apache.commons.monitoring.aop;

import org.apache.commons.monitoring.counter.Counter;
import org.apache.commons.monitoring.repositories.Repository;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class MonitoringProxyFactoryTest {
    @Test
    public void test() {
        final Foo foo = MonitoringProxyFactory.monitor(Foo.class, new FooImpl());
        foo.haveARest(2000);

        final Counter perf = Repository.INSTANCE.getMonitor(FooImpl.class.getName() + ".haveARest").getCounter("performances");
        assertNotNull(perf);
        assertEquals(2000, TimeUnit.NANOSECONDS.toMillis((int) perf.getMax()), 200);

        try {
            foo.throwSthg();
        } catch (final Exception e) {
            // normal
        }

        final Counter failures = Repository.INSTANCE.getMonitor(FooImpl.class.getName() + ".throwSthg").getCounter("failures");
        assertNotNull(failures);
        assertEquals(1, failures.getHits());
    }

    public static interface Foo {
        void haveARest(long ms);
        void throwSthg();
    }

    public static class FooImpl implements Foo {
        @Override
        public void haveARest(long ms) {
            try {
                Thread.sleep(ms);
            } catch (final InterruptedException e) {
                // no-ôp
            }
        }

        public void throwSthg() {
            throw new UnsupportedOperationException();
        }
    }
}
