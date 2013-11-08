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

import org.apache.sirona.configuration.Configuration;

@Configuration.AutoSet
public class CassandraBuilder {
    private String hosts = "localhost:9171";
    private String cluster = "sirona-cluster";
    private String keyspace = "sirona";
    private String counterColumnFamily = "counters";
    private String gaugeColumnFamily = "gauges";
    private String statusColumnFamily = "statuses";
    private String markerCountersColumFamily = "markers_counters";
    private int replicationFactor = 1;

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

    public String getGaugeColumnFamily() {
        return gaugeColumnFamily;
    }

    public String getStatusColumnFamily() {
        return statusColumnFamily;
    }

    public int getReplicationFactor() {
        return replicationFactor;
    }

    public String getMarkerCountersColumFamily() {
        return markerCountersColumFamily;
    }
}
