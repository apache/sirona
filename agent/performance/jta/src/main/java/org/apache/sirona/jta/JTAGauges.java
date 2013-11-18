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
package org.apache.sirona.jta;

import org.apache.sirona.Role;
import org.apache.sirona.configuration.Configuration;
import org.apache.sirona.counters.Unit;
import org.apache.sirona.gauges.Gauge;
import org.apache.sirona.gauges.GaugeFactory;

import java.util.concurrent.atomic.AtomicLong;

public class JTAGauges implements GaugeFactory {
    public static final Role JTA_COMMITED = new Role("jta-commited", Unit.UNARY);
    public static final Role JTA_ROLLBACKED = new Role("jta-rollbacked", Unit.UNARY);
    public static final Role JTA_ACTIVE = new Role("jta-active", Unit.UNARY);

    static final AtomicLong ACTIVE = new AtomicLong(0);
    static final AtomicLong COMMITTED = new AtomicLong(0);
    static final AtomicLong ROLLBACKED = new AtomicLong(0);

    @Override
    public Gauge[] gauges() {
        final long period = Configuration.getInteger(Configuration.CONFIG_PROPERTY_PREFIX + "gauge.jta.period", 4000);
        return new Gauge[] {
            new JTAGauge(JTA_COMMITED, COMMITTED, period),
            new JTAGauge(JTA_ROLLBACKED, ROLLBACKED, period),
            new JTAActiveGauge(JTA_ACTIVE, ACTIVE, period)
        };
    }

    protected static class JTAGauge implements Gauge {
        private final Role role;
        private final long period;
        protected final AtomicLong counter;

        protected JTAGauge(final Role role, final AtomicLong counter, final long period) {
            this.role = role;
            this.counter = counter;
            this.period = period;
        }

        @Override
        public Role role() {
            return role;
        }

        @Override
        public double value() {
            return counter.getAndSet(0);
        }
    }

    protected static class JTAActiveGauge extends JTAGauge {
        protected JTAActiveGauge(final Role role, final AtomicLong counter, final long period) {
            super(role, counter, period);
        }

        @Override
        public double value() {
            return counter.get();
        }
    }
}
