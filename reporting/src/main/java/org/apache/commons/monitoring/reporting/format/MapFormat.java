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
package org.apache.commons.monitoring.reporting.format;

import org.apache.commons.monitoring.counter.Counter;
import org.apache.commons.monitoring.counter.Unit;
import org.apache.commons.monitoring.monitors.Monitor;
import org.apache.commons.monitoring.repositories.Repository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

public class MapFormat {
    protected static final Collection<String> ATTRIBUTES_ORDERED_LIST = buildMetricDataHeader();

    protected static Collection<String> buildMetricDataHeader() {
        final Collection<String> list = new CopyOnWriteArrayList<String>();
        list.add("Monitor");
        list.add("Category");
        list.add("Role");
        for (final MetricData md : MetricData.values()) {
            list.add(md.name());
        }
        return list;
    }

    protected static Unit timeUnit(final Map<String, ?> params) {
        final Object u = params.get("unit");
        if (u != null) {
            if (String.class.isInstance(u)) {
                final Unit unit = Unit.get(String.class.cast(u).toLowerCase());
                if (unit != null) {
                    return unit;
                }
            }
            if (String[].class.isInstance(u)) {
                final String[] array = String[].class.cast(u);
                if (array.length > 0) {
                    final Unit unit = Unit.get(array[0].toLowerCase());
                    if (unit != null) {
                        return unit;
                    }
                }
            }
        }
        return Unit.Time.MILLISECOND;
    }

    protected static Collection<Collection<String>> snapshot(final Unit timeUnit) {
        final Collection<Collection<String>> data = new ArrayList<Collection<String>>();
        for (final Monitor monitor : Repository.INSTANCE.getMonitors()) {
            for (final Counter counter : monitor.getCounters()) {
                final Unit counterUnit = counter.getRole().getUnit();
                final boolean compatible = timeUnit.isCompatible(counterUnit);

                final Collection<String> line = new ArrayList<String>();
                data.add(line);

                line.add(monitor.getKey().getName());
                line.add(monitor.getKey().getCategory());

                if (compatible) {
                    line.add(counter.getRole().getName() + " (" + timeUnit.getName() + ")");
                } else {
                    line.add(counter.getRole().getName() + " (" + counterUnit.getName() + ")");
                }

                for (final MetricData md : MetricData.values()) {
                    double value = md.value(counter);
                    if (md.isTime() && compatible && timeUnit != counterUnit) {
                        value = timeUnit.convert(value, counterUnit);
                    }
                    line.add(Double.toString(value));
                }
            }
        }
        return data;
    }
}
