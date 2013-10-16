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
package org.apache.commons.monitoring.reporting.web.plugin.report.format;

import org.apache.commons.monitoring.counters.Counter;
import org.apache.commons.monitoring.counters.MetricData;
import org.apache.commons.monitoring.counters.Unit;
import org.apache.commons.monitoring.repositories.Repository;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

public abstract class MapFormat {
    protected static final Collection<String> ATTRIBUTES_ORDERED_LIST = buildMetricDataHeader();

    protected static Collection<String> buildMetricDataHeader() {
        final Collection<String> list = new CopyOnWriteArrayList<String>();
        list.add("Counter");
        list.add("Role");
        for (final MetricData md : MetricData.values()) {
            list.add(md.name());
        }
        return list;
    }

    protected static String format(final Map<String, ?> params, final String defaultValue) {
        final Object format = params.get("format");
        if (format != null) {
            if (String.class.isInstance(format)) {
                final String strFormat = String.class.cast(format);
                return decode(strFormat);
            }
            if (String[].class.isInstance(format)) {
                final String[] array = String[].class.cast(format);
                return decode(array[0]);
            }
        }
        return defaultValue;
    }

    private static String decode(String strFormat) {
        try {
            return URLDecoder.decode(strFormat, "UTF-8");
        } catch (final UnsupportedEncodingException e) {
            return strFormat;
        }
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

    protected static Collection<Collection<String>> snapshot(final Unit timeUnit, final String format) {
        final Collection<Collection<String>> data = new ArrayList<Collection<String>>();
        for (final Counter counter : Repository.INSTANCE) {
            final Unit counterUnit = counter.getKey().getRole().getUnit();
            final boolean compatible = timeUnit.isCompatible(counterUnit);

            final Collection<String> line = new ArrayList<String>();
            data.add(line);

            line.add(counter.getKey().getName());

            if (compatible) {
                line.add(counter.getKey().getRole().getName() + " (" + timeUnit.getName() + ")");
            } else {
                line.add(counter.getKey().getRole().getName() + " (" + counterUnit.getName() + ")");
            }

            final DecimalFormat formatter;
            if (format != null) {
                formatter = new DecimalFormat(format);
            } else {
                formatter = null;
            }

            for (final MetricData md : MetricData.values()) {
                double value = md.value(counter);
                if (md.isTime() && compatible && timeUnit != counterUnit) {
                    value = timeUnit.convert(value, counterUnit);
                }
                if (formatter != null && !Double.isNaN(value) && !Double.isInfinite(value)) {
                    line.add(formatter.format(value));
                } else {
                    line.add(Double.toString(value));
                }
            }
        }
        return data;
    }
}
