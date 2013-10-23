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
package org.apache.sirona.web.session;

import org.apache.sirona.Role;
import org.apache.sirona.configuration.Configuration;
import org.apache.sirona.counters.Unit;
import org.apache.sirona.gauges.Gauge;

import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class SessionGauge implements Gauge {
    private static final WeakHashMap<Role, SessionGauge> GAUGES = new WeakHashMap<Role, SessionGauge>();

    public static Map<Role, SessionGauge> gauges() {
        return new HashMap<Role, SessionGauge>(GAUGES);
    }

    private final Role role;
    private final AtomicLong counter;

    public SessionGauge(final String ctx, final AtomicLong counter) {
        this.role = new Role("sessions-" + ctx, Unit.UNARY);
        this.counter = counter;

        synchronized (GAUGES) {
            GAUGES.put(role, this);
        }
    }

    @Override
    public Role role() {
        return role;
    }

    @Override
    public double value() {
        return counter.get();
    }

    @Override
    public long period() {
        return Configuration.getInteger(Configuration.CONFIG_PROPERTY_PREFIX + "gauge.sessions.period", 4000);
    }
}
