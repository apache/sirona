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

import org.apache.commons.monitoring.configuration.Configuration;
import org.apache.commons.monitoring.counter.Unit;

import java.io.PrintWriter;
import java.util.Collection;
import java.util.Map;

public class CSVFormat extends MapFormat implements Format {
    private static final String SEPARATOR = Configuration.getProperty(Configuration.COMMONS_MONITORING_PREFIX + "csv.separator", ";");
    public static final String HEADER = "Monitor" + SEPARATOR + "Category" + SEPARATOR + "Role" + SEPARATOR + toCsv(ATTRIBUTES_ORDERED_LIST);


    @Override
    public void render(final PrintWriter writer, final Map<String, ?> params) {
        final Unit timeUnit = timeUnit(params);

        writer.write(HEADER);
        for (final Collection<String> line : snapshot(timeUnit)) {
            writer.write(toCsv(line));
        }
    }

    @Override
    public String type() {
        return "text/plain";
    }

    private static String toCsv(final Collection<String> line) {
        final StringBuilder builder = new StringBuilder();
        for (final String s : line) {
            builder.append(s).append(SEPARATOR);
        }

        final String str = builder.toString();
        return str.substring(0, str.length() - 1) + '\n';
    }
}
