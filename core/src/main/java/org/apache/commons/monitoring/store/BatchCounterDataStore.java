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
package org.apache.commons.monitoring.store;

import org.apache.commons.monitoring.Role;
import org.apache.commons.monitoring.configuration.Configuration;
import org.apache.commons.monitoring.repositories.Repository;
import org.apache.commons.monitoring.util.DaemonThreadFactory;

import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public abstract class BatchCounterDataStore extends InMemoryCounterBaseStore {
    protected final BatchFuture scheduledTask;

    protected BatchCounterDataStore() {
        final String name = getClass().getSimpleName().toLowerCase(Locale.ENGLISH).replace("datastore", "");
        final long period = Configuration.getInteger(Configuration.COMMONS_MONITORING_PREFIX + name + ".period", 60000);

        final ScheduledExecutorService ses = Executors.newSingleThreadScheduledExecutor(new DaemonThreadFactory(name + "-schedule-"));
        final ScheduledFuture<?> future = ses.scheduleAtFixedRate(new BatchPushCountersTask(), period, period, TimeUnit.MILLISECONDS);
        scheduledTask = new BatchFuture(ses, future);
    }

    @Configuration.Destroying
    public void shutdown() {
        scheduledTask.done();
    }

    @Override
    public void createOrNoopGauge(final Role role) {
        // no-op
    }

    protected abstract void pushCountersByBatch(final Repository instance);

    private class BatchPushCountersTask implements Runnable {
        @Override
        public void run() {
            pushCountersByBatch(Repository.INSTANCE);
        }
    }

    public static class BatchFuture {
        private final ScheduledExecutorService executor;
        private final ScheduledFuture<?> task;

        public BatchFuture(final ScheduledExecutorService ses, final ScheduledFuture<?> future) {
            this.executor = ses;
            this.task = future;
        }

        public void done() {
            try {
                executor.shutdown(); // don't add anything more now
                task.cancel(false);
                executor.awaitTermination(1, TimeUnit.MINUTES);
                if (!task.isDone()) {
                    task.cancel(true);
                }
            } catch (final InterruptedException e) {
                // no-op
            }
        }
    }
}
