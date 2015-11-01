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

import org.apache.johnzon.mapper.Converter;
import org.apache.johnzon.mapper.Mapper;
import org.apache.johnzon.mapper.MapperBuilder;
import org.apache.sirona.Role;
import org.apache.sirona.configuration.ioc.IoCs;
import org.apache.sirona.counters.Counter;
import org.apache.sirona.counters.Unit;
import org.apache.sirona.reporting.web.plugin.api.MapBuilder;
import org.apache.sirona.repositories.Repository;
import org.apache.sirona.status.NodeStatus;
import org.apache.sirona.status.Status;
import org.apache.sirona.store.counter.CollectorCounterStore;
import org.apache.sirona.store.gauge.CollectorGaugeDataStore;
import org.apache.sirona.store.gauge.GaugeValuesRequest;
import org.apache.sirona.store.status.CollectorNodeStatusDataStore;
import org.apache.sirona.store.status.NodeStatusDataStore;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class HttpCollectorTest {
    private CollectorServer server;
    private Mapper mapper;

    @Before
    public void start() {
        server = new CollectorServer("localhost", Integer.getInteger("collector.server.port", 1234)).start();
        mapper = new MapperBuilder().addConverter(Date.class, new Converter<Date>() {
            @Override
            public String toString(Date instance) {
                final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ENGLISH);
                simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
                return simpleDateFormat.format(instance);
            }

            @Override
            public Date fromString(final String text) {
                throw new UnsupportedOperationException(text);
            }
        }).build();
        Repository.INSTANCE.clearCounters();
    }

    @After
    public void shutdown() {
        server.stop();
        Repository.INSTANCE.clearCounters();
    }

    @Test
    public void collectCounter() throws Exception {
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

        final CollectorCounterStore store = IoCs.getInstance(CollectorCounterStore.class);
        final Counter counter1 = store.getOrCreateCounter(new Counter.Key(new Role("role1", Unit.UNARY), "counter1"));
        final Counter counter1Client1 = store.getOrCreateCounter(new Counter.Key(new Role("role1", Unit.UNARY), "counter1"), "client1");
        final Counter counter1Client2 = store.getOrCreateCounter(new Counter.Key(new Role("role1", Unit.UNARY), "counter1"), "client2");
        assertCounter(counter1, 200, 4, 612, 3.59, 12.24785, 150.01005, 718);
        assertCounter(counter1Client1, 10, 4, 12, 8, 2.64575, 7, 64);
        assertCounter(counter1Client2, 190, 46, 612, 64, 8.83176, 78, 654);
    }

    @Test
    public void collectGauges() throws Exception {
        final Date pushDate = new Date(); // we aggregated only if push was done on the exactly same date so ensuring it
        {
            final Event[] events1 = new Event[1];
            {
                events1[0] = new Event();
                events1[0].setTime(pushDate);
                events1[0].setType("gauge");
                events1[0].setData(new MapBuilder<String, Object>()
                    .set("role", "event-role")
                    .set("unit", Unit.UNARY.getName())
                    .set("value", 5)
                    .set("marker", "node1")
                    .build());
            }
            doPost(events1);
        }

        {
            final Event[] events2 = new Event[2];
            {
                events2[0] = new Event();
                events2[0].setTime(pushDate);
                events2[0].setType("gauge");
                events2[0].setData(new MapBuilder<String, Object>()
                    .set("role", "event-role")
                    .set("unit", Unit.UNARY.getName())
                    .set("value", 15)
                    .set("marker", "node2")
                    .build());
            }
            {
                events2[1] = new Event();
                events2[1].setTime(pushDate);
                events2[1].setType("gauge");
                events2[1].setData(new MapBuilder<String, Object>()
                    .set("role", "event2-role")
                    .set("unit", Unit.UNARY.getName())
                    .set("value", 25)
                    .set("marker", "node2")
                    .build());
            }
            doPost(events2);
        }

        final CollectorGaugeDataStore store = IoCs.getInstance(CollectorGaugeDataStore.class);
        final GaugeValuesRequest gaugeValuesRequest = new GaugeValuesRequest(0, System.currentTimeMillis() + 1000, new Role("event-role", Unit.UNARY));
        final Map<Long, Double> aggregated = store.getGaugeValues(gaugeValuesRequest);
        final Map<Long, Double> node1 = store.getGaugeValues(gaugeValuesRequest, "node1");
        final Map<Long, Double> node2 = store.getGaugeValues(gaugeValuesRequest, "node2");
        assertEquals(1, aggregated.size());
        assertTrue(aggregated.containsValue(20.));
        assertTrue(node1.containsValue(5.));
        assertTrue(node2.containsValue(15.));
    }

    @Test
    public void collectStatus() throws Exception {
        final Date pushDate = new Date(); // we aggregated only if push was done on the exactly same date so ensuring it
        {
            final Event[] events1 = new Event[1];
            {
                events1[0] = new Event();
                events1[0].setTime(pushDate);
                events1[0].setType("validation");
                events1[0].setData(new MapBuilder<String, Object>()
                    .set("message", "good")
                    .set("status", Status.OK)
                    .set("name", "validation1")
                    .set("marker", "node1")
                    .build());
            }
            doPost(events1);
        }

        {
            final Event[] events2 = new Event[1];
            {
                events2[0] = new Event();
                events2[0].setTime(pushDate);
                events2[0].setType("validation");
                events2[0].setData(new MapBuilder<String, Object>()
                    .set("message", "bad")
                    .set("status", Status.KO)
                    .set("name", "validation3")
                    .set("marker", "node2")
                    .build());
            }
            doPost(events2);
        }

        final NodeStatusDataStore store = IoCs.getInstance(CollectorNodeStatusDataStore.class);
        final Map<String,NodeStatus> statuses = store.statuses();
        assertEquals(2, statuses.size());
        assertEquals(Status.OK, statuses.get("node1").getStatus());
        assertEquals(1, statuses.get("node1").getResults().length);
        assertEquals(Status.KO, statuses.get("node2").getStatus());
        assertEquals(1, statuses.get("node2").getResults().length);
    }

    private void doPost(final Event[] events) throws Exception {
        final URL url = new URL("http://localhost:" + server.getPort());

        final HttpURLConnection connection = HttpURLConnection.class.cast(url.openConnection());
        connection.setRequestMethod("POST");
        connection.setUseCaches(false);
        connection.setDoInput(true);
        connection.setDoOutput(true);

        final StringWriter writer = new StringWriter();
        mapper.writeArray(events, writer);

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
