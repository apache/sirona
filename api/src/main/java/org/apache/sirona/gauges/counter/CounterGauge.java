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
package org.apache.sirona.gauges.counter;

import org.apache.sirona.Role;
import org.apache.sirona.counters.Counter;
import org.apache.sirona.counters.MetricData;
import org.apache.sirona.counters.Unit;
import org.apache.sirona.gauges.Gauge;

public class CounterGauge implements Gauge {
    protected final Counter counter;
    protected final MetricData metric;
    protected final Role role;

    protected CounterGauge(final Counter counter) {
        this(counter, MetricData.Sum);
    }

    public CounterGauge(final Counter counter, final MetricData metric) {
        this.counter = counter;
        this.metric = metric;
        this.role = new Role("counter-" + counter.getKey().getRole().getName() + "-" + counter.getKey().getName() + "-" + metric.name(), Unit.UNARY);
    }

    @Override
    public Role role() {
        return role;
    }

    @Override
    public double value() {
        return metric.value(counter);
    }
}
