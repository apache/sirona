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
package org.apache.sirona.reporting.web.plugin.gauge;

import org.apache.commons.codec.binary.Base64;
import org.apache.sirona.Role;
import org.apache.sirona.reporting.web.plugin.api.Regex;
import org.apache.sirona.reporting.web.plugin.api.Template;
import org.apache.sirona.repositories.Repository;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

import static org.apache.sirona.reporting.web.plugin.api.graph.Graphs.generateReport;

// gauge names can be not so URL friendly so using urlencode(base64), encoding is not in an utility class since it can change depending the data
public class GaugeEndpoints {
    private static final String UTF8 = "UTF-8";

    @Regex
    public Template home() {
        return new Template("gauge/home.vm").set("gauges", sortNames(Repository.INSTANCE.gauges()));
    }

    @Regex("/([^/]*)")
    public Template detail(final String role) {
        return new Template("gauge/detail.vm")
            .set("gauge", decode(role))
            .set("gauge64", role);
    }

    @Regex("/([^/]*)/([0-9]*)/([0-9]*)")
    public String jsonDetail(final String base64Role, final long start, final long end) {
        final String role = decode(base64Role);
        return generateReport(role, Repository.INSTANCE.findGaugeRole(role), start, end);
    }

    private static String decode(final String base64Role) {
        try {
            return new String(Base64.decodeBase64(URLDecoder.decode(base64Role, UTF8)));
        } catch (final UnsupportedEncodingException e) {
            return base64Role; // shouldn't occur
        }
    }

    private static String encode(final String role) {
        final String base64 = Base64.encodeBase64URLSafeString(role.getBytes());
        try {
            return URLEncoder.encode(base64, UTF8);
        } catch (final UnsupportedEncodingException e) {
            return base64; // shouldn't occur
        }
    }

    private static Map<String, String> sortNames(final Collection<Role> gauges) {
        final Map<String, String> names = new TreeMap<String, String>();
        for (final Role gauge : gauges) {
            final String name = gauge.getName();
            names.put(name, encode(name));
        }
        return names;
    }
}
