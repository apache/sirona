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
package org.apache.sirona.cassandra;

import me.prettyprint.cassandra.service.CassandraHost;
import me.prettyprint.hector.api.HConsistencyLevel;
import org.apache.sirona.configuration.ioc.AutoSet;

@AutoSet
public class CassandraBuilder {
    private String hosts = "localhost:" + CassandraHost.DEFAULT_PORT;
    private String cluster = "sirona-cluster";
    private String keyspace = "sirona";
    private String counterColumnFamily = "counters_values";
    private String gaugeValuesColumnFamily = "gauges_values";
    private String statusColumnFamily = "statuses_values";
    private String pathTrackingColumFamily = "path_tracking";
    private String markerCountersColumnFamily = "markers_counters";
    private String markerStatusesColumnFamily = "markers_statuses";
    private String markerGaugesColumFamily = "markers_gauges";
    private String markerPathTrackingColumFamily = "markers_path_tracking";
    private String writeConsistencyLevel = HConsistencyLevel.QUORUM.name();
    private String readConsistencyLevel = HConsistencyLevel.QUORUM.name();
    private int replicationFactor = 1;
    private int maxActive = CassandraHost.DEFAULT_MAX_ACTIVE;

    public String getHosts() {
        return hosts;
    }

    public String getCluster() {
        return cluster;
    }

    public String getKeyspace() {
        return keyspace;
    }

    public String getCounterColumnFamily() {
        return counterColumnFamily;
    }

    public String getGaugeValuesColumnFamily() {
        return gaugeValuesColumnFamily;
    }

    public String getStatusColumnFamily() {
        return statusColumnFamily;
    }

    public int getReplicationFactor() {
        return replicationFactor;
    }

    public String getMarkerGaugesColumFamily() {
        return markerGaugesColumFamily;
    }

    public String getMarkerCountersColumnFamily() {
        return markerCountersColumnFamily;
    }

    public String getMarkerStatusesColumnFamily() {
        return markerStatusesColumnFamily;
    }

    public String getPathTrackingColumFamily() {
        return pathTrackingColumFamily;
    }

    public String getMarkerPathTrackingColumFamily() {
        return markerPathTrackingColumFamily;
    }

    public HConsistencyLevel getWriteConsistencyLevel() {
        return HConsistencyLevel.valueOf(writeConsistencyLevel);
    }

    public HConsistencyLevel getReadConsistencyLevel() {
        return HConsistencyLevel.valueOf(readConsistencyLevel);
    }

    public int getMaxActive() {
        return maxActive;
    }

    public void setMaxActive(int maxActive) {
        this.maxActive = maxActive;
    }
}
