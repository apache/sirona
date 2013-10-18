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
package org.apache.commons.monitoring.collector.rest.store;

import org.apache.commons.monitoring.counters.DefaultCounter;
import org.apache.commons.monitoring.store.CounterDataStore;

public class CollectorCounter extends DefaultCounter {
    public CollectorCounter(final Key key, final CounterDataStore store) {
        super(key, store);
    }

    public void addEvent(final long time, final long hits, final long sum, final int concurrency) {
        if (hits == 0) {
            return;
        }

        // TODO: find a better solution? to let hits be correct we consider we add N times the average which is
        // mathematically wrong
        // a best solution would be to push all raw data
        // but it has big impact on the measure side that we don't want
        final double avg = sum * 1. / hits;
        lock.lock();
        try {
            for (long i = 0; i < hits; i++) {
                addInternal(avg);
            }
            updateConcurrency(concurrency);
        } finally {
            lock.unlock();
        }
    }
}
