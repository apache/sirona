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
import org.apache.commons.monitoring.monitors.Monitor;

import java.util.concurrent.atomic.AtomicInteger;

import static org.apache.commons.monitoring.counter.Unit.Time.NANOSECOND;

/**
 * Simple implementation of StopWatch that estimate monitored element execution time.
 *
 * @author <a href="mailto:nicolas@apache.org">Nicolas De Loof</a>
 */
public class CounterStopWatch implements StopWatch {
    protected final Monitor monitor;
    protected final long startedAt;
    protected final Role role;
    protected final AtomicInteger concurrencyCounter;
    protected long stopedAt;
    protected long pauseDelay;
    protected boolean stoped;
    protected boolean paused;

    public CounterStopWatch(final Monitor monitor) {
        this.role = Role.PERFORMANCES;
        this.monitor = monitor;
        startedAt = nanotime();

        concurrencyCounter = monitor.currentConcurrency();
        final int concurrency = concurrencyCounter.incrementAndGet();
        monitor.updateConcurrency(concurrency);
    }

    protected long nanotime() {
        return System.nanoTime();
    }

    public long getElapsedTime() {
        if (!stoped && !paused) {
            return nanotime() - startedAt - pauseDelay;
        }
        return stopedAt - startedAt - pauseDelay;
    }

    public StopWatch pause() {
        if (!paused && !stoped) {
            stopedAt = nanotime();
            paused = true;
        }
        return this;
    }

    public StopWatch resume() {
        if (paused && !stoped) {
            pauseDelay = nanotime() - stopedAt;
            paused = false;
            stopedAt = 0;
        }
        return this;
    }

    public StopWatch stop() {
        if (!stoped) {
            final long t = nanotime();
            if (paused) {
                pauseDelay = t - stopedAt;
            }
            stopedAt = t;
            stoped = true;
            doStop();
        }
        return this;
    }

    protected void doStop() {
        monitor.getCounter(role).add(getElapsedTime(), NANOSECOND);
        concurrencyCounter.decrementAndGet();
    }

    public StopWatch stop(boolean canceled) {
        if (canceled) {
            cancel();
        } else {
            stop();
        }
        return this;
    }

    public StopWatch cancel() {
        if (!stoped) {
            stoped = true;
            doCancel();
        }
        return this;
    }

    protected void doCancel() {
        // no-op
    }

    public boolean isStoped() {
        return stoped;
    }

    public boolean isPaused() {
        return paused;
    }

    public Monitor getMonitor() {
        return monitor;
    }

    @Override
    public String toString() {
        final StringBuilder stb = new StringBuilder();
        if (monitor != null) {
            stb.append("Execution for ").append(monitor.getKey().toString()).append(" ");
        }
        if (paused) {
            stb.append("paused after ").append(getElapsedTime()).append("ns");
        } else if (stoped) {
            stb.append("stoped after ").append(getElapsedTime()).append("ns");
        } else {
            stb.append("running for ").append(getElapsedTime()).append("ns");
        }
        return stb.toString();

    }
}