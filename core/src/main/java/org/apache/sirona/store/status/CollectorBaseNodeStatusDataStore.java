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
package org.apache.sirona.store.status;

import org.apache.sirona.alert.AlertListener;
import org.apache.sirona.alert.AlerterSupport;
import org.apache.sirona.configuration.Configuration;
import org.apache.sirona.configuration.ioc.Destroying;
import org.apache.sirona.store.BatchFuture;
import org.apache.sirona.util.DaemonThreadFactory;

import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class CollectorBaseNodeStatusDataStore implements CollectorNodeStatusDataStore {
    private final AlerterSupport listeners = new AlerterSupport();
    private final AtomicReference<BatchFuture> scheduledTask = new AtomicReference<BatchFuture>();

    @Destroying
    public void shutdown() {
        final BatchFuture task = scheduledTask.get();
        if (task != null) {
            task.done();
            scheduledTask.compareAndSet(task, null);
        }
    }

    @Override
    public void reset() {
        shutdown();
    }

    @Override
    public void addAlerter(final AlertListener listener) {
        listeners.addAlerter(listener);
        if (scheduledTask.get() == null) {
            final BatchFuture update = startTask();
            if (!scheduledTask.compareAndSet(null, update)) {
                update.done();
            }
        }
    }

    @Override
    public void removeAlerter(final AlertListener listener) {
        listeners.removeAlerter(listener);
        if (!listeners.hasAlerter()) {
            shutdown();
        }
    }

    private BatchFuture startTask() {
        final String name = getClass().getSimpleName().toLowerCase(Locale.ENGLISH).replace("nodestatusdatastore", "");
        final long period = getPeriod(name);

        final ScheduledExecutorService ses = Executors.newSingleThreadScheduledExecutor(new DaemonThreadFactory(name + "-status-alert-schedule-"));
        final ScheduledFuture<?> future = ses.scheduleAtFixedRate(new CheckStatus(), period, period, TimeUnit.MILLISECONDS);
        return new BatchFuture(ses, future);
    }

    protected int getPeriod(final String name) {
        return Configuration.getInteger(Configuration.CONFIG_PROPERTY_PREFIX + name + ".status.period",
            Configuration.getInteger(Configuration.CONFIG_PROPERTY_PREFIX + name + ".period", 60000));
    }

    private class CheckStatus implements Runnable {
        private final Logger logger = Logger.getLogger(CheckStatus.class.getName());

        public void run() {
            try {
                listeners.notify(CollectorBaseNodeStatusDataStore.this.statuses());
            } catch (final Exception e) {
                logger.log(Level.SEVERE, e.getMessage(), e);
            }
        }
    }
}
