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
package org.apache.sirona.collector.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.sirona.Role;
import org.apache.sirona.counters.CollectorCounterStore;
import org.apache.sirona.configuration.Configuration;
import org.apache.sirona.counters.Counter;
import org.apache.sirona.counters.Unit;
import org.apache.sirona.repositories.Repository;
import org.apache.sirona.store.CounterDataStore;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class HttpCollectorTest {
    private CollectorServer server;
    private ObjectMapper mapper;

    @Before
    public void start() {
        server = new CollectorServer("localhost", 1234).start();
        mapper = new ObjectMapper();
        Repository.INSTANCE.clear();
    }

    @After
    public void shutdown() {
        server.stop();
        Repository.INSTANCE.clear();
    }

    @Test
    public void collect() throws Exception {
        {
            final Event[] events1 = new Event[2];
            {
                events1[0] = new Event();
                events1[0].setType("counter");
                events1[0].setData(buildData("counter1", "role1", Unit.UNARY.getName(), "client1", 6, 8, 7, 10, 12, 4, 64, 55));
            }
            {
                events1[1] = new Event();
                events1[1].setType("counter");
                events1[1].setData(buildData("counter2", "role2", Unit.UNARY.getName(), "client1", 8, 10, 3, 156, 75, 44, 4, 525));
            }
            doPost(events1);
        }

        {
            final Event[] events2 = new Event[2];
            {
                events2[0] = new Event();
                events2[0].setType("counter");
                events2[0].setData(buildData("counter1", "role1", Unit.UNARY.getName(), "client2", 7, 64, 78, 190, 612, 46, 654, 5));
            }
            {
                events2[1] = new Event();
                events2[1].setType("counter");
                events2[1].setData(buildData("counter2", "role2", Unit.UNARY.getName(), "client2", 84, 10978, 3869, 1586, 715, 474, 44, 65));
            }
            doPost(events2);
        }

        final CollectorCounterStore store = CollectorCounterStore.class.cast(Configuration.getInstance(CounterDataStore.class));
        final Counter counter1 = store.getOrCreateCounter(new Counter.Key(new Role("role1", Unit.UNARY), "counter1"));
        final Counter counter1Client1 = store.getOrCreateCounter(new Counter.Key(new Role("role1", Unit.UNARY), "counter1"), "client1");
        final Counter counter1Client2 = store.getOrCreateCounter(new Counter.Key(new Role("role1", Unit.UNARY), "counter1"), "client2");
        assertCounter(counter1, 200, 4, 612, 3.59, 12.24785, 150.01005, 718);
        assertCounter(counter1Client1, 10, 4, 12, 8, 2.64575, 7, 64);
        assertCounter(counter1Client2, 190, 46, 612, 64, 8.83176, 78, 654);
    }

    private void doPost(final Event[] events) throws Exception {
        final URL url = new URL("http://localhost:" + server.getPort());

        final HttpURLConnection connection = HttpURLConnection.class.cast(url.openConnection());
        connection.setRequestMethod("POST");
        connection.setUseCaches(false);
        connection.setDoInput(true);
        connection.setDoOutput(true);

        final StringWriter writer = new StringWriter();
        mapper.writeValue(writer, events);

        try {
            final OutputStream output = connection.getOutputStream();
            try {
                output.write(writer.toString().getBytes());
                output.flush();

                final int status = connection.getResponseCode();
                if (status / 100 != 2) {
                    throw new IOException("Status = " + status);
                }
            } finally {
                if (output != null) {
                    output.close();
                }
            }
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private static Map<String, Object> buildData(final String name, final String role, final String unit,
                                          final String marker, final int concurrency,
                                          final double mean, final double variance, final long n,
                                          final double max, final double min, final double sum,
                                          final double m2) {
        final Map<String, Object> data = new HashMap<String, Object>();
        data.put("name", name);
        data.put("role", role);
        data.put("unit", unit);
        data.put("marker", marker);
        data.put("concurrency", concurrency);
        data.put("min", min);
        data.put("mean", mean);
        data.put("max", max);
        data.put("variance", variance);
        data.put("hits", n);
        data.put("sum", sum);
        data.put("m2", m2);
        return data;
    }

    private static void assertCounter(final Counter counter, final int n, final double min, final double max,
                                      final double mean, final double stdDev,
                                      final double variance, final double sum) {
        assertEquals(n, counter.getHits());
        assertEquals(min, counter.getMin(), 0.);
        assertEquals(max, counter.getMax(), 0.);
        assertEquals(mean, counter.getMean(), 0.);
        assertEquals(stdDev, counter.getStandardDeviation(), 0.0001);
        assertEquals(variance, counter.getVariance(), 0.0001);
        assertEquals(sum, counter.getSum(), 0.);
    }
}
