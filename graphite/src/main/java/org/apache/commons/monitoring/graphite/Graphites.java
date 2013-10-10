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
package org.apache.commons.monitoring.graphite;

import org.apache.commons.monitoring.configuration.Configuration;
import org.apache.commons.monitoring.counters.Counter;
import org.apache.commons.monitoring.counters.MetricData;
import org.apache.commons.monitoring.repositories.Repository;
import org.apache.commons.monitoring.util.DaemonThreadFactory;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class Graphites {
    public static final Graphite GRAPHITE = Configuration.newInstance(GraphiteBuilder.class).build();

    public static GraphiteFuture scheduleReport() {
        return scheduleReport(GRAPHITE, Configuration.getInteger(Configuration.COMMONS_MONITORING_PREFIX + "graphite.period", 60000));
    }

    public static GraphiteFuture scheduleReport(final long period) {
        return scheduleReport(GRAPHITE, period);
    }

    public static GraphiteFuture scheduleReport(final Graphite graphite, final long period) {
        final ScheduledExecutorService ses = Executors.newSingleThreadScheduledExecutor(new DaemonThreadFactory("graphite-schedule-"));
        final ScheduledFuture<?> future = ses.schedule(new GraphitePushCountersTask(graphite), period, TimeUnit.MILLISECONDS);
        ses.shutdown(); // don't add anything more
        return new GraphiteFuture(ses, future);
    }

    public static class GraphiteFuture {
        private final ScheduledExecutorService executor;
        private final ScheduledFuture<?> task;

        public GraphiteFuture(final ScheduledExecutorService ses, final ScheduledFuture<?> future) {
            this.executor = ses;
            this.task = future;
        }

        public void done() {
            try {
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

    private Graphites() {
        // no-op
    }

    private static class GraphitePushCountersTask implements Runnable {
        private static final Logger LOGGER = Logger.getLogger(GraphitePushCountersTask.class.getName());

        private static final String COUNTER_PREFIX = "counter-";
        private static final char SEP = '-';

        private final Graphite connector;

        public GraphitePushCountersTask(final Graphite graphite) {
            this.connector = graphite;
        }

        @Override
        public void run() {
            final Repository repo = Repository.INSTANCE;

            try {
                connector.open();

                final long ts = System.currentTimeMillis();

                for (final Counter counter : repo) {
                    final Counter.Key key = counter.getKey();
                    final String prefix = COUNTER_PREFIX + key.getRole().getName() + SEP + key.getName() + SEP;

                    for (final MetricData data : MetricData.values()) {
                        connector.push(
                            prefix + data.name(),
                            data.value(counter),
                            ts);
                    }
                }
            } catch (final IOException e) {
                LOGGER.log(Level.SEVERE, e.getMessage(), e);
            } finally {
                connector.close();
            }
        }
    }
}
