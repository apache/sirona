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
package org.apache.sirona.graphite;

import org.apache.sirona.configuration.ioc.IoCs;
import org.apache.sirona.counters.Counter;
import org.apache.sirona.counters.MetricData;
import org.apache.sirona.store.memory.counter.BatchCounterDataStore;

import java.io.IOException;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GraphiteCounterDataStore extends BatchCounterDataStore
{
    private static final Logger LOGGER = Logger.getLogger(GraphiteCounterDataStore.class.getName());

    private static final String COUNTER_PREFIX = "counter-";
    private static final char SEP = '-';

    private final Graphite graphite = IoCs.findOrCreateInstance(GraphiteBuilder.class).build();

    @Override
    protected synchronized void pushCountersByBatch(final Collection<Counter> instances) {
        try {
            graphite.open();

            // timestamp is the unix epoch time in seconds NOT ms.
            final long ts = System.currentTimeMillis() / 1000l;

            for (final Counter counter : instances) {
                final Counter.Key key = counter.getKey();
                final String prefix = COUNTER_PREFIX + key.getRole().getName() + SEP + key.getName() + SEP;

                for (final MetricData data : MetricData.values()) {
                    graphite.push(
                            prefix + data.name(),
                            data.value(counter),
                            ts);
                }
            }
        } catch (final IOException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        } finally {
            graphite.close();
        }
    }

}
