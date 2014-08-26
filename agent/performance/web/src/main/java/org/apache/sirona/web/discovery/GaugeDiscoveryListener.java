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
package org.apache.sirona.web.discovery;

import org.apache.sirona.Role;
import org.apache.sirona.configuration.Configuration;
import org.apache.sirona.counters.Unit;
import org.apache.sirona.gauges.Gauge;
import org.apache.sirona.web.servlet.SironaFilter;
import org.apache.sirona.web.servlet.StatusGauge;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.net.HttpURLConnection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class GaugeDiscoveryListener implements ServletContextListener {
    public static final String STATUS_GAUGES_ATTRIBUTE = "status-gauges";
    private static final int[] DEFAULT_STATUSES = {
        HttpURLConnection.HTTP_OK,
        HttpURLConnection.HTTP_CREATED,
        HttpURLConnection.HTTP_NO_CONTENT,
        HttpURLConnection.HTTP_BAD_REQUEST,
        HttpURLConnection.HTTP_MOVED_PERM,
        HttpURLConnection.HTTP_MOVED_TEMP,
        HttpURLConnection.HTTP_FORBIDDEN,
        HttpURLConnection.HTTP_NOT_FOUND,
        HttpURLConnection.HTTP_INTERNAL_ERROR
    };

    private Gauge.LoaderHelper helper;

    @Override
    public void contextInitialized(final ServletContextEvent sce) {
        // init status gauges
        final ConcurrentMap<Integer, StatusGauge> gauges = new ConcurrentHashMap<Integer, StatusGauge>(35);
        if ("true".equalsIgnoreCase((String) sce.getServletContext().getAttribute(SironaFilter.MONITOR_STATUS))) {
            final String monitoredStatuses = sce.getServletContext().getInitParameter(Configuration.CONFIG_PROPERTY_PREFIX + "web.monitored-statuses");

            String contextPath = sce.getServletContext().getContextPath();
            if (contextPath == null || contextPath.isEmpty()) {
                contextPath = "/";
            }

            if (monitoredStatuses == null) {
                for (final int status : DEFAULT_STATUSES) {
                    gauges.put(status, statusGauge(contextPath, gauges, status));
                }
                /* we could use it but it defines 25 statuses, surely too much
                for (final Field f : HttpURLConnection.class.getDeclaredFields()) {
                    final int modifiers = f.getModifiers();
                    if (f.getName().startsWith("HTTP_")
                        && Modifier.isStatic(modifiers) && Modifier.isPublic(modifiers) && Modifier.isFinal(modifiers)) {
                        try {
                            final int status = (Integer) f.get(null);
                            gauges.put(status, statusGauge(sce.getServletContext().getContextPath(), gauges, (Integer) f.get(null)));
                        } catch (final IllegalAccessException e) {
                            // no-op
                        }
                    }
                }
                */
            } else {
                for (final String status : monitoredStatuses.split(",")) {
                    final int statusInt = Integer.parseInt(status.trim());
                    gauges.put(statusInt, statusGauge(contextPath, gauges, statusInt));
                }
            }
            sce.getServletContext().setAttribute(STATUS_GAUGES_ATTRIBUTE, gauges);
        }

        // discovery registration
        final String prefixesStr = sce.getServletContext().getInitParameter("monitoring.discovery.packages");
        final String[] prefixes;
        if (prefixesStr != null) {
            prefixes = prefixesStr.split(",");
        } else {
            prefixes = null;
        }
        helper = new Gauge.LoaderHelper("true".equals(sce.getServletContext().getInitParameter("monitoring.discovery.exclude-parent")), //
                                        gauges.values(), //
                                        prefixes);
    }

    @Override
    public void contextDestroyed(final ServletContextEvent sce) {
        if (helper != null) {
            helper.destroy();
        }
    }

    private static StatusGauge statusGauge(final String prefix, final ConcurrentMap<Integer, StatusGauge> gauges, final int status) {
        final StatusGauge gauge = gauges.get(status);
        if (gauge != null) {
            return gauge;
        }

        final StatusGauge newGauge = new StatusGauge(new Role(prefix + "-HTTP-" + Integer.toString(status), Unit.UNARY));
        final StatusGauge old = gauges.putIfAbsent(status, newGauge);
        if (old != null) {
            return old;
        }
        return newGauge;
    }
}
