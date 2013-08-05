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

import org.apache.commons.monitoring.counters.Unit;
import org.apache.commons.monitoring.reporting.web.handler.api.Template;
import org.apache.commons.monitoring.reporting.web.template.MapBuilder;

import java.util.Map;

public class HTMLFormat extends MapFormat implements Format {
    @Override
    public Template render(final Map<String, ?> params) {
        final Unit timeUnit = timeUnit(params);
        return new Template("report/report.vm",
            new MapBuilder<String, Object>()
                .set(Map.class.cast(params))
                .set("headers", ATTRIBUTES_ORDERED_LIST)
                .set("data", snapshot(timeUnit))
                .build());
    }

    @Override
    public String type() {
        return "text/html";
    }
}
