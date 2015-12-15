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
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
            final Collection<String> counters = extract(messages(), "(counter-performances-test-[a-zA-Z]+) .+ [0-9]+"); // don't keep values

            assertTrue(String.valueOf(counters.size()), counters.size() >= 30);
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

            final Collection<String> gauges = extract(messages(), "(gauge-mock [0-9.]+) [0-9]+");

            assertTrue(gauges.size() >= 3);

            final String message = gauges.toString();
            // graphite store uses an aggregated gauge store
            assertTrue("0.0 " + message, gauges.contains("gauge-mock 0.00"));
            assertTrue("1.5 " + message, gauges.contains("gauge-mock 1.50"));
            assertTrue("3.5 " + message, gauges.contains("gauge-mock 3.50"));
        }
    }

    // The graphite protocol uses Unix Timestamp which is the number or seconds since epoc
    // In Java System.currentTimeMillis() is the number of milliseconds since epoc.
    // test case to reproduce the Unix Timestamp issue
    // It sends 2 events in the same second and verifies they have the same timestamp.
    @Test
    public void issueUnixTimestampEpoc() throws InterruptedException {
        final Counter counter = Repository.INSTANCE.getCounter(new Counter.Key(Role.PERFORMANCES, "UnixTimestamp"));
        counter.add(2.4);
        counter.add(2.9);

        // wait a bit so the that we can send more than once the data to the graphite server
        // wait less than a second obviously
        Thread.sleep(600);

        {
            final Collection<String> counters = extract(messages(), "counter-performances-UnixTimestamp-Hits [0-9.]+ ([0-9]+)"); // don't keep values
            validateTimestamp(counters, 1);
        }

        {
            final Collection<String> counters = extract(messages(), "gauge-mock [0-9.]+ ([0-9]+)"); // don't keep values
            validateTimestamp(counters, 5);
        }
    }

    private void validateTimestamp(final Collection<String> counters, final int delta) {
        assertTrue(counters.size() > 0);
        final Iterator<String> iterator = counters.iterator();
        String val = null;
        while (iterator.hasNext()) {
            final String next = iterator.next();
            if (val == null) {
                val = next;
            }
            // allow one second difference cause we might switch to the following second in the middle
            // with milliseconds 1ms never succeeds
            assertTrue(Integer.parseInt(next) - Integer.parseInt(val) <= delta);
        }
    }

    private static Collection<String> extract(final Collection<String> messages, final String regexp) {
        final Pattern pattern = Pattern.compile(regexp);
        final Collection<String> list = new ArrayList<String>(messages.size());
        for (final String msg : messages) {
            final Matcher matcher = pattern.matcher(msg);
            if (matcher.matches()) {
                if (matcher.groupCount() != 1) {
                    throw new IllegalArgumentException("Can only capture 1 group. Current count is " + matcher.groupCount());
                }

                list.add(matcher.group(1));
//            } else {
//                System.out.println(msg + " does not match pattern " + regexp);
            }
        }
        return list;
    }
}
