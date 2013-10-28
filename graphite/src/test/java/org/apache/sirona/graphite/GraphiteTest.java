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

import org.apache.sirona.Role;
import org.apache.sirona.counters.Counter;
import org.apache.sirona.repositories.Repository;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class GraphiteTest extends GraphiteTestBase {
    @Test
    public void checkCountersAndGauges() throws InterruptedException {
        { // force some counter data
            final Counter counter = Repository.INSTANCE.getCounter(new Counter.Key(Role.PERFORMANCES, "test"));
            counter.add(1.4);
            counter.add(1.6);
            Thread.sleep(150);
            counter.add(2.3);
            counter.add(2.9);
            Thread.sleep(200);
        }

        final Collection<String> messages = messages();
        final Collection<String> counters = new ArrayList<String>(messages.size());
        final Collection<String> gauges = new ArrayList<String>(messages.size());
        for (final String msg : messages) {
            final String substring = msg.substring(0, msg.lastIndexOf(" "));
            if (msg.startsWith("counter")) {
                counters.add(substring);
            } else {
                gauges.add(substring);
            }
        }

        { // counters
            assertEquals(30, counters.size());
            assertTrue(counters.contains("counter-performances-test-Hits 2.00"));
            assertTrue(counters.contains("counter-performances-test-Max 1.60"));
            assertTrue(counters.contains("counter-performances-test-Mean 1.50"));
            assertTrue(counters.contains("counter-performances-test-Min 1.40"));
            assertTrue(counters.contains("counter-performances-test-StandardDeviation 0.14"));
            assertTrue(counters.contains("counter-performances-test-Sum 3.00"));
            assertTrue(counters.contains("counter-performances-test-Value 3.00"));
        }
        { // gauges
            assertEquals(3, gauges.size());

            final String message = gauges.toString();
            // graphite store uses an aggregated gauge store
            assertTrue("0.5 " + message, gauges.contains("gauge-mock 0.50"));
            assertTrue("2.0 " + message, gauges.contains("gauge-mock 2.00"));
            assertTrue("3.0 " + message, gauges.contains("gauge-mock 3.00"));
        }
    }
}
