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
package org.apache.commons.monitoring.graphite;

import org.apache.commons.monitoring.gauges.Gauge;
import org.apache.commons.monitoring.graphite.lifecycle.GraphiteLifecycle;
import org.apache.commons.monitoring.graphite.server.GraphiteMockServer;
import org.apache.commons.monitoring.repositories.Repository;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

import java.io.IOException;
import java.util.Collection;

public abstract class GraphiteTestBase {
    private GraphiteMockServer server;
    private Gauge.LoaderHelper gauges;

    @BeforeClass
    @AfterClass
    public static void reset() {
        Repository.INSTANCE.clear();
    }

    @Before
    public void startGraphite() throws IOException {
        server = new GraphiteMockServer(1234).start();
        gauges = new Gauge.LoaderHelper(false);
    }

    @After
    public void shutdownGraphite() throws IOException {
        new GraphiteLifecycle().contextDestroyed(null);
        gauges.destroy();
        server.stop();
    }

    protected Collection<String> messages() {
        return server.getMessages();
    }
}
