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
package org.apache.sirona.gauges.jmx;

import org.apache.sirona.Role;
import org.apache.sirona.SironaException;
import org.apache.sirona.counters.Unit;
import org.apache.sirona.gauges.Gauge;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;

public abstract class JMXAttributeGaugeBase implements Gauge {
    private static final MBeanServer SERVER = ManagementFactory.getPlatformMBeanServer();

    private final ObjectName name;
    private final String attribute;
    private final Role role;

    public JMXAttributeGaugeBase(final ObjectName name, final String attribute, final String role, final Unit unit) {
        this.name = name;
        this.attribute = attribute;
        this.role = new Role(role, unit);
    }

    public JMXAttributeGaugeBase(final ObjectName name, final String attribute) {
        this(name, attribute, name.getCanonicalName() + "#" + attribute, Unit.UNARY);
    }

    @Override
    public Role role() {
        return role;
    }

    @Override
    public double value() {
        try {
            return Number.class.cast(SERVER.getAttribute(name, attribute)).doubleValue();
        } catch (final Exception e) {
            throw new SironaException(e);
        }
    }
}
