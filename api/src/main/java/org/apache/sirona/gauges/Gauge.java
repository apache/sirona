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
package org.apache.sirona.gauges;

import org.apache.sirona.Role;
import org.apache.sirona.repositories.Repository;
import org.apache.sirona.spi.SPI;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

public interface Gauge {
    Role role();

    double value();

    public static class LoaderHelper {
        private LinkedList<Gauge> gauges = new LinkedList<Gauge>();

        public LoaderHelper(final boolean excludeParent, final Collection<? extends Gauge> manualGauges, final String... includedPrefixes) {
            final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

            for (final Gauge g : manualGauges) {
                addGauge(g);
            }
            for (final Gauge g : SPI.INSTANCE.find(Gauge.class, classLoader)) {
                addGaugeIfNecessary(classLoader, g, excludeParent, includedPrefixes);
            }
            for (final GaugeFactory gf : SPI.INSTANCE.find(GaugeFactory.class, classLoader)) {
                final Gauge[] list = gf.gauges();
                if (list != null) {
                    for (final Gauge g : list) {
                        addGaugeIfNecessary(classLoader, g, excludeParent, includedPrefixes);
                    }
                }
            }
        }

        public LoaderHelper(final boolean excludeParent, final String... includedPrefixes) {
            this(excludeParent, Collections.<Gauge>emptyList(), includedPrefixes);
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
                addGauge(g);
            }
        }

        private void addGauge(final Gauge g) {
            Repository.INSTANCE.addGauge(g);
            gauges.add(g);
        }

        public void destroy() {
            for (final Gauge gauge : gauges) {
                Repository.INSTANCE.stopGauge(gauge);
            }
            gauges.clear();
        }
    }
}
