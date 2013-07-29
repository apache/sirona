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
import org.apache.commons.monitoring.Visitor;
import org.apache.commons.monitoring.monitors.Monitor;
import org.apache.commons.monitoring.reporting.format.CSVFormat;
import org.apache.commons.monitoring.reporting.format.Format;
import org.apache.commons.monitoring.reporting.format.FormattingVisitor;
import org.apache.commons.monitoring.reporting.format.RoleFilter;
import org.apache.commons.monitoring.repositories.DefaultRepository;
import org.apache.commons.monitoring.repositories.Repository;
import org.custommonkey.xmlunit.XMLAssert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;

import static org.junit.Assert.assertEquals;

/**
 * @author <a href="mailto:nicolas@apache.org">Nicolas De Loof</a>
 */
public class FormattingVisitorTest {
    private static Repository repository;

    @BeforeClass
    public static void setup() {
        repository = new DefaultRepository();

        final Monitor monitor = repository.getMonitor(new Monitor.Key("RendererTest", "unit"));
        monitor.updateConcurrency(1);
        monitor.getCounter(Role.FAILURES).add(1.);
    }

    @Test
    public void renderToXML()
        throws Exception {
        final StringWriter out = new StringWriter();
        final Visitor v = new FormattingVisitor(Format.Defaults.XML_PRETTY, new PrintWriter(out), RoleFilter.Defaults.FAILURES);
        repository.accept(v);

        final Reader expected = new InputStreamReader(getClass().getResourceAsStream("RendererTest.xml"));
        XMLAssert.assertXMLEqual(expected, new StringReader(out.toString()));
    }

    @Test
    public void renderToJSON()
        throws Exception {
        final StringWriter out = new StringWriter();
        final Visitor v = new FormattingVisitor(Format.Defaults.JSON_PRETTY, new PrintWriter(out), RoleFilter.Defaults.FAILURES);
        repository.accept(v);

        assertEquals("{\n" +
            "  \"RendererTest\":{\n" +
            "    \"category\": \"unit\",\n" +
            "    \"failures\":{\"type\":\"counter\",\"Hits\":\"1.0\",\"Max\":\"1.0\",\"Mean\":\"1.0\",\"Min\":\"1.0\",\"StandardDeviation\":\"0.0\",\"Sum\":\"1.0\",\"SumOfLogs\":\"0.0\",\"SumOfSquares\":\"0.0\",\"Variance\":\"0.0\",\"GeometricMean\":\"1.0\",\"Value\":\"1.0\",\"MaxConcurrency\":\"1.0\"}\n" +
            "  }\n" +
            "}", out.toString());
    }

    @Test
    public void renderToCSV()
        throws Exception {
        final StringWriter out = new StringWriter();
        final Visitor v = new FormattingVisitor(Format.Defaults.CSV, new PrintWriter(out), RoleFilter.Defaults.FAILURES);
        repository.accept(v);

        assertEquals(CSVFormat.HEADER +
            "RendererTest;unit;failures;1.0;1.0;1.0;1.0;0.0;1.0;0.0;0.0;0.0;1.0;1.0;1.0\n",
            out.toString());
    }
}
