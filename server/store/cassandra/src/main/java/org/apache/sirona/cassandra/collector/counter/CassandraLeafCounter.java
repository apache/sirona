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
package org.apache.sirona.cassandra.collector.counter;

import org.apache.sirona.store.counter.LeafCollectorCounter;
import org.apache.sirona.math.M2AwareStatisticalSummary;

public class CassandraLeafCounter extends LeafCollectorCounter {
    private final CassandraCollectorCounterDataStore store;
    private final String marker;

    public CassandraLeafCounter(final Key key, final CassandraCollectorCounterDataStore store, final String marker) {
        super(key);
        this.store = store;
        this.marker = marker;
    }

    public CassandraLeafCounter sync(final M2AwareStatisticalSummary newStats, final int newConcurrency) {
        super.update(newStats, newConcurrency);
        return this;
    }

    @Override
    public void update(final M2AwareStatisticalSummary newStats, final int newConcurrency) {
        super.update(newStats, newConcurrency);
        store.save(this, marker);
    }

    public M2AwareStatisticalSummary getStatistics() {
        return statistics;
    }
}
