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

import org.apache.sirona.configuration.Configuration;
import org.apache.sirona.gauges.Gauge;
import org.apache.sirona.graphite.server.GraphiteMockServer;
import org.apache.sirona.repositories.Repository;
import org.junit.After;
import org.junit.Before;

import java.io.IOException;
import java.util.Collection;

public abstract class GraphiteTestBase {
    private GraphiteMockServer server;
    private Gauge.LoaderHelper gauges;

    @Before
    public void startGraphite() throws IOException {
        Repository.INSTANCE.clearCounters();
        server = new GraphiteMockServer(1234).start();
        gauges = new Gauge.LoaderHelper(false);
    }

    @After
    public void shutdownGraphite() throws IOException {
        Configuration.shutdown();
        gauges.destroy();
        server.stop();
        Repository.INSTANCE.clearCounters();
    }

    protected Collection<String> messages() {
        return server.getMessages();
    }
}
