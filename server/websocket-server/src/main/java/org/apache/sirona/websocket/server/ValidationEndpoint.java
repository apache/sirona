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
package org.apache.sirona.websocket.server;

import org.apache.johnzon.websocket.mapper.JohnzonTextDecoder;
import org.apache.sirona.configuration.ioc.IoCs;
import org.apache.sirona.repositories.Repository;
import org.apache.sirona.status.NodeStatus;
import org.apache.sirona.status.ValidationResult;
import org.apache.sirona.store.status.CollectorNodeStatusDataStore;
import org.apache.sirona.websocket.client.domain.WSValidation;

import javax.websocket.OnMessage;
import javax.websocket.server.ServerEndpoint;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

import static java.util.Arrays.asList;

// TODO: think to make it in batch
@ServerEndpoint(value = "/wsirona/validation", decoders = JohnzonTextDecoder.class)
public class ValidationEndpoint {
    @OnMessage
    public void onMessage(final WSValidation validation) throws Exception {
        final Map<String, NodeStatus> current = LazyDataStore.COLLECTOR_NODE_STATUS_DATA_STORE.statuses();
        final NodeStatus ns = current.get(validation.getMarker());
        final ValidationResult newVr = new ValidationResult(validation.getName(), validation.getStatus(), validation.getMessage());
        final ValidationResult[] newResults;
        if (ns != null) {
            final Collection<ValidationResult> results = new ArrayList<ValidationResult>(asList(ns.getResults()));
            for (final ValidationResult vr : ns.getResults()) {
                if (vr.getName().equals(validation.getName())) {
                    // replace it
                    results.remove(vr);
                    break;
                }
            }

            // not found so add it
            results.add(newVr);
            newResults = results.toArray(new ValidationResult[results.size()]);
        } else {
            newResults = new ValidationResult[] { newVr };
        }
        LazyDataStore.COLLECTOR_NODE_STATUS_DATA_STORE.store(
            validation.getMarker(), new NodeStatus(newResults, validation.getDate() == null ? new Date() : validation.getDate()));
    }

    private static class LazyDataStore {
        private static final CollectorNodeStatusDataStore COLLECTOR_NODE_STATUS_DATA_STORE;
        static {
            IoCs.findOrCreateInstance(Repository.class);
            COLLECTOR_NODE_STATUS_DATA_STORE = IoCs.findOrCreateInstance(CollectorNodeStatusDataStore.class);
            if (COLLECTOR_NODE_STATUS_DATA_STORE == null) {
                throw new IllegalStateException("Collector only works with " + CollectorNodeStatusDataStore.class.getName());
            }
        }
    }
}
