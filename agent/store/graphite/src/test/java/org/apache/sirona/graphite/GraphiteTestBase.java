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

import org.apache.sirona.configuration.ioc.IoCs;
import org.apache.sirona.gauges.Gauge;
import org.apache.sirona.graphite.server.GraphiteMockServer;
import org.apache.sirona.repositories.Repository;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

import java.io.IOException;
import java.util.Collection;

public abstract class GraphiteTestBase {
    private GraphiteMockServer server;
    private Gauge.LoaderHelper gauges;

    @Before
    public void startGraphite() throws IOException, InterruptedException {
        server = new GraphiteMockServer(Integer.getInteger("collector.server.port", 1234)).start();
        if (System.getProperty("org.apache.sirona.graphite.GraphiteBuilder.port", "").isEmpty()) {
            System.setProperty("org.apache.sirona.graphite.GraphiteBuilder.port", Integer.toString(server.getPort()));
        }
        Thread.sleep(200); // make sure it gets time to restart between tests
        server.clear();
        Repository.INSTANCE.clearCounters();
        gauges = new Gauge.LoaderHelper(false);
    }

    @After
    public void shutdownGraphite() throws IOException {
        gauges.destroy();
        server.stop();
        Repository.INSTANCE.clearCounters();
    }

    @AfterClass
    public static void shutdownSirona() throws IOException {
        IoCs.shutdown();
    }

    protected Collection<String> messages() {
        return server.getMessages();
    }
}
