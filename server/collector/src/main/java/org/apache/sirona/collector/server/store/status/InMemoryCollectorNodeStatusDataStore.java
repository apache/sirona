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
package org.apache.sirona.collector.server.store.status;

import org.apache.sirona.status.NodeStatus;
import org.apache.sirona.store.status.CollectorNodeStatusDataStore;
import org.apache.sirona.store.status.NodeStatusDataStore;

import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryCollectorNodeStatusDataStore implements CollectorNodeStatusDataStore {
    private final Map<String, NodeStatus> statuses = new ConcurrentHashMap<String, NodeStatus>();

    @Override
    public Map<String, NodeStatus> statuses() {
        return new TreeMap<String, NodeStatus>(statuses);
    }

    @Override
    public void reset() {
        statuses.clear();
    }

    @Override
    public void store(final String node, final NodeStatus status) {
        statuses.put(node, status);
    }
}
