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
import org.apache.sirona.web.discovery.GaugeDiscoveryListener;
import org.apache.sirona.web.lifecycle.SironaLifecycle;
import org.apache.sirona.web.servlet.MonitoringFilter;
import org.apache.sirona.web.session.MonitoringSessionListener;

import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration;
import javax.servlet.ServletContainerInitializer;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import java.util.EnumSet;
import java.util.Set;

public class WebMonitoringInitializer implements ServletContainerInitializer {
    @Override
    public void onStartup(final Set<Class<?>> classes, final ServletContext ctx) throws ServletException {
        final String activated = ctx.getInitParameter(Configuration.CONFIG_PROPERTY_PREFIX + "web.activated");
        if ("false".equalsIgnoreCase(activated)) {
            return;
        }

        ctx.addListener(MonitoringSessionListener.class);
        ctx.addListener(GaugeDiscoveryListener.class);
        if (ctx.getClassLoader().equals(Repository.class.getClassLoader())) {
            ctx.addListener(SironaLifecycle.class);
        }

        final String monStatus = Boolean.toString(!"false".equalsIgnoreCase(ctx.getInitParameter(MonitoringFilter.MONITOR_STATUS)));
        String monitoredUrls = ctx.getInitParameter(Configuration.CONFIG_PROPERTY_PREFIX + "web.monitored-urls");
        if (!"false".equalsIgnoreCase(monitoredUrls)) {
            if (monitoredUrls == null) {
                monitoredUrls = "/*";
            }
            if (monitoredUrls.contains(",")) {
                final String[] split = monitoredUrls.split(",");
                for (int i = 0; i < split.length; i++) {
                    final FilterRegistration.Dynamic filter = ctx.addFilter("monitoring-filter-" + i, MonitoringFilter.class);
                    filter.addMappingForUrlPatterns(EnumSet.allOf(DispatcherType.class), false, split[i]);
                    filter.setInitParameter(MonitoringFilter.MONITOR_STATUS, monStatus);
                }
            } else {
                final FilterRegistration.Dynamic filter = ctx.addFilter("monitoring-filter", MonitoringFilter.class);
                filter.addMappingForUrlPatterns(EnumSet.allOf(DispatcherType.class), false, monitoredUrls);
                filter.setInitParameter(MonitoringFilter.MONITOR_STATUS, monStatus);
            }
        }
    }
}
