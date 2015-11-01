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
package org.apache.sirona.store.memory.counter;

import org.apache.sirona.configuration.Configuration;
import org.apache.sirona.configuration.ioc.Destroying;
import org.apache.sirona.counters.Counter;
import org.apache.sirona.store.BatchFuture;
import org.apache.sirona.util.DaemonThreadFactory;

import java.util.Collection;
import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class BatchCounterDataStore extends InMemoryCounterDataStore {
    private static final Logger LOGGER = Logger.getLogger(BatchCounterDataStore.class.getName());

    protected final BatchFuture scheduledTask;
    protected final boolean clearAfterCollect;

    protected BatchCounterDataStore() {
        final String name = getClass().getSimpleName().toLowerCase(Locale.ENGLISH).replace("counterdatastore", "");
        final String prefix = Configuration.CONFIG_PROPERTY_PREFIX + name;
        final long period = getPeriod(prefix);
        clearAfterCollect = isClearAfterCollect(prefix);

        final ScheduledExecutorService ses = Executors.newSingleThreadScheduledExecutor(new DaemonThreadFactory(name + "-counter-schedule-"));
        final ScheduledFuture<?> future = ses.scheduleAtFixedRate(new BatchPushCountersTask(), period, period, TimeUnit.MILLISECONDS);
        scheduledTask = new BatchFuture(ses, future);
    }

    protected boolean isClearAfterCollect(final String prefix) {
        return Configuration.is(prefix + ".counter.clearOnCollect", false);
    }

    protected int getPeriod(final String prefix) {
        return Configuration.getInteger(prefix + ".counter.period", Configuration.getInteger(prefix + ".period", 60000));
    }

    @Destroying
    public void shutdown() {
        scheduledTask.done();
    }

    protected void clearCountersIfNeeded() {
        if (clearAfterCollect) {
            clearCounters();
        }
    }

    protected abstract void pushCountersByBatch(final Collection<Counter> instance);

    private class BatchPushCountersTask implements Runnable {
        @Override
        public void run() {
            try {
                pushCountersByBatch(counters.values());
                clearCountersIfNeeded();
            } catch (final Exception e) {
                LOGGER.log(Level.SEVERE, e.getMessage(), e);
            }
        }
    }
}
