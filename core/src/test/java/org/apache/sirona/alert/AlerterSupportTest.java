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
import org.apache.sirona.status.Status;
import org.apache.sirona.status.ValidationResult;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class AlerterSupportTest {
    @Test
    public void alert() {
        final Map<String, NodeStatus> nodeStatus = new HashMap<String, NodeStatus>();

        final AlerterSupport support = new AlerterSupport();
        support.notify(nodeStatus); // all is empty but no exception

        // add a listener
        final Collection<AlertListener.Alert> alerts = new ArrayList<AlertListener.Alert>();
        support.addAlerter(new AlertListener() {
            public void onAlert(final Alert alert) {
                alerts.add(alert);
            }
        });

        support.notify(nodeStatus); // no status so no alert
        assertEquals(0, alerts.size());

        // now add a OK status
        nodeStatus.put("host1", new NodeStatus(new ValidationResult[] { new ValidationResult("v1", Status.OK, "")}, new Date()));
        support.notify(nodeStatus); // only 1 OK status so no alert
        assertEquals(0, alerts.size());

        // now add another OK status
        nodeStatus.put("host2", new NodeStatus(new ValidationResult[] { new ValidationResult("v2", Status.OK, "")}, new Date()));
        support.notify(nodeStatus); // only OK status so no alert
        assertEquals(0, alerts.size());

        // add a KO status so one alert
        nodeStatus.put("host1", new NodeStatus(new ValidationResult[] { new ValidationResult("v2", Status.KO, "")}, new Date()));
        support.notify(nodeStatus); // only OK status so no alert
        assertEquals(1, alerts.size());
        alerts.clear();

        // add a DEGRADED status so one alert
        nodeStatus.put("host1", new NodeStatus(new ValidationResult[] { new ValidationResult("v2", Status.DEGRADED, "")}, new Date()));
        support.notify(nodeStatus); // only OK status so no alert
        assertEquals(1, alerts.size());
        alerts.clear();

        // add a DEGRADED and a KO status so one alert
        nodeStatus.put("host1", new NodeStatus(new ValidationResult[] { new ValidationResult("v2", Status.DEGRADED, "")}, new Date()));
        nodeStatus.put("host2", new NodeStatus(new ValidationResult[] { new ValidationResult("v1", Status.KO, "")}, new Date()));
        support.notify(nodeStatus); // only OK status so no alert
        assertEquals(2, alerts.size());
        alerts.clear();
    }
}
