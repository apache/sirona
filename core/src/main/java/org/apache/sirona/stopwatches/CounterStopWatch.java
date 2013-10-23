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
package org.apache.sirona.stopwatches;

import org.apache.sirona.counters.Counter;

import java.util.concurrent.atomic.AtomicInteger;

import static org.apache.sirona.counters.Unit.Time.NANOSECOND;

public class CounterStopWatch implements StopWatch {
    protected final Counter counter;
    protected final long startedAt;
    protected final AtomicInteger concurrencyCounter;
    protected long stopedAt;
    protected boolean stoped;

    public CounterStopWatch(final Counter counter) {
        this.counter = counter;
        startedAt = nanotime();

        concurrencyCounter = counter.currentConcurrency();
        final int concurrency = concurrencyCounter.incrementAndGet();
        counter.updateConcurrency(concurrency);
    }

    protected long nanotime() {
        return System.nanoTime();
    }

    @Override
    public long getElapsedTime() {
        if (!stoped) {
            return nanotime() - startedAt;
        }
        return stopedAt - startedAt;
    }

    @Override
    public StopWatch stop() {
        if (!stoped) {
            stopedAt = nanotime();
            stoped = true;
            doStop();
        }
        return this;
    }

    protected void doStop() {
        counter.add(getElapsedTime(), NANOSECOND);
        concurrencyCounter.decrementAndGet();
    }

    @Override
    public String toString() {
        final StringBuilder stb = new StringBuilder();
        if (counter != null) {
            stb.append("Execution for ").append(counter.getKey().toString()).append(" ");
        }
        if (stoped) {
            stb.append("stoped after ").append(getElapsedTime()).append("ns");
        } else {
            stb.append("running for ").append(getElapsedTime()).append("ns");
        }
        return stb.toString();

    }
}