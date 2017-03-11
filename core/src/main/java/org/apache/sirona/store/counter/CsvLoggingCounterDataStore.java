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

import org.apache.sirona.counters.Counter;

public class CsvLoggingCounterDataStore extends LoggingCounterDataStore {
    // name, role, hits, concurrency, max, min, mean, sum, stddev
    protected String format(final Counter c) {
        return '"' + c.getKey().getName().replace('"', '\'') + "\"," +
                '"' + (c.getKey().getRole() == null ? "-" : c.getKey().getRole().getName()).replace('"', '\'') + "\"," +
                c.getHits() + "," +
                c.getMaxConcurrency() + "," +
                c.getMax() + "," +
                c.getMin() + "," +
                c.getMean() + "," +
                c.getSum() + "," +
                c.getStandardDeviation();
    }
}
