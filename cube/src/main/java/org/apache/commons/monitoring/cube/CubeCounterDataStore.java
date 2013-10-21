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
package org.apache.commons.monitoring.cube;

import org.apache.commons.monitoring.configuration.Configuration;
import org.apache.commons.monitoring.counters.Counter;
import org.apache.commons.monitoring.counters.MetricData;
import org.apache.commons.monitoring.repositories.Repository;
import org.apache.commons.monitoring.store.BatchCounterDataStore;

public class CubeCounterDataStore extends BatchCounterDataStore {
    private static final String COUNTER_TYPE = "counter";

    private static final String NAME = "name";
    private static final String ROLE = "role";
    private static final String UNIT = "unit";
    private static final String CONCURRENCY = "concurrency";
    private static final String MEAN = "mean";
    private static final String VARIANCE = "variance";
    private static final String HITS = "hits";
    private static final String MAX = "max";
    private static final String MIN = "min";
    private static final String SUM = "sum";
    private static final String M_2 = "m2";

    private final Cube cube = Configuration.findOrCreateInstance(CubeBuilder.class).build();

    @Override
    protected synchronized void pushCountersByBatch(final Repository instance) {
        final long ts = System.currentTimeMillis();
        final StringBuilder events = cube.newEventStream();
        for (final Counter counter : instance) {
            cube.buildEvent(events, COUNTER_TYPE, ts, new MapBuilder()
                    .add(NAME, counter.getKey().getName())
                    .add(ROLE, counter.getKey().getRole().getName())
                    .add(UNIT, counter.getKey().getRole().getUnit().getName())
                    // minimum metrics to be able to aggregate counters later
                    .add(CONCURRENCY, counter.currentConcurrency().intValue())
                    .add(MEAN, counter.getMean())
                    .add(VARIANCE, counter.getVariance())
                    .add(HITS, counter.getHits())
                    .add(MAX, counter.getMax())
                    .add(MIN, counter.getMin())
                    .add(SUM, counter.getSum())
                    .add(M_2, counter.getSecondMoment())
                    .map());
        }
        cube.post(events);
    }
}
