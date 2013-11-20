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
package org.apache.sirona.cube;

import org.apache.sirona.Role;
import org.apache.sirona.counters.Counter;
import org.apache.sirona.gauges.Gauge;
import org.apache.sirona.repositories.Repository;
import org.apache.sirona.util.Localhosts;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class CubeDataStoreTest {
    private CubeServer server;
    private Gauge.LoaderHelper gauges;
    private static Locale oldLocale;

    @Before
    public void startCube() throws IOException {
        server = new CubeServer("localhost", Integer.getInteger("collector.server.port", 1234)).start();
        Repository.INSTANCE.clearCounters();
        gauges = new Gauge.LoaderHelper(false);
    }

    @After
    public void stopCube() {
        gauges.destroy();
        Repository.INSTANCE.clearCounters();
        server.stop();
    }

    @BeforeClass
    public static void setDefaultLocale() {
        oldLocale = Locale.getDefault();
        Locale.setDefault(Locale.ENGLISH);
    }

    @AfterClass
    public static void restoreLocale() {
        Locale.setDefault(oldLocale);
    }

    @Test
    public void store() throws InterruptedException, UnknownHostException {
        { // force some counter data
            final Counter counter = Repository.INSTANCE.getCounter(new Counter.Key(Role.PERFORMANCES, "test"));
            counter.add(1.4);
            counter.add(1.6);
            Thread.sleep(150);
            counter.add(2.3);
            counter.add(2.9);
            Thread.sleep(1500);
        }

        final String host = Localhosts.get();

        final Collection<String> messages = server.getMessages();
        final Collection<Double> gauges = new ArrayList<Double>(4);
        int counters = 0;
        String aCounterMessage = null;
        for (final String m : messages) {
            if (m.contains("\"type\": \"gauge\"")) {
                assertThat(m, containsString("\"role\":\"mock\""));
                assertThat(m, containsString("\"unit\":\"u\""));
                assertThat(m, containsString("\"marker\":\"" + host + "\""));

                final String valueStr = "value\":";
                final int start = m.indexOf(valueStr) + valueStr.length();
                gauges.add(Double.parseDouble(m.substring(start, indexOf(m, start))));
            } else if (m.contains("\"type\": \"counter\"")) {
                counters++;
                aCounterMessage = m;
            }
        }

        assertTrue(gauges.contains(0.));
        assertTrue(gauges.contains(1.));
        assertTrue(gauges.contains(2.));

        assertTrue(counters >= 3);
        assertNotNull(aCounterMessage);
        assertThat(aCounterMessage, containsString("name"));
        assertThat(aCounterMessage, containsString("role"));
        assertThat(aCounterMessage, containsString("hits"));
        assertThat(aCounterMessage, containsString("sum"));
        assertThat(aCounterMessage, containsString("concurrency"));
        assertThat(aCounterMessage, containsString("marker"));
    }

    private static int indexOf(String m, int start) {
        final int i = m.indexOf(',', start + 1);
        if (i == -1) {
            return m.indexOf('}', start + 1);
        }
        return i;
    }
}
