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
package org.apache.commons.monitoring.gauges;

import org.apache.commons.monitoring.Role;
import org.apache.commons.monitoring.repositories.Repository;

import java.util.LinkedList;
import java.util.ServiceLoader;

public interface Gauge {
    Role role();
    double value();
    long period();

    public static class LoaderHelper {
        private LinkedList<Gauge> gauges = new LinkedList<Gauge>();

        public LoaderHelper(final boolean excludeParent, final String... includedPrefixes) {
            final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

            for (final Gauge g : ServiceLoader.load(Gauge.class, classLoader)) {
                addGaugeIfNecessary(classLoader, g, excludeParent, includedPrefixes);
            }
            for (final GaugeFactory gf : ServiceLoader.load(GaugeFactory.class, classLoader)) {
                for (final Gauge g : gf.gauges()) {
                    addGaugeIfNecessary(classLoader, g, excludeParent, includedPrefixes);
                }
            }
        }

        private void addGaugeIfNecessary(final ClassLoader classLoader, final Gauge g, final boolean excludeParent, final String... prefixes) {
            final Class<? extends Gauge> gaugeClass = g.getClass();
            if (!excludeParent || gaugeClass.getClassLoader() == classLoader) {
                if (prefixes != null && prefixes.length > 0) {
                    boolean found = false;
                    for (final String p : prefixes) {
                        if (gaugeClass.getName().startsWith(p.trim())) {
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        return;
                    }
                }
                Repository.INSTANCE.addGauge(g);
                gauges.add(g);
            }
        }

        public void destroy() {
            for (final Gauge gauge : gauges) {
                Repository.INSTANCE.stopGauge(gauge.role());
            }
        }
    }
}
