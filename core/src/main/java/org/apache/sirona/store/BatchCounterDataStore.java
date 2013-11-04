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
package org.apache.sirona.store;

import org.apache.sirona.configuration.Configuration;
import org.apache.sirona.counters.Counter;
import org.apache.sirona.repositories.Repository;
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

    protected BatchCounterDataStore() {
        final String name = getClass().getSimpleName().toLowerCase(Locale.ENGLISH).replace("counterdatastore", "");
        final long period = getPeriod(name);

        final ScheduledExecutorService ses = Executors.newSingleThreadScheduledExecutor(new DaemonThreadFactory(name + "-counter-schedule-"));
        final ScheduledFuture<?> future = ses.scheduleAtFixedRate(new BatchPushCountersTask(), period, period, TimeUnit.MILLISECONDS);
        scheduledTask = new BatchFuture(ses, future);
    }

    protected int getPeriod(final String name) {
        return Configuration.getInteger(Configuration.CONFIG_PROPERTY_PREFIX + name + ".counter.period",
            Configuration.getInteger(Configuration.CONFIG_PROPERTY_PREFIX + name + ".period", 60000));
    }

    @Configuration.Destroying
    public void shutdown() {
        scheduledTask.done();
    }

    protected abstract void pushCountersByBatch(final Collection<Counter> instance);

    private class BatchPushCountersTask implements Runnable {
        @Override
        public void run() {
            try {
                pushCountersByBatch(Repository.INSTANCE.counters());
            } catch (final Exception e) {
                LOGGER.log(Level.SEVERE, e.getMessage(), e);
            }
        }
    }
}
