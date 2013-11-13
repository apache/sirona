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
package org.apache.sirona.reporting.template;

import com.gargoylesoftware.htmlunit.WebAssert;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.apache.sirona.Role;
import org.apache.sirona.counters.Counter;
import org.apache.sirona.counters.Unit;
import org.apache.sirona.reporting.web.plugin.report.format.MapFormat;
import org.apache.sirona.repositories.Repository;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.util.Locale;

import static org.junit.Assert.assertEquals;

public class ReportTest extends SironaReportingTestBase {
    private Counter.Key key;
    
    private static Locale oldLocale;
    private static final String lineSeparator = System.getProperty("line.separator");

    @Before
    public void init() {
        Repository.INSTANCE.clearCounters();
        key = new Counter.Key(new Role("role", Unit.UNARY), "counter");
        Repository.INSTANCE.getCounter(key).add(55);
    }

    @After
    public void reset() {
        Repository.INSTANCE.clearCounters();
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
    public void generalList() throws IOException {
        final WebClient client = newClient();
        final HtmlPage page = client.getPage(base.toExternalForm() + "sirona/report");
        WebAssert.assertElementPresent(page, "report-table");

        final String text = page.getElementById("report-table").asText();
        assertEquals("Counter\tRole\tHits\tMax\tMean\tMin\tStandardDeviation\tSum\tVariance\tValue\tConcurrency\tMaxConcurrency" + lineSeparator +
            "counter\trole(u)\t1.00\t55.00\t55.00\t55.00\t0.00\t55.00\t0.00\t55.00\t0.00\t0.00", text.replace(" ", ""));
    }

    @Test
    public void detail() throws IOException {
        final WebClient client = newClient();
        final HtmlPage page = client.getPage(base.toExternalForm() + "sirona/report/counter/" + MapFormat.generateCounterKeyString(key));
        WebAssert.assertElementPresent(page, "counter");

        final String text = page.getElementById("counter").asText();
        assertEquals("Counter\tRole\tHits\tMax\tMean\tMin\tStandardDeviation\tSum\tVariance\tValue\tConcurrency\tMaxConcurrency" + lineSeparator +
            "counter\trole (u)\t1.00\t55.00\t55.00\t55.00\t0.00\t55.00\t0.00\t55.00\t0.00\t0.00", text);
    }
}
