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

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.net.URL;
import java.util.Locale;

import javax.servlet.ServletContainerInitializer;

import org.apache.catalina.startup.Constants;
import org.apache.sirona.Role;
import org.apache.sirona.counters.Counter;
import org.apache.sirona.counters.Unit;
import org.apache.sirona.reporting.web.plugin.Plugin;
import org.apache.sirona.reporting.web.plugin.report.ReportPlugin;
import org.apache.sirona.reporting.web.plugin.report.format.MapFormat;
import org.apache.sirona.reporting.web.registration.MonitoringReportingInitializer;
import org.apache.sirona.repositories.Repository;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.ClassLoaderAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.gargoylesoftware.htmlunit.WebAssert;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

@RunWith(Arquillian.class)
public class CounterDetailTemplateTest {
    @Deployment(testable = false)
    public static Archive<?> war() { // note: we depend on tomcat embedded adapter since we don't add dependencies + we use the fact Repository.INSTANCE is in the same JVM
        // we use this hack to init jars to skip since this method is triggered before any deployment
        // so ContextConfig is not yet loaded
        System.setProperty(Constants.DEFAULT_JARS_TO_SKIP, "a*,c*,d*,e*,g*,h*,i*,j*,l*,m*,n*,p*,r*,sa*,se*,sh*,su*,t*,v*,w*,x*,z*");

        // real impl of this method starts here
        return ShrinkWrap.create(WebArchive.class, "sirona-test.war")
            .addPackages(true, "org.apache.sirona.reporting.web")
            .addAsServiceProvider(Plugin.class, ReportPlugin.class)
            .addAsWebInfResource(new ClassLoaderAsset("templates/report/report.vm"), "classes/templates/report/report.vm")
            .addAsWebInfResource(new ClassLoaderAsset("templates/report/counter.vm"), "classes/templates/report/counter.vm")
            .addAsWebInfResource(new ClassLoaderAsset("templates/macro.vm"), "classes/templates/macro.vm")
            .addAsLibraries(
                ShrinkWrap.create(JavaArchive.class, "sci.jar") // bug in tomcat?
                    .addAsServiceProvider(ServletContainerInitializer.class, MonitoringReportingInitializer.class));
    }

    @ArquillianResource
    private URL base;

    private Counter.Key key;
    
    private static Locale oldLocale;
    private static final String lineSeparator = System.getProperty("line.separator");

    @Before
    public void init() {
        Repository.INSTANCE.clear();
        key = new Counter.Key(new Role("role", Unit.UNARY), "counter");
        Repository.INSTANCE.getCounter(key).add(55);
    }

    @After
    public void reset() {
        Repository.INSTANCE.clear();
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
        final HtmlPage page = client.getPage(base.toExternalForm() + "monitoring/report");
        WebAssert.assertElementPresent(page, "report-table");

        final String text = page.getElementById("report-table").asText();
        assertEquals("Counter\tRole\tHits\tMax\tMean\tMin\tStandardDeviation\tSum\tVariance\tValue\tConcurrency\tMaxConcurrency" + lineSeparator +
            "counter \t role (u) \t 1.00 \t 55.00 \t 55.00 \t 55.00 \t 0.00 \t 55.00 \t 0.00 \t 55.00 \t 0.00 \t 0.00", text);
    }

    @Test
    public void detail() throws IOException {
        final WebClient client = newClient();
        final HtmlPage page = client.getPage(base.toExternalForm() + "monitoring/report/counter/" + MapFormat.generateCounterKeyString(key));
        WebAssert.assertElementPresent(page, "counter");

        final String text = page.getElementById("counter").asText();
        assertEquals("Counter\tRole\tHits\tMax\tMean\tMin\tStandardDeviation\tSum\tVariance\tValue\tConcurrency\tMaxConcurrency" + lineSeparator +
            "counter\trole (u)\t1.00\t55.00\t55.00\t55.00\t0.00\t55.00\t0.00\t55.00\t0.00\t0.00", text);
    }

    private static WebClient newClient() {
        final WebClient webClient = new WebClient();
        webClient.getOptions().setJavaScriptEnabled(false);
        webClient.getOptions().setCssEnabled(false);
        webClient.getOptions().setAppletEnabled(false);
        return webClient;
    }
}
