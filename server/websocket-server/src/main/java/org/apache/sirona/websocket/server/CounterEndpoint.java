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
import org.apache.sirona.counters.Counter;
import org.apache.sirona.counters.Unit;
import org.apache.sirona.math.M2AwareStatisticalSummary;
import org.apache.sirona.repositories.Repository;
import org.apache.sirona.store.counter.CollectorCounterStore;
import org.apache.sirona.websocket.client.domain.WSCounter;

import javax.websocket.OnMessage;
import javax.websocket.server.ServerEndpoint;

@ServerEndpoint(value = "/wsirona/counter", decoders = JohnzonTextDecoder.class)
public class CounterEndpoint {
    @OnMessage
    public void onMessage(final WSCounter counter) throws Exception {
        LazyDataStore.COLLECTOR_COUNTER_STORE.update(
            new Counter.Key(new Role(counter.getRoleName(), Unit.get(counter.getRoleUnit())), counter.getName()),
            counter.getMarker(),
            new M2AwareStatisticalSummary(
                counter.getMean(), counter.getVariance(), counter.getHits(), counter.getMax(), counter.getMin(), counter.getSum(), counter.getSecondMoment()),
            counter.getConcurrency()
        );
    }

    private static class LazyDataStore {
        private static final CollectorCounterStore COLLECTOR_COUNTER_STORE;
        static {
            IoCs.findOrCreateInstance(Repository.class);
            COLLECTOR_COUNTER_STORE = IoCs.findOrCreateInstance(CollectorCounterStore.class);
            if (COLLECTOR_COUNTER_STORE == null) {
                throw new IllegalStateException("Collector only works with " + CollectorCounterStore.class.getName());
            }
        }
    }
}
