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
package org.apache.sirona.store.counter;

import org.apache.sirona.math.M2AwareStatisticalSummary;
import org.apache.sirona.counters.Counter;

import java.util.concurrent.locks.Lock;

public class LeafCollectorCounter extends CollectorCounter {
    public LeafCollectorCounter(final Counter.Key key) {
        super(key);
    }

    public void update(final M2AwareStatisticalSummary newStats, final int newConcurrency) {
        final Lock workLock = lock.writeLock();
        workLock.lock();
        try {
            concurrency.set(newConcurrency);
            updateConcurrency(newConcurrency);
            statistics = newStats;
        } finally {
            workLock.unlock();
        }
    }
}
