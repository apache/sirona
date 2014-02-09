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

import org.apache.catalina.startup.Constants;
import org.apache.sirona.Role;
import org.apache.sirona.counters.Counter;
import org.apache.sirona.repositories.Repository;
import org.apache.sirona.web.lifecycle.LazyJspMonitoringFilterActivator;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.descriptor.api.Descriptors;
import org.jboss.shrinkwrap.descriptor.api.webapp30.WebAppDescriptor;
import org.jboss.shrinkwrap.descriptor.api.webcommon30.WebAppVersionType;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URL;

import static org.apache.sirona.test.web.Clients.newClient;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

@RunWith(Arquillian.class)
public class JspMonitoringTest {
    static { // to start faster
        System.setProperty(Constants.PLUGGABILITY_JARS_TO_SKIP, "a*,c*,d*,e*,g*,h*,i*,j*,l*,m*,n*,p*," +
                                                                "r*,sa*,se*,sh*,su*,t*,v*,w*,x*,z*");
        System.setProperty(Constants.TLD_JARS_TO_SKIP, "*");
    }

    @Deployment(testable = false)
    public static Archive<?> war() {
        return ShrinkWrap.create(WebArchive.class, "ajsp.war")
                .addAsWebResource(new StringAsset("Hello"), "test.jsp")
                .setWebXML(new StringAsset(
                    Descriptors.create(WebAppDescriptor.class)
                        .metadataComplete(true) // don't scan
                        .version(WebAppVersionType._3_0)
                        .createFilter()
                            .filterClass(LazyJspMonitoringFilterActivator.class.getName())
                            .filterName("jsp-mon-on")
                        .up()
                        .createFilterMapping()
                            .filterName("jsp-mon-on")
                            .urlPattern("*")
                        .up()
                        .createServlet()
                            .servletName("redir")
                            .servletClass(RedirectServlet.class.getName())
                        .up()
                        .createServletMapping()
                            .servletName("redir")
                            .urlPattern("/test")
                        .up()
                        .exportAsString()));
    }

    @ArquillianResource
    private URL url;

    @Before
    @After
    public void resetCounters() {
        Repository.INSTANCE.clearCounters();
    }

    @Test
    public void jsp() throws IOException {
        final String testUrl = url.toExternalForm() + "test.jsp";
        for (int i = 0; i < 2; i++) {
            assertEquals("Hello", newClient().getPage(testUrl).getWebResponse().getContentAsString());
        }
        assertEquals("Hello", newClient().getPage(testUrl + "?ignoredQuery=yes&ofcourse=itis").getWebResponse().getContentAsString());

        assertFalse(Repository.INSTANCE.counters().isEmpty());
        final Counter counter = Repository.INSTANCE.counters().iterator().next();
        assertEquals(Role.JSP, counter.getKey().getRole());
        assertEquals(url.getPath() + "test.jsp", counter.getKey().getName());
        assertEquals(3, counter.getHits());
    }

    @Test
    public void redirect() throws IOException {
        final String testUrl = url.toExternalForm() + "test";
        for (int i = 0; i < 2; i++) {
            assertEquals("Hello", newClient().getPage(testUrl).getWebResponse().getContentAsString());
        }
        assertEquals("Hello", newClient().getPage(testUrl + "?ignoredQuery=yes&ofcourse=itis").getWebResponse().getContentAsString());

        assertFalse(Repository.INSTANCE.counters().isEmpty());
        final Counter counter = Repository.INSTANCE.counters().iterator().next();
        assertEquals(Role.JSP, counter.getKey().getRole());
        assertEquals(url.getPath() + "test.jsp", counter.getKey().getName());
        assertEquals(3, counter.getHits());
    }

    public static class RedirectServlet extends HttpServlet {
        @Override
        protected void service(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
            req.getRequestDispatcher("test.jsp").forward(req, resp);
        }
    }
}
