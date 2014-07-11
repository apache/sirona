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
package org.apache.sirona.reporting.web.plugin.report.format;

import org.apache.sirona.configuration.Configuration;
import org.apache.sirona.counters.Unit;
import org.apache.sirona.reporting.web.plugin.api.MapBuilder;
import org.apache.sirona.reporting.web.plugin.api.Template;

import java.util.Collection;
import java.util.Map;

public class CSVFormat extends MapFormat implements Format {
    private static final String SEPARATOR = Configuration.getProperty(Configuration.CONFIG_PROPERTY_PREFIX + "csv.separator", ";");
    public static final String HEADER = "Counter" + SEPARATOR + "Role" + SEPARATOR + toCsv(ATTRIBUTES_ORDERED_LIST);

    @Override
    public Template render(final Map<String, ?> params) {
        final Unit timeUnit = timeUnit(params);
        return new Template("/templates/report/report-csv.vm",
                        new MapBuilder<String, Object>()
                        .set("headers", HEADER)
                        .set("separator", SEPARATOR)
                        .set("lines", snapshot(timeUnit, format(params, null)))
                        .build(), false);
    }

    @Override
    public String type() {
        return "text/plain";
    }

    private static String toCsv(final Collection<String> line) {
        final StringBuilder builder = new StringBuilder();
        for (final String s : line) {
            if ("Counter".equals(s) || "Role".equals(s)) { // forced first
                continue;
            }

            builder.append(s).append(SEPARATOR);
        }

        final String str = builder.toString();
        return str.substring(0, str.length() - 1) + '\n';
    }
}
