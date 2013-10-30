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

import static com.gargoylesoftware.htmlunit.WebAssert.assertElementPresent;
import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertThat;

public class JMXTest extends SironaReportingTestBase {
    @Test
    public void checkSomeMBeanIsAvailable() throws IOException {
        final String classLoadingMBeanId = "amF2YS5sYW5nOnR5cGU9Q2xhc3NMb2FkaW5n";  // java.lang:ClassLoading

        final HtmlPage page = page("jmx");
        assertElementPresent(page, classLoadingMBeanId);

        // we could click on the link but it would mandates to activate js in the WebClient (see parent)
        // this is not as easy as setting the boolean because of dev environment (proxy, etc...)
        final TextPage detail = page("jmx/" + classLoadingMBeanId);
        final String detailAsStr = detail.getWebResponse().getContentAsString();
        assertThat(detailAsStr, containsString("LoadedClassCount"));
        assertThat(detailAsStr, containsString("sun.management.ClassLoadingImpl"));
    }
}
