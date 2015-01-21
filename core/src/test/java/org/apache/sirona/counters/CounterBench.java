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
package org.apache.sirona.counters;

import org.apache.sirona.Role;
import org.apache.sirona.store.memory.counter.InMemoryCounterDataStore;
import org.junit.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;

public class CounterBench implements Runnable {
    private static final int THREADS = 30;
    private static final int LOOPS = 20000000;

    private Counter counter;
    private String mode;

    @Test
    public void defaultCounter() throws Exception {
        mode = "RentrantLockCounter";
        counter = new DefaultCounter(new Counter.Key(Role.FAILURES, mode), new InMemoryCounterDataStore());
        runConcurrent();
    }

    private void runConcurrent() throws InterruptedException {
        final long start = System.nanoTime();
        final ExecutorService pool = Executors.newFixedThreadPool(THREADS);
        for (int i = 0; i < LOOPS; i++) {
            pool.submit(this);
        }
        pool.shutdown();
        pool.awaitTermination(60, TimeUnit.SECONDS);

        final long duration = System.nanoTime() - start;
        System.out.printf("%s : %,d ns/operation %n\n", mode, duration / LOOPS);
        assertEquals(LOOPS, counter.getSum(), 0);

    }

    public void run() {
        counter.add(1, Unit.UNARY);
    }
}


