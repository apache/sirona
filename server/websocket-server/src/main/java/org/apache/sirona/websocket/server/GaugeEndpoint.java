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
import org.apache.sirona.Role;
import org.apache.sirona.configuration.ioc.IoCs;
import org.apache.sirona.counters.Unit;
import org.apache.sirona.repositories.Repository;
import org.apache.sirona.store.gauge.CollectorGaugeDataStore;
import org.apache.sirona.websocket.client.domain.WSGauge;

import javax.websocket.OnMessage;
import javax.websocket.server.ServerEndpoint;

@ServerEndpoint(value = "/wsirona/gauge", decoders = JohnzonTextDecoder.class)
public class GaugeEndpoint {
    @OnMessage
    public void onMessage(final WSGauge gauge) throws Exception {
        LazyDataStore.COLLECTOR_GAUGE_DATA_STORE.addToGauge(
            new Role(gauge.getRoleName(), Unit.get(gauge.getRoleUnit())),
            gauge.getTime(),
            gauge.getValue(),
            gauge.getMarker()
        );
    }

    private static class LazyDataStore {
        private static final CollectorGaugeDataStore COLLECTOR_GAUGE_DATA_STORE;
        static {
            Repository.INSTANCE.hashCode();
            COLLECTOR_GAUGE_DATA_STORE = IoCs.findOrCreateInstance(CollectorGaugeDataStore.class);
            if (COLLECTOR_GAUGE_DATA_STORE == null) {
                throw new IllegalStateException("Collector only works with " + CollectorGaugeDataStore.class.getName());
            }
        }
    }
}
