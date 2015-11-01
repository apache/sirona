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
            Thread.sleep(175);
            counter.add(1.4);
            counter.add(1.6);
            Thread.sleep(140);
            counter.add(2.3);
            counter.add(2.9);
            Thread.sleep(130);
        }

        { // counters
            final Collection<String> counters = extract(extract(messages(), "counter"), "counter"); // don't keep values

            assertTrue(counters.size() >= 30);
            assertTrue(counters.contains("counter-performances-test-Hits"));
            assertTrue(counters.contains("counter-performances-test-Max"));
            assertTrue(counters.contains("counter-performances-test-Mean"));
            assertTrue(counters.contains("counter-performances-test-Min"));
            assertTrue(counters.contains("counter-performances-test-StandardDeviation"));
            assertTrue(counters.contains("counter-performances-test-Sum"));
            assertTrue(counters.contains("counter-performances-test-Value"));
        }

        { // gauges
            Thread.sleep(450);

            final Collection<String> gauges = extract(messages(), "gauge");

            assertTrue(gauges.size() >= 3);

            final String message = gauges.toString();
            // graphite store uses an aggregated gauge store
            assertTrue("0.0 " + message, gauges.contains("gauge-mock 0.00"));
            assertTrue("1.5 " + message, gauges.contains("gauge-mock 1.50"));
            assertTrue("3.5 " + message, gauges.contains("gauge-mock 3.50"));
        }
    }

    private static Collection<String> extract(final Collection<String> messages, final String prefix) {
        final Collection<String> list = new ArrayList<String>(messages.size());
        for (final String msg : messages) {
            if (msg.startsWith(prefix)) {
                list.add(msg.substring(0, msg.lastIndexOf(" ")));
            }
        }
        return list;
    }
}
