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
import org.apache.sirona.configuration.ioc.IoCs;
import org.apache.sirona.repositories.Repository;
import org.apache.sirona.status.NodeStatus;
import org.apache.sirona.status.Status;
import org.apache.sirona.status.ValidationResult;
import org.apache.sirona.store.status.NodeStatusDataStore;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.util.Date;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertThat;

public class StatusTest extends SironaReportingTestBase {
    @BeforeClass
    public static void addStatus() {
        IoCs.findOrCreateInstance(Repository.class);
        IoCs.getInstance(NodeStatusDataStore.class).statuses()
            .put("node1",
                new NodeStatus(
                    new ValidationResult[] { new ValidationResult("validation #1", Status.OK, "all is fine") },
                    new Date()));
    }

    @Test
    public void checkNodesAreListed() throws IOException {
        final HtmlPage page = page("status");
        assertThat(page.getWebResponse().getContentAsString(), containsString("/status/node1"));
    }

    @Test
    public void checkDetail() throws IOException {
        final HtmlPage page = page("status/node1");
        assertThat(page.getWebResponse().getContentAsString(), containsString("Global status"));
        assertThat(page.getWebResponse().getContentAsString(), containsString(": OK"));
        assertThat(page.getWebResponse().getContentAsString(), containsString("validation #1"));
        assertThat(page.getWebResponse().getContentAsString(), containsString("all is fine"));
    }
}
