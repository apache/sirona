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

package org.apache.commons.monitoring.reporting;

import org.apache.commons.monitoring.Role;
import org.apache.commons.monitoring.monitors.Monitor;
import org.apache.commons.monitoring.reporting.web.handler.format.CSVFormat;
import org.apache.commons.monitoring.reporting.web.handler.format.Format;
import org.apache.commons.monitoring.repositories.Repository;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collections;

import static org.junit.Assert.assertEquals;

public class FormatsTest {
    @BeforeClass
    public static void setup() {
        Repository.INSTANCE.clear();

        final Monitor monitor = Repository.INSTANCE.getMonitor(new Monitor.Key("RendererTest", "unit"));
        monitor.updateConcurrency(1);
        monitor.getCounter(Role.FAILURES).add(1.);
    }

    @AfterClass
    public static void clear() {
        Repository.INSTANCE.clear();
    }

    @Test
    public void renderToXML() throws Exception {
        final StringWriter out = new StringWriter();
        Format.Defaults.XML.render(new PrintWriter(out), Collections.<String, Object>emptyMap());

        assertEquals("<repository>" +
            "<monitor name=\"RendererTest\" category=\"unit\">" +
            "<counter role=\"failures\" unit=\"u\" Hits=\"1.0\" Max=\"1.0\" Mean=\"1.0\" Min=\"1.0\" StandardDeviation=\"0.0\" Sum=\"1.0\" " +
            "SumOfLogs=\"0.0\" SumOfSquares=\"0.0\" Variance=\"0.0\" GeometricMean=\"1.0\" Value=\"1.0\" Concurrency=\"0.0\" MaxConcurrency=\"1.0\" />" +
            "</monitor>" +
            "</repository>".trim(), out.toString());
    }

    @Test
    public void renderToJSON() throws Exception {
        final StringWriter out = new StringWriter();
        Format.Defaults.JSON.render(new PrintWriter(out), Collections.<String, Object>emptyMap());

        assertEquals("{\"monitors\":[" +
            "{\"name\":\"RendererTest\",\"category\":\"unit\",\"counters\":[" +
            "{\"role\":\"failures\",\"unit\":\"u\",\"Hits\":\"1.0\",\"Max\":\"1.0\",\"Mean\":\"1.0\",\"Min\":\"1.0\"," +
            "\"StandardDeviation\":\"0.0\",\"Sum\":\"1.0\",\"SumOfLogs\":\"0.0\",\"SumOfSquares\":\"0.0\",\"Variance\":\"0.0\"," +
            "\"GeometricMean\":\"1.0\",\"Value\":\"1.0\",\"Concurrency\":\"0.0\",\"MaxConcurrency\":\"1.0\"}]}]}", out.toString());
    }

    @Test
    public void renderToCSV() throws Exception {
        final StringWriter out = new StringWriter();
        Format.Defaults.CSV.render(new PrintWriter(out), Collections.<String, Object>emptyMap());

        assertEquals(CSVFormat.HEADER +
            "RendererTest;unit;failures (u);1.0;1.0;1.0;1.0;0.0;1.0;0.0;0.0;0.0;1.0;1.0;0.0;1.0\n",
            out.toString());
    }
}
