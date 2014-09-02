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
package org.apache.sirona.reporting.web.plugin.status;

import org.apache.sirona.reporting.web.plugin.api.Regex;
import org.apache.sirona.reporting.web.plugin.api.Template;
import org.apache.sirona.reporting.web.status.StatusHelper;
import org.apache.sirona.repositories.Repository;
import org.apache.sirona.status.NodeStatus;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class StatusEndpoints {
    private static final String DEFAULT_ROOT = "-";
    private static final String APP_DELIMITER = "#";

    @Regex
    public Template home() {
        final Map<String, Map<String, NodeStatus>> statusesByApp = new HashMap<String, Map<String, NodeStatus>>();
        for (final Map.Entry<String, NodeStatus> entry : Repository.INSTANCE.statuses().entrySet()) {
            final String key = entry.getKey();
            final String[] segments;
            if (key.contains(APP_DELIMITER)) {
                segments = key.split(APP_DELIMITER);
            } else {
                segments = new String[] { DEFAULT_ROOT, key };
            }

            Map<String, NodeStatus> statusesOfTheApp = statusesByApp.get(segments[0]);
            if (statusesOfTheApp == null) {
                statusesOfTheApp = new TreeMap<String, NodeStatus>();
                statusesByApp.put(segments[0], statusesOfTheApp);
            }
            statusesOfTheApp.put(segments[1], entry.getValue());
        }

        return new Template("status/home.vm")
                    .set("helper", StatusHelper.class)
                    .set("apps", statusesByApp);
    }

    @Regex("/([^/]*)")
    public Template detail(final String node) {
        return new Template("status/detail.vm")
            .set("helper", StatusHelper.class)
            .set("node", Repository.INSTANCE.statuses().get(node))
            .set("name", node);
    }
}
