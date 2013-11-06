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
package org.apache.sirona.agent.webapp.pull.gauge;

import org.apache.sirona.gauges.Gauge;
import org.apache.sirona.gauges.GaugeManager;

import java.util.Collection;
import java.util.concurrent.CopyOnWriteArrayList;

public class PullGaugeManager implements GaugeManager {
    private final Collection<Gauge> gauges = new CopyOnWriteArrayList<Gauge>();

    @Override
    public void stop() {
        gauges.clear();
    }

    @Override
    public void addGauge(final Gauge gauge) {
        gauges.add(gauge);
    }

    @Override
    public void stopGauge(final Gauge gauge) {
        gauges.remove(gauge);
    }

    public Collection<Gauge> getGauges() {
        return gauges;
    }
}
