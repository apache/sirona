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
package org.apache.sirona.websocket.client.domain;

import org.apache.sirona.Role;
import org.apache.sirona.store.gauge.BatchGaugeDataStoreAdapter;

public class WSGauge extends WSDomain {
    private String roleName;
    private String roleUnit;
    private long time;
    private double value;

    public WSGauge() {
        // no-op
    }

    public WSGauge(final Role key, final BatchGaugeDataStoreAdapter.Measure measure, final String marker) {
        super("gauge", marker);
        roleName = key.getName();
        roleUnit = key.getUnit().getName();
        time = measure.getTime();
        value = measure.getValue();
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(final String roleName) {
        this.roleName = roleName;
    }

    public String getRoleUnit() {
        return roleUnit;
    }

    public void setRoleUnit(final String roleUnit) {
        this.roleUnit = roleUnit;
    }

    public long getTime() {
        return time;
    }

    public void setTime(final long time) {
        this.time = time;
    }

    public double getValue() {
        return value;
    }

    public void setValue(final double value) {
        this.value = value;
    }
}
