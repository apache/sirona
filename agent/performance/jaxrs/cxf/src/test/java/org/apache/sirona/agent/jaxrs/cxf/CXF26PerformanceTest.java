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
package org.apache.sirona.agent.jaxrs.cxf;

import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebClient;
import org.apache.catalina.startup.Constants;
import org.apache.cxf.jaxrs.servlet.CXFNonSpringJaxrsServlet;
import org.apache.sirona.agent.jaxrs.cxf.service.SimpleService;
import org.apache.sirona.counters.Counter;
import org.apache.sirona.repositories.Repository;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.OverProtocol;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.descriptor.api.Descriptors;
import org.jboss.shrinkwrap.descriptor.api.webapp30.WebAppDescriptor;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import static org.junit.Assert.assertEquals;

@RunWith(Arquillian.class)
public class CXF26PerformanceTest {
    static { // to start faster
        System.setProperty(Constants.DEFAULT_JARS_TO_SKIP, "a*,c*,d*,e*,g*,h*,i*,j*,k*,l*,m*,n*,o*,p*,q*,r*,s*,t*,u*,v*,w*,x*,z*");
    }

    @Deployment(testable = false)
    @OverProtocol("Servlet 2.5") // to use a custom web.xml
    public static Archive<?> war() {
        return ShrinkWrap.create(WebArchive.class, "sirona-cxf26.war")
            .addAsWebInfResource(new StringAsset(
                Descriptors.create(WebAppDescriptor.class)
                    .metadataComplete(true)
                    .createServlet()
                        .servletName(CXFNonSpringJaxrsServlet.class.getSimpleName())
                        .servletClass(CXFNonSpringJaxrsServlet.class.getName())
                        .createInitParam()
                            .paramName("jaxrs.serviceClasses").paramValue(SimpleService.class.getName())
                        .up()
                        .createInitParam()
                            .paramName("jaxrs.providers").paramValue(CxfJaxRsPerformanceHandler.class.getName())
                        .up()
                    .up()
                    .createServletMapping()
                        .servletName(CXFNonSpringJaxrsServlet.class.getSimpleName())
                        .urlPattern("/api/*")
                    .up()
                    .exportAsString()
            ), "web.xml");
    }

    @ArquillianResource
    private URL base;

    @Before
    @After
    public void resetCounters() {
        Repository.INSTANCE.clearCounters();
    }

    @Test
    public void onePathSegment() throws IOException {
        final Page page = new WebClient().getPage(base.toExternalForm() + "api/service/simple?ignored=true");
        assertEquals(HttpURLConnection.HTTP_OK, page.getWebResponse().getStatusCode());
        assertEquals("simple", page.getWebResponse().getContentAsString());

        assertEquals(1, Repository.INSTANCE.counters().size());

        final String name = "GET-/sirona-cxf26/api/service/{name}";
        assertEquals(name, Repository.INSTANCE.counters().iterator().next().getKey().getName());
        final Counter hitCounter = Repository.INSTANCE.getCounter(new Counter.Key(CxfJaxRsPerformanceHandler.ROLE, name));
        assertEquals(1, hitCounter.getHits());
    }

    @Test
    public void twoPathSegments() throws IOException {
        final Page page = new WebClient().getPage(base.toExternalForm() + "api/service/foo/bar");
        assertEquals(HttpURLConnection.HTTP_OK, page.getWebResponse().getStatusCode());
        assertEquals("foobar", page.getWebResponse().getContentAsString());

        assertEquals(1, Repository.INSTANCE.counters().size());

        final String name = "GET-/sirona-cxf26/api/service/{a}/{b}";
        assertEquals(name, Repository.INSTANCE.counters().iterator().next().getKey().getName());
        final Counter hitCounter = Repository.INSTANCE.getCounter(new Counter.Key(CxfJaxRsPerformanceHandler.ROLE, name));
        assertEquals(1, hitCounter.getHits());
    }

    @Test
    public void ambiguousPathSegments() throws IOException {
        final Page page = new WebClient().getPage(base.toExternalForm() + "api/service/foo/foo");
        assertEquals(HttpURLConnection.HTTP_OK, page.getWebResponse().getStatusCode());
        assertEquals("foofoo", page.getWebResponse().getContentAsString());

        assertEquals(1, Repository.INSTANCE.counters().size());

        final String name = "GET-/sirona-cxf26/api/service/{a}/{b}";
        assertEquals(name, Repository.INSTANCE.counters().iterator().next().getKey().getName());
        final Counter hitCounter = Repository.INSTANCE.getCounter(new Counter.Key(CxfJaxRsPerformanceHandler.ROLE, name));
        assertEquals(1, hitCounter.getHits());
    }
}
