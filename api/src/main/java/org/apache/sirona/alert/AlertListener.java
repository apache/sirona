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
package org.apache.sirona.alert;

import org.apache.sirona.status.NodeStatus;
import org.apache.sirona.status.ValidationResult;

import java.util.HashMap;
import java.util.Map;

public interface AlertListener {
    void onAlert(Alert alert);

    class Alert {
        private final String marker;
        private final NodeStatus status;

        protected Alert(final String node, final NodeStatus status) {
            this.marker = node;
            this.status = status;
        }

        public String getMarker() {
            return marker;
        }

        public NodeStatus getStatus() {
            return status;
        }

        public Map<String, Object> asMap() {
            final String ln = System.getProperty("line.separator");

            final StringBuilder csv = new StringBuilder();
            for (final ValidationResult result : status.getResults()) {
                csv.append(result.getName()).append(";")
                    .append(result.getMessage()).append(";")
                    .append(result.getStatus().name()).append(ln);
            }

            final Map<String, Object> map = new HashMap<String, Object>();
            map.put("marker", marker == null ? "-" : marker);
            map.put("status", status.getStatus().name());
            map.put("date", status.getDate());
            map.put("resultsLength", status.getResults().length);
            map.put("resultsCsv", csv.toString());
            return map;
        }
    }
}
