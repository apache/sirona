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
package org.apache.sirona.cassandra.agent.counter;

import org.apache.sirona.cassandra.collector.counter.CassandraCollectorCounterDataStore;
import org.apache.sirona.configuration.ioc.AutoSet;
import org.apache.sirona.configuration.ioc.Created;
import org.apache.sirona.counters.Counter;
import org.apache.sirona.math.M2AwareStatisticalSummary;
import org.apache.sirona.store.memory.counter.BatchCounterDataStore;
import org.apache.sirona.util.Localhosts;

import java.util.Collection;
import java.util.HashSet;
import java.util.logging.Logger;

@AutoSet // for marker
public class CassandraCounterDataStore extends BatchCounterDataStore
{
    private static final Logger LOGGER = Logger.getLogger(CassandraCounterDataStore.class.getName());

    private final CassandraCollectorCounterDataStore delegate = new CassandraCollectorCounterDataStore();
    protected String marker;
    protected boolean readFromStore = true;

    @Created
    protected void initMarkerIfNotAlreadyDone() {
        if (marker == null) {
            marker = Localhosts.get();
        }
        LOGGER.warning("This storage used on app side can be a bit slow, maybe consider using a remote collector");
    }

    @Override
    protected void pushCountersByBatch(final Collection<Counter> instances) {
        for (final Counter counter : instances) {
            delegate.getOrCreateCounter(counter.getKey(), marker)
                .update(new M2AwareStatisticalSummary(
                        counter.getMean(), counter.getVariance(), counter.getHits(),
                        counter.getMax(), counter.getMin(), counter.getSum(), counter.getSecondMoment()),
                    counter.currentConcurrency().get());
        }
    }

    @Override
    public Collection<Counter> getCounters() {
        final Collection<Counter> all = new HashSet<Counter>();
        if (readFromStore) {
            all.addAll(delegate.getCounters());
        }
        all.addAll(super.getCounters()); // override by more recent ones
        return all;
    }
}
