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
package org.apache.sirona.reporting.web.plugin.web;

import org.apache.sirona.Role;
import org.apache.sirona.reporting.web.handler.api.Regex;
import org.apache.sirona.reporting.web.handler.api.Template;
import org.apache.sirona.reporting.web.plugin.json.Jsons;
import org.apache.sirona.repositories.Repository;
import org.apache.sirona.web.session.SessionGauge;

public class WebEndpoints {
    @Regex
    public Template home() {
        return new Template("web/web.vm");
    }

    @Regex("/sessions/([0-9]*)/([0-9]*)")
    public String sessions(final long start, final long end) {
        final StringBuilder builder = new StringBuilder("[");
        for (final Role gauge : SessionGauge.gauges().keySet()) {
            builder.append("{ \"data\": ")
                .append(Jsons.toJson(Repository.INSTANCE.getGaugeValues(start, end, gauge)))
                .append(", \"label\": \"").append(gauge.getName()).append("\", \"color\": \"#317eac\" }")
                .append(",");
        }

        final int length = builder.length();
        if (length > 1) {
            return builder.toString().substring(0, length - 1) + "]";
        }
        return builder.append("]").toString();
    }
}
