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
package org.apache.sirona.cassandra.agent.status;

import org.apache.sirona.cassandra.collector.status.CassandraCollectorNodeStatusDataStore;
import org.apache.sirona.configuration.ioc.AutoSet;
import org.apache.sirona.configuration.ioc.Created;
import org.apache.sirona.status.NodeStatus;
import org.apache.sirona.store.status.PeriodicNodeStatusDataStore;
import org.apache.sirona.util.Localhosts;

import java.util.Map;
import java.util.logging.Logger;

@AutoSet
public class CassandraStatusDataStore extends PeriodicNodeStatusDataStore {
    private static final Logger LOGGER = Logger.getLogger(CassandraStatusDataStore.class.getName());

    private final CassandraCollectorNodeStatusDataStore delegate = new CassandraCollectorNodeStatusDataStore();
    protected String marker;
    protected boolean readFromStore = true;

    @Created
    protected void initMarkerIfNeeded() {
        if (marker == null) {
            marker = Localhosts.get();
        }
        LOGGER.warning("This storage used on app side can be a bit slow, maybe consider using a remote collector");
    }

    @Override
    protected void reportStatus(final NodeStatus nodeStatus) {
        delegate.store(marker, nodeStatus);
    }

    @Override
    public Map<String, NodeStatus> statuses() {
        final NodeStatus localStatus = status.get();
        if (readFromStore) {
            final Map<String, NodeStatus> statuses = delegate.statuses();
            if (localStatus == null) {
                return statuses;
            }
            statuses.put(marker, localStatus);
            listeners.notify(statuses);
            return statuses;
        }
        return super.statuses();
    }
}
