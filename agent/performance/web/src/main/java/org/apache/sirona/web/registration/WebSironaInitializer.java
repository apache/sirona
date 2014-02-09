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
package org.apache.sirona.web.registration;

import org.apache.sirona.configuration.Configuration;
import org.apache.sirona.repositories.Repository;
import org.apache.sirona.util.Environment;
import org.apache.sirona.web.discovery.GaugeDiscoveryListener;
import org.apache.sirona.web.lifecycle.LazyJspMonitoringFilterActivator;
import org.apache.sirona.web.lifecycle.SironaLifecycle;
import org.apache.sirona.web.servlet.SironaFilter;
import org.apache.sirona.web.session.SironaSessionListener;

import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration;
import javax.servlet.ServletContainerInitializer;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import java.util.EnumSet;
import java.util.Set;

public class WebSironaInitializer implements ServletContainerInitializer {
    private static final String JSP_ACTIVATED = Configuration.CONFIG_PROPERTY_PREFIX + "web.jsp.activated";
    private static final String ACTIVATED = Configuration.CONFIG_PROPERTY_PREFIX + "web.activated";

    @Override
    public void onStartup(final Set<Class<?>> classes, final ServletContext ctx) throws ServletException {
        if (Environment.isCollector()) {
            return;
        }

        final String falseStr = Boolean.FALSE.toString();

        final String activated = ctx.getInitParameter(ACTIVATED);
        if (falseStr.equalsIgnoreCase(Configuration.getProperty(ACTIVATED, activated))) {
            return;
        }

        final String monStatus = Boolean.toString(!falseStr.equalsIgnoreCase(ctx.getInitParameter(SironaFilter.MONITOR_STATUS)));
        ctx.setAttribute(SironaFilter.MONITOR_STATUS, monStatus);

        ctx.addListener(SironaSessionListener.class);
        ctx.addListener(GaugeDiscoveryListener.class);
        if (ctx.getClassLoader().equals(Repository.class.getClassLoader())) {
            ctx.addListener(SironaLifecycle.class);
        }

        String ignoredUrls = ctx.getInitParameter(SironaFilter.IGNORED_URLS);
        String monitoredUrls = ctx.getInitParameter(Configuration.CONFIG_PROPERTY_PREFIX + "web.monitored-urls");
        if (!falseStr.equalsIgnoreCase(monitoredUrls)) {
            if (monitoredUrls == null) {
                monitoredUrls = "/*";
            }

            if (ignoredUrls == null) {
                ignoredUrls = Configuration.getProperty(SironaFilter.IGNORED_URLS, "/sirona");
            }

            if (monitoredUrls.contains(",")) {
                final String[] split = monitoredUrls.split(",");
                for (int i = 0; i < split.length; i++) {
                    final FilterRegistration.Dynamic filter = ctx.addFilter("monitoring-filter-" + i, SironaFilter.class);
                    filter.addMappingForUrlPatterns(EnumSet.allOf(DispatcherType.class), false, split[i]);
                    filter.setInitParameter(SironaFilter.MONITOR_STATUS, monStatus);
                    filter.setInitParameter(SironaFilter.IGNORED_URLS, ignoredUrls);
                }
            } else {
                final FilterRegistration.Dynamic filter = ctx.addFilter("monitoring-filter", SironaFilter.class);
                filter.addMappingForUrlPatterns(EnumSet.allOf(DispatcherType.class), false, monitoredUrls);
                filter.setInitParameter(SironaFilter.MONITOR_STATUS, monStatus);
                filter.setInitParameter(SironaFilter.IGNORED_URLS, ignoredUrls);
            }
        }

        // default is false for jsp monitoring since it brings things only in specific cases
        if (Boolean.TRUE.toString().equalsIgnoreCase(Configuration.getProperty(JSP_ACTIVATED, ctx.getInitParameter(JSP_ACTIVATED)))) {
            ctx.addFilter("sirona-jsp-activator", LazyJspMonitoringFilterActivator.class)
                    .addMappingForUrlPatterns(EnumSet.allOf(DispatcherType.class), false, "*");
        }
    }
}
