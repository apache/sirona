/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.sirona.tomee.agent.gauge;

import org.apache.openejb.util.reflection.Reflections;
import org.apache.sirona.Role;
import org.apache.sirona.counters.Unit;
import org.apache.sirona.gauges.Gauge;
import org.apache.sirona.tomee.agent.Reflection;

import java.beans.Introspector;

public class PoolGauge implements Gauge, Reflection {
    private final Object stats;
    private final Role role;
    private final String aggregate;

    public PoolGauge(final String name, final Object stats, final String target) {
        this.stats = stats;
        this.role = new Role("tomee-pool-stateless-" + Introspector.decapitalize(target.substring(3)) + "-" + name, Unit.UNARY);
        this.aggregate = target;
    }

    @Override
    public Role role() {
        return role;
    }

    @Override
    public double value() {
        return Number.class.cast(Reflections.invokeByReflection(stats, aggregate, NO_PARAM_TYPES, NO_PARAM)).doubleValue();
    }
}
