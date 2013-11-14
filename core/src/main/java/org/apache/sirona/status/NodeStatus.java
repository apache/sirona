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
package org.apache.sirona.status;

import java.util.Date;

public class NodeStatus {
    private final ValidationResult[] results;
    private final Date date;
    private final Status status;

    public NodeStatus(final ValidationResult[] results, final Date date) {
        this.results = results;
        this.date = date;
        this.status = computeStatus();
    }

    public Date getDate() {
        if (date == null) {
            return new Date(0);
        }
        return date;
    }

    public ValidationResult[] getResults() {
        return results;
    }

    public Status getStatus() {
        return status;
    }

    protected Status computeStatus() {
        Status lowest = Status.OK;
        for (final ValidationResult result : results) {
            if (Status.KO.equals(result.getStatus())) {
                return Status.KO;
            } else if (Status.DEGRADED.equals(result.getStatus())) {
                lowest = Status.DEGRADED;
            }
        }
        return lowest;
    }
}
