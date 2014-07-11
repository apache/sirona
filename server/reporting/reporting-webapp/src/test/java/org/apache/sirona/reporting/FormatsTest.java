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
package org.apache.sirona.reporting;

import org.apache.sirona.Role;
import org.apache.sirona.counters.Counter;
import org.apache.sirona.reporting.web.handler.TemplateHelper;
import org.apache.sirona.reporting.web.plugin.api.Template;
import org.apache.sirona.reporting.web.plugin.report.format.CSVFormat;
import org.apache.sirona.reporting.web.plugin.report.format.Format;
import org.apache.sirona.reporting.web.template.Templates;
import org.apache.sirona.repositories.Repository;
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
        Repository.INSTANCE.clearCounters();
        Templates.init("", "");

        final Counter counter = Repository.INSTANCE.getCounter(new Counter.Key(Role.FAILURES, "RendererTest"));
        counter.updateConcurrency(1);
        counter.add(1.);
    }

    @AfterClass
    public static void clear() {
        Repository.INSTANCE.clearCounters();
    }

    @Test
    public void renderToXML() throws Exception {
        final StringWriter out = new StringWriter();
        final TemplateHelper helper = new TemplateHelper(new PrintWriter(out), Collections.<String, Object>emptyMap());
        final Template template = Format.Defaults.XML.render(Collections.<String, Object>emptyMap());
        helper.renderPlain(template.getTemplate(), template.getUserParams());

        assertEquals("<?xml version=\"1.0\"?> <repository> " +
            "<counter name=\"RendererTest\" role=\"failures\" unit=\"u\" Hits=\"1.0\" Max=\"1.0\" Mean=\"1.0\" Min=\"1.0\" " +
            "StandardDeviation=\"0.0\" Sum=\"1.0\" Variance=\"0.0\" Value=\"1.0\" Concurrency=\"0.0\" MaxConcurrency=\"1.0\" />" +
            " </repository>", inline(out));
    }

    @Test
    public void renderToJSON() throws Exception {
        final StringWriter out = new StringWriter();
        final TemplateHelper helper = new TemplateHelper(new PrintWriter(out), Collections.<String, Object>emptyMap());
        final Template template = Format.Defaults.JSON.render(Collections.<String, Object>emptyMap());
        helper.renderPlain(template.getTemplate(), template.getUserParams());

        assertEquals("{\"counters\":[" +
            " {\"name\":\"RendererTest\", \"role\":\"failures\",\"unit\":\"u\",\"Hits\":\"1.0\",\"Max\":\"1.0\",\"Mean\":\"1.0\",\"Min\":\"1.0\"," +
            "\"StandardDeviation\":\"0.0\",\"Sum\":\"1.0\",\"Variance\":\"0.0\"," +
            "\"Value\":\"1.0\",\"Concurrency\":\"0.0\",\"MaxConcurrency\":\"1.0\"} ]}", inline(out));
    }

    @Test
    public void renderToCSV() throws Exception {
        final StringWriter out = new StringWriter();
        final TemplateHelper helper = new TemplateHelper(new PrintWriter(out), Collections.<String, Object>emptyMap());
        final Template template = Format.Defaults.CSV.render(Collections.<String, Object>emptyMap());
        helper.renderPlain(template.getTemplate(), template.getUserParams());

        assertEquals(CSVFormat.HEADER +
            "RendererTest;failures (u);1.0;1.0;1.0;1.0;0.0;1.0;0.0;1.0;0.0;1.0\n",
            out.toString());
    }

    private static String inline(StringWriter out) {
        return out.toString().replace("\r\n", " ").replace("\n", " ").replaceAll(" +", " ").replace("\t", "").trim();
    }
}
