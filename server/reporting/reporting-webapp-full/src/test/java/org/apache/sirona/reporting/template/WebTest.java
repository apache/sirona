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

import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.junit.Test;

import java.io.IOException;

import static com.gargoylesoftware.htmlunit.WebAssert.assertElementPresent;
import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class WebTest extends SironaReportingTestBase {
    @Test
    public void checkSessionsGraphIsPresent() throws IOException {
        final HtmlPage page = page("web");
        assertElementPresent(page, "sessions-graph");

        final String detailAsStr = page.getWebResponse().getContentAsString();
        assertThat(detailAsStr, containsString("Sessions"));
    }

    @Test
    public void checkJsonEndpoint() throws IOException {
        final String page = page("web/sessions/0/" + System.currentTimeMillis()).getWebResponse().getContentAsString().replace(" ", "");
        // we don't deploy in this tests sessions gauges so we have nothing
        assertTrue(page.contains("\"data\":[]"));
        assertTrue(page.contains("\"label\":\"sessions-/sirona-test\""));
    }
}
