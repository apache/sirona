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

import org.apache.sirona.cassandra.collector.counter.CassandraCollectorCounterDataStore;
import org.apache.sirona.cassandra.collector.gauge.CassandraCollectorGaugeDataStore;
import org.apache.sirona.cassandra.collector.status.CassandraCollectorNodeStatusDataStore;
import org.apache.sirona.cassandra.collector.pathtracking.CassandraPathTrackingDataStore;
import org.apache.sirona.configuration.ioc.IoCs;
import org.apache.sirona.store.DelegateDataStoreFactory;

public class CassandraCollectorDataStoreFactory extends DelegateDataStoreFactory {
    public CassandraCollectorDataStoreFactory() {
        super(
            IoCs.processInstance(new CassandraCollectorCounterDataStore()),
            IoCs.processInstance(new CassandraCollectorGaugeDataStore()),
            IoCs.processInstance(new CassandraCollectorNodeStatusDataStore()),
            IoCs.processInstance(new CassandraPathTrackingDataStore()));
    }
}
