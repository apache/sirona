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
package org.apache.sirona.hazelcast.plugin.gui;

import com.gargoylesoftware.htmlunit.NicelyResynchronizingAjaxController;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebClient;
import org.apache.catalina.startup.Constants;
import org.apache.sirona.Role;
import org.apache.sirona.counters.Unit;
import org.apache.sirona.gauges.Gauge;
import org.apache.sirona.plugin.hazelcast.gui.HazelcastPlugin;
import org.apache.sirona.reporting.web.plugin.api.Plugin;
import org.apache.sirona.reporting.web.registration.SironaReportingInitializer;
import org.apache.sirona.repositories.Repository;
import org.apache.sirona.web.lifecycle.SironaLifecycle;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.servlet.ServletContainerInitializer;
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.CountDownLatch;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.junit.Assert.assertThat;

@RunWith(Arquillian.class)
public class HazelcastGuiTest {
    static {
        System.setProperty(Constants.DEFAULT_JARS_TO_SKIP, "a*,c*,d*,e*,g*,h*,i*,j*,l*,m*,n*,p*,r*,sa*,se*,sh*,su*,t*,v*,w*,x*,z*");
    }

    private static Gauge gauge1;
    private static Gauge gauge2;

    @Deployment(testable = false)
    public static Archive<?> war() {
        return ShrinkWrap.create(WebArchive.class, "sirona-hazelcast.war")
            .addAsLibraries(
                ShrinkWrap.create(JavaArchive.class, "sci.jar")
                    .addAsServiceProvider(ServletContainerInitializer.class, SironaReportingInitializer.class)
                    .addAsServiceProvider(Plugin.class, HazelcastPlugin.class)
                    .addClass(SironaLifecycle.class));
    }

    @ArquillianResource
    protected URL base;

    protected <P extends Page> P page(final String path) throws IOException {
        final WebClient webClient = new WebClient();
        webClient.getOptions().setJavaScriptEnabled(false);
        webClient.getOptions().setCssEnabled(false);
        webClient.getOptions().setAppletEnabled(false);
        webClient.setAjaxController(new NicelyResynchronizingAjaxController());
        return webClient.getPage(base.toExternalForm() + "sirona/" + path);
    }

    @BeforeClass
    public static void feedSomeHazelcastDate() throws InterruptedException {
        final Role role1 = new Role("hazelcast-members-cluster", Unit.UNARY);
        final CountDownLatch latch1 = new CountDownLatch(1);
        gauge1 = new Gauge() {
            @Override
            public Role role() {
                return role1;
            }

            @Override
            public double value() {
                try {
                    return 2;
                } finally {
                    latch1.countDown();
                }
            }
        };

        final Role role2 = new Role("hazelcast-partitions-cluster", Unit.UNARY);
        final CountDownLatch latch2 = new CountDownLatch(1);
        gauge2 = new Gauge() {
            @Override
            public Role role() {
                return role2;
            }

            @Override
            public double value() {
                try {
                    return 3;
                } finally {
                    latch2.countDown();
                }
            }
        };
        Repository.INSTANCE.reset();
        Repository.INSTANCE.addGauge(gauge1);
        Repository.INSTANCE.addGauge(gauge2);
        latch1.await();
        latch2.await();
    }

    @AfterClass
    public static void reset() {
        Repository.INSTANCE.stopGauge(gauge1);
        Repository.INSTANCE.stopGauge(gauge2);
        Repository.INSTANCE.reset();
    }

    @Test
    public void sironaHome() throws IOException {
        assertThat(page("").getWebResponse().getContentAsString(),
            containsString("<a href=\"/sirona-hazelcast/sirona/hazelcast\">Hazelcast</a>"));
    }

    @Test
    public void hazelcastPluginHome() throws IOException {
        final String hazelcast = page("hazelcast").getWebResponse().getContentAsString();
        assertThat(hazelcast, containsString("You can choose to see"));
    }

    @Test
    public void hazelcastPluginMembers() throws IOException {
        final String hazelcast = page("hazelcast/members").getWebResponse().getContentAsString();
        assertThat(hazelcast, containsString("<div id=\"hazelcast-members-cluster-graph\" class=\"plot\">"));
        assertThat(hazelcast, containsString("Sirona.initGraph(\"/sirona-hazelcast/sirona\", \"hazelcast\", \"hazelcast-members-cluster\", options);"));
    }

    @Test
    public void hazelcastPluginPartitions() throws IOException {
        final String hazelcast = page("hazelcast/partitions").getWebResponse().getContentAsString();
        assertThat(hazelcast, containsString("<div id=\"hazelcast-partitions-cluster-graph\" class=\"plot\">"));
        assertThat(hazelcast, containsString("Sirona.initGraph(\"/sirona-hazelcast/sirona\", \"hazelcast\", \"hazelcast-partitions-cluster\", options);"));
    }

    @Test
    public void gaugeJsonHome() throws IOException {
        final long end = System.currentTimeMillis();

        assertThat(page("hazelcast/hazelcast-members-cluster/0/" + end).getWebResponse().getContentAsString(),
            startsWith("[{\"label\":\"hazelcast-members-cluster\""));
        assertThat(page("hazelcast/hazelcast-partitions-cluster/0/" + end).getWebResponse().getContentAsString(),
            startsWith("[{\"label\":\"hazelcast-partitions-cluster\""));
    }
}
