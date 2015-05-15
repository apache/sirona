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

import com.gargoylesoftware.htmlunit.TextPage;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.junit.Test;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertThat;

public class GaugesTest extends SironaReportingTestBase {
    private static final String CPU_GAUGE_ENCODED = "Q1BV";

    @Test
    public void checkGaugesAreListed() throws IOException {
        final HtmlPage page = page("gauges");
        final String content = page.getWebResponse().getContentAsString();
        assertThat(content, containsString("<a href=\"/sirona-test/sirona/gauges/" + CPU_GAUGE_ENCODED + "\">"));
        assertThat(content, containsString("CPU"));
    }

    @Test
    public void checkDetail() throws IOException {
        final HtmlPage page = page("gauges/" + CPU_GAUGE_ENCODED);
        assertThat(page.getWebResponse().getContentAsString(), containsString("id=\"" + CPU_GAUGE_ENCODED + "-graph\""));
    }

    @Test
    public void checkJsonDetail() throws IOException {
        final TextPage page = page("gauges/" + CPU_GAUGE_ENCODED + "/0/" + (System.currentTimeMillis() + 1000));
        final String json = page.getWebResponse().getContentAsString();
        assertThat(json, containsString("[{\"label\":\"CPU\",\"color\":"));
        assertThat(json, containsString("\"data\":"));
    }
}
