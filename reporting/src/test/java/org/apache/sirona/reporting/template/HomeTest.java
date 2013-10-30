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

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertThat;
import org.junit.Test;

import java.io.IOException;

public class HomeTest extends SironaReportingTestBase {
    @Test
    public void checkHomeShowsPlugins() throws IOException {
        final HtmlPage page = page("");
        final String plugins = page.getElementById("plugins").asText();
        assertThat(plugins, containsString("Home"));
        assertThat(plugins, containsString("Report"));
        assertThat(plugins, containsString("JMX"));
        assertThat(plugins, containsString("JVM"));
        assertThat(plugins, containsString("JTA"));
        assertThat(plugins, containsString("Threads"));
        assertThat(plugins, containsString("Web"));
    }
}
