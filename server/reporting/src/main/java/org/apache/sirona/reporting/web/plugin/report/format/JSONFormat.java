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

import org.apache.sirona.counters.MetricData;
import org.apache.sirona.reporting.web.plugin.api.MapBuilder;
import org.apache.sirona.reporting.web.plugin.api.Template;
import org.apache.sirona.repositories.Repository;

import java.util.Map;

public class JSONFormat implements Format {
    @Override
    public Template render(final Map<String, ?> params) {
        return new Template("/templates/report/report-json.vm",
            new MapBuilder<String, Object>()
                .set("MetricData", MetricData.class)
                .set("counters", Repository.INSTANCE.counters())
                .build(), false);
    }

    @Override
    public String type() {
        return "application/json";
    }
}