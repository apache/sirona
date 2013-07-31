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
package org.apache.commons.monitoring.reporting.web.handler.format;

import org.apache.commons.monitoring.counter.Unit;
import org.apache.commons.monitoring.reporting.web.template.MapBuilder;
import org.apache.commons.monitoring.reporting.web.template.Templates;

import java.io.PrintWriter;
import java.util.Map;

public class HTMLFormat extends MapFormat implements Format {
    @Override
    public void render(final PrintWriter writer, final Map<String, ?> params) {
        final Unit timeUnit = timeUnit(params);
        Templates.htmlRender(writer, "report.vm",
            new MapBuilder<String, Object>()
                .set("headers", ATTRIBUTES_ORDERED_LIST)
                .set("data", snapshot(timeUnit))
                .build());
    }

    @Override
    public String type() {
        return "text/html";
    }
}
