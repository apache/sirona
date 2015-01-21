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
package org.apache.sirona.test.web;

import com.gargoylesoftware.htmlunit.TextPage;
import org.apache.catalina.startup.Constants;
import org.apache.sirona.Role;
import org.apache.sirona.counters.Counter;
import org.apache.sirona.counters.Unit;
import org.apache.sirona.repositories.Repository;
import org.apache.sirona.web.registration.WebSironaInitializer;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.servlet.ServletContainerInitializer;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

import static org.apache.sirona.test.web.Clients.newClient;
import static org.junit.Assert.assertEquals;

@RunWith(Arquillian.class)
public class RequestPerformanceTest {
    static { // to start faster
        System.setProperty(Constants.DEFAULT_JARS_TO_SKIP, "a*,c*,d*,e*,g*,h*,i*,j*,l*,m*,n*,p*,r*,sa*,se*,sh*,su*,t*,v*,w*,x*,z*");
    }

    @Deployment(testable = false)
    public static Archive<?> war() { // note: we depend on tomcat embedded adapter since we don't add dependencies + we use the fact Repository.INSTANCE is in the same JVM
        return ShrinkWrap.create(WebArchive.class, "sirona-test.war")
            .addPackages(true, "org.apache.sirona.web")
            .addClasses(HittableServlet.class)
            .addAsLibraries(
                ShrinkWrap.create(JavaArchive.class, "sci.jar")
                    .addAsServiceProvider(ServletContainerInitializer.class, WebSironaInitializer.class));
    }

    @ArquillianResource
    private URL base;

    @Before
    public void resetCounters() {
        Repository.INSTANCE.clearCounters();
    }

    @Test
    public void monitorRequest() throws IOException {
        final TextPage page = newClient().getPage(base.toExternalForm() + "hit");
        assertEquals(HttpURLConnection.HTTP_OK, page.getWebResponse().getStatusCode());

        final Counter hitCounter = Repository.INSTANCE.getCounter(new Counter.Key(Role.WEB, "/sirona-test/hit"));
        assertEquals(1, hitCounter.getHits());
    }

    @Test
    public void knownStatusIsMonitored() throws IOException, InterruptedException {
        final Role role = new Role("/sirona-test-HTTP-200", Unit.UNARY);
        final int before = statusGaugeSum(role);
        final TextPage page = newClient().getPage(base.toExternalForm() + "hit");
        assertEquals(HttpURLConnection.HTTP_OK, page.getWebResponse().getStatusCode());
        Thread.sleep(1000);
        assertEquals("" + Repository.INSTANCE.getGaugeValues(0, System.currentTimeMillis() + 1000, role), 1, statusGaugeSum(role) - before);
    }

    @Test
    public void unknownStatusIsIgnored() throws IOException, InterruptedException {
        final TextPage page = newClient().getPage(base.toExternalForm() + "hit?status=4567");
        assertEquals(4567, page.getWebResponse().getStatusCode());
        Thread.sleep(1000);
        assertEquals(0, statusGaugeSum(new Role("/sirona-test-HTTP-4567", Unit.UNARY)));
    }

    private static int statusGaugeSum(final Role role) {
        return sum(Repository.INSTANCE.getGaugeValues(0, System.currentTimeMillis() + 1000, role));
    }

    private static int sum(final Map<Long, Double> gaugeValues) {
        int sum = 0;
        for (final Double d : gaugeValues.values()) {
            sum += d.intValue();
        }
        return sum;
    }
}
