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
package org.apache.sirona.web.servlet;

import org.apache.sirona.Role;
import org.apache.sirona.configuration.Configuration;
import org.apache.sirona.gauges.Gauge;

import java.util.concurrent.atomic.AtomicLong;

public class StatusGauge implements Gauge {
    private static final int PERIOD = Configuration.getInteger(Configuration.CONFIG_PROPERTY_PREFIX + "web.gauge.status.period", 4000);

    private final Role role;
    private final AtomicLong count = new AtomicLong(0);

    public StatusGauge(final Role role) {
        this.role = role;
    }

    @Override
    public Role role() {
        return role;
    }

    @Override
    public double value() {
        return count.getAndSet(0);
    }

    @Override
    public long period() {
        return PERIOD;
    }

    public void incr() {
        count.incrementAndGet();
    }

    @Override
    public String toString() {
        return "StatusGauge{role=" + role + '}';
    }
}
