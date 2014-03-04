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

import com.gargoylesoftware.htmlunit.NicelyResynchronizingAjaxController;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebClient;
import org.apache.catalina.startup.Constants;
import org.apache.sirona.reporting.web.registration.SironaReportingInitializer;
import org.apache.sirona.web.lifecycle.SironaLifecycle;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.runner.RunWith;

import javax.servlet.ServletContainerInitializer;
import java.io.IOException;
import java.net.URL;

// NOTE: we depend implicitely on tomcat embedded adapter since
// 1) we don't add dependencies
// 2) we use the fact Repository.INSTANCE is in the same JVM
@RunWith(Arquillian.class)
public abstract class SironaReportingTestBase {
    static {
        // we use this hack to init jars to skip since this method is triggered before any deployment
        // so ContextConfig is not yet loaded
        System.setProperty(Constants.DEFAULT_JARS_TO_SKIP, "a*,c*,d*,e*,g*,h*,i*,j*,l*,m*,n*,p*,r*,sa*,se*,sh*,su*,t*,v*,w*,x*,z*");
    }

    @Deployment(testable = false)
    public static Archive<?> war() {
        // bug hack: we don't create an Archive<?> representing our webapp since all will be at classpath
        // this makes test faster and easier to maintain (if we add stuff) but dependent on our current tomcat embedded adapter
        return ShrinkWrap.create(WebArchive.class, "sirona-test.war")
            .addAsLibraries(
                ShrinkWrap.create(JavaArchive.class, "sci.jar") // bug in tomcat?
                    .addAsServiceProvider(ServletContainerInitializer.class, SironaReportingInitializer.class)
                    .addClass(SironaLifecycle.class));
    }

    @ArquillianResource
    protected URL base;

    protected <P extends Page> P page(final String path) throws IOException {
        return newClient().getPage(base.toExternalForm() + "/sirona/" + path);
    }

    protected static WebClient newClient() {
        final WebClient webClient = new WebClient();
        webClient.getOptions().setJavaScriptEnabled(false);
        webClient.getOptions().setCssEnabled(false);
        webClient.getOptions().setAppletEnabled(false);
        webClient.setAjaxController(new NicelyResynchronizingAjaxController());
        return webClient;
    }
}
