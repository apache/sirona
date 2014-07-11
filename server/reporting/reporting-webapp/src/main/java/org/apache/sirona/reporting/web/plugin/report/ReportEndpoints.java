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
package org.apache.sirona.reporting.web.plugin.report;

import org.apache.sirona.Role;
import org.apache.sirona.SironaException;
import org.apache.sirona.counters.AggregatedCounter;
import org.apache.sirona.counters.Counter;
import org.apache.sirona.counters.Unit;
import org.apache.sirona.reporting.web.plugin.api.MapBuilder;
import org.apache.sirona.reporting.web.plugin.api.Regex;
import org.apache.sirona.reporting.web.plugin.api.Template;
import org.apache.sirona.reporting.web.plugin.report.format.Format;
import org.apache.sirona.reporting.web.plugin.report.format.HTMLFormat;
import org.apache.sirona.reporting.web.plugin.report.format.MapFormat;
import org.apache.sirona.repositories.Repository;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

import static org.apache.sirona.reporting.web.plugin.report.format.MapFormat.format;
import static org.apache.sirona.reporting.web.plugin.report.format.MapFormat.generateLine;
import static org.apache.sirona.reporting.web.plugin.report.format.MapFormat.timeUnit;

public class ReportEndpoints {
    @Regex
    public Template html(final HttpServletRequest request, final HttpServletResponse response) {
        return renderFormat(request, response, Format.Defaults.HTML);
    }

    @Regex("/counter/([^/]*)/([^/]*)\\?name=(.*)")
    public Template counterDetail(final String role, final String unit, final String name, final HttpServletRequest request) {
        final Counter counter = Repository.INSTANCE.getCounter(new Counter.Key(new Role(decode(role), Unit.get(unit)), name)); // name is already decoded by servlet container

        final Map<String, String[]> params = request.getParameterMap();
        final Unit timeUnit = timeUnit(params);
        final String format = format(params, HTMLFormat.NUMBER_FORMAT);

        final Map<String, Collection<String>> counters = new TreeMap<String, Collection<String>>();
        if (AggregatedCounter.class.isInstance(counter)) {
            for (final Map.Entry<String, ? extends Counter> marker : AggregatedCounter.class.cast(counter).aggregated().entrySet()) {
                counters.put(marker.getKey(), generateLine(marker.getValue(), timeUnit, format));
            }
        } else {
            counters.put("", generateLine(counter, timeUnit, format));
        }

        return new Template("report/counter.vm",
            new MapBuilder<String, Object>()
                .set("headers", HTMLFormat.ATTRIBUTES_ORDERED_LIST)
                .set("counter", counter)
                .set("counters", counters)
                .build());
    }

    @Regex(".csv")
    public Template csv(final HttpServletRequest request, final HttpServletResponse response) {
        return renderFormat(request, response, Format.Defaults.CSV);
    }

    @Regex(".json")
    public Template json(final HttpServletRequest request, final HttpServletResponse response) {
        return renderFormat(request, response, Format.Defaults.JSON);
    }

    @Regex(".xml")
    public Template xml(final HttpServletRequest request, final HttpServletResponse response) {
        return renderFormat(request, response, Format.Defaults.XML);
    }

    @Regex("/clear")
    public void clear(final HttpServletRequest request, final HttpServletResponse response) {
        Repository.INSTANCE.clearCounters();
        try {
            response.sendRedirect(request.getRequestURI().substring(0, request.getRequestURI().length() - "/clear".length()));
        } catch (final IOException e) {
            throw new SironaException(e);
        }
    }

    private Template renderFormat(final HttpServletRequest request, final HttpServletResponse response, final Format format) {
        response.setContentType(format.type());
        try {
            request.setCharacterEncoding("UTF-8");
        } catch (final UnsupportedEncodingException e) {
            // no-op
        }
        return format.render(request.getParameterMap());
    }

    private static String decode(final String role) {
        try {
            return URLDecoder.decode(role, MapFormat.ENCODING);
        } catch (final UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
        }
    }
}
