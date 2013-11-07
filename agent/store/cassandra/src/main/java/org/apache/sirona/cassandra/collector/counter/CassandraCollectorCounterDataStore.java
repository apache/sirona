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
package org.apache.sirona.cassandra.collector.counter;

import org.apache.sirona.collector.server.store.counter.InMemoryCollectorCounterStore;
import org.apache.sirona.collector.server.store.counter.LeafCollectorCounter;
import org.apache.sirona.counters.Counter;

import java.util.Collection;

public class CassandraCollectorCounterDataStore extends InMemoryCollectorCounterStore {
    private final CounterDao dao;

    public CassandraCollectorCounterDataStore() {
        dao = new CounterDao();
    }

    @Override
    public LeafCollectorCounter getOrCreateCounter(final Counter.Key key, final String marker) {
        final CassandraLeafCounter byKey = dao.findByKey(key, marker);
        if (byKey != null) {
            return byKey;
        }
        return dao.save(new CassandraLeafCounter(key), marker);
    }

    @Override
    public Collection<String> markers() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Collection<? extends Counter> getCounters(final String marker) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clearCounters() {
        throw new UnsupportedOperationException();
    }
}
