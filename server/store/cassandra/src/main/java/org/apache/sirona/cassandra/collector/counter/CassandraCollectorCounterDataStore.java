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

import me.prettyprint.cassandra.serializers.DoubleSerializer;
import me.prettyprint.cassandra.serializers.IntegerSerializer;
import me.prettyprint.cassandra.serializers.LongSerializer;
import me.prettyprint.cassandra.serializers.StringSerializer;
import me.prettyprint.hector.api.Keyspace;
import me.prettyprint.hector.api.Serializer;
import me.prettyprint.hector.api.beans.HColumn;
import me.prettyprint.hector.api.beans.HSuperColumn;
import me.prettyprint.hector.api.beans.OrderedSuperRows;
import me.prettyprint.hector.api.beans.SuperRow;
import me.prettyprint.hector.api.factory.HFactory;
import me.prettyprint.hector.api.query.QueryResult;
import org.apache.sirona.Role;
import org.apache.sirona.cassandra.DynamicDelegatedSerializer;
import org.apache.sirona.cassandra.collector.CassandraSirona;
import org.apache.sirona.configuration.ioc.IoCs;
import org.apache.sirona.counters.Counter;
import org.apache.sirona.counters.Unit;
import org.apache.sirona.math.M2AwareStatisticalSummary;
import org.apache.sirona.store.counter.AggregatedCollectorCounter;
import org.apache.sirona.store.counter.InMemoryCollectorCounterStore;
import org.apache.sirona.store.counter.LeafCollectorCounter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.apache.sirona.cassandra.collector.CassandraSirona.column;
import static org.apache.sirona.cassandra.collector.CassandraSirona.keys;

public class CassandraCollectorCounterDataStore extends InMemoryCollectorCounterStore {
    private final Keyspace keyspace;
    private final String family;
    private final CassandraSirona cassandra;

    public CassandraCollectorCounterDataStore() {
        this.cassandra = IoCs.findOrCreateInstance(CassandraSirona.class);
        this.keyspace = cassandra.getKeyspace();
        this.family = cassandra.getCounterColumnFamily();
    }

    @Override
    public LeafCollectorCounter getOrCreateCounter(final Counter.Key key, final String marker) {
        final CassandraLeafCounter byKey = findByKey(key, marker);
        if (byKey != null) {
            return byKey;
        }
        return save(new CassandraLeafCounter(key, this, marker), marker);
    }

    @Override
    public Collection<? extends LeafCollectorCounter> getCounters(final String marker) {
        final DynamicDelegatedSerializer<Object> dynamicSerializer = new DynamicDelegatedSerializer<Object>();
        final QueryResult<OrderedSuperRows<String, String, String, Object>> mainResult =
            HFactory.createRangeSuperSlicesQuery(keyspace, StringSerializer.get(), StringSerializer.get(), StringSerializer.get(), dynamicSerializer)
                .setColumnFamily(family)
                .setRange(null, null, false, Integer.MAX_VALUE)
                .setKeys(marker, marker)
                .execute();

        if (mainResult == null) {
            return Collections.<LeafCollectorCounter>emptyList();
        }

        final Collection<CassandraLeafCounter> counters = new LinkedList<CassandraLeafCounter>();
        for (final SuperRow<String, String, String, Object> c : mainResult.get()) {
            for (final HSuperColumn<String, String, Object> col : c.getSuperSlice().getSuperColumns()) {
                final String[] segments = col.getName().split(cassandra.keySeparator());
                final Counter.Key ckey = new Counter.Key(new Role(segments[0], Unit.get(segments[1])), segments[2]);
                counters.add(counter(ckey, dynamicSerializer, col, marker));
            }
        }
        return counters;
    }

    @Override
    public Collection<String> markers() {
        return keys(keyspace, family);
    }

    @Override // TODO: see if we shouldn't store it or if aggregation can be done on java side
    public AggregatedCollectorCounter getOrCreateCounter(final Counter.Key key) {
        final Map<String, LeafCollectorCounter> counters = new HashMap<String, LeafCollectorCounter>();
        for (final String marker : markers()) {
            final LeafCollectorCounter c = getOrCreateCounter(key, marker);
            counters.put(marker, c);
        }
        return new AggregatedCollectorCounter(key, counters);
    }

    @Override // TODO: see if we shouldn't store it or if aggregation can be done on java side
    public Collection<Counter> getCounters() {
        final Map<Counter.Key, Map<String, LeafCollectorCounter>> counters = new HashMap<Counter.Key, Map<String, LeafCollectorCounter>>();
        for (final String marker : markers()) {
            for (final LeafCollectorCounter c : getCounters(marker)) {
                Map<String, LeafCollectorCounter> values = counters.get(c.getKey());
                if (values == null) {
                    values = new HashMap<String, LeafCollectorCounter>();
                    counters.put(c.getKey(), values);
                }
                values.put(marker, c);
            }
        }

        final Collection<Counter> c = new LinkedList<Counter>();
        for (final Map.Entry<Counter.Key, Map<String, LeafCollectorCounter>> entry : counters.entrySet()) {
            c.add(new AggregatedCollectorCounter(entry.getKey(), entry.getValue()));
        }

        return c;
    }

    @Override
    public void update(final Counter.Key key, final String marker, final M2AwareStatisticalSummary stats, final int concurrency) {
        save(new CassandraLeafCounter(key, this, marker).sync(stats, concurrency), marker);
    }

    @Override // TODO: should we really clear counters or use a timestamp or a flag?
    public void clearCounters() {
        // no-op
    }

    protected CassandraLeafCounter findByKey(final Counter.Key ckey, final String marker) {
        final String key = id(ckey, marker);

        final DynamicDelegatedSerializer<Object> serializer = new DynamicDelegatedSerializer<Object>();

        final HSuperColumn<String, String, Object> result =
            HFactory.createSuperColumnQuery(keyspace, StringSerializer.get(), StringSerializer.get(), StringSerializer.get(), serializer)
                .setColumnFamily(family)
                .setKey(marker)
                .setSuperName(key)
                .execute()
                .get();

        if (result == null) {
            return null;
        }

        return counter(ckey, serializer, result, marker);
    }

    protected CassandraLeafCounter counter(final Counter.Key ckey,
                                           final DynamicDelegatedSerializer<Object> serializer,
                                           final HSuperColumn<String, String, Object> map,
                                           final String marker) {
        return new CassandraLeafCounter(ckey, this, marker)
            .sync(new M2AwareStatisticalSummary(
                getOrDefault(serializer, map.getSubColumnByName("mean"), DoubleSerializer.get()).doubleValue(),
                getOrDefault(serializer, map.getSubColumnByName("variance"), DoubleSerializer.get()).doubleValue(),
                getOrDefault(serializer, map.getSubColumnByName("n"), LongSerializer.get()).longValue(),
                getOrDefault(serializer, map.getSubColumnByName("max"), DoubleSerializer.get()).doubleValue(),
                getOrDefault(serializer, map.getSubColumnByName("min"), DoubleSerializer.get()).doubleValue(),
                getOrDefault(serializer, map.getSubColumnByName("sum"), DoubleSerializer.get()).doubleValue(),
                getOrDefault(serializer, map.getSubColumnByName("m2"), DoubleSerializer.get()).doubleValue()),
                getOrDefault(serializer, map.getSubColumnByName("maxConcurrency"), IntegerSerializer.get()).intValue());
    }

    protected CassandraLeafCounter save(final CassandraLeafCounter counter, final String marker) {
        final Counter.Key key = counter.getKey();
        final String id = id(key, marker);

        final List<HColumn<String, Object>> columns = new ArrayList<HColumn<String, Object>>();
        columns.add(HColumn.class.cast(column("role", key.getRole().getName())));
        columns.add(HColumn.class.cast(column("key", key.getName())));
        columns.add(HColumn.class.cast(column("maxConcurrency", counter.getMaxConcurrency())));
        columns.add(HColumn.class.cast(column("variance", counter.getVariance())));
        columns.add(HColumn.class.cast(column("n", counter.getHits())));
        columns.add(HColumn.class.cast(column("max", counter.getMax())));
        columns.add(HColumn.class.cast(column("min", counter.getMin())));
        columns.add(HColumn.class.cast(column("sum", counter.getSum())));
        columns.add(HColumn.class.cast(column("m2", counter.getSecondMoment())));
        columns.add(HColumn.class.cast(column("mean", counter.getMean())));

        HFactory.createMutator(keyspace, StringSerializer.get())
            .addInsertion(marker, family,
                HFactory.createSuperColumn(id, columns, StringSerializer.get(), StringSerializer.get(), new DynamicDelegatedSerializer<Object>()))
            .execute();

        return counter;
    }

    protected String id(final Counter.Key key, final String marker) {
        return cassandra.generateKey(key.getRole().getName(), key.getRole().getUnit().getName(), key.getName(), marker);
    }

    protected static Number getOrDefault(final DynamicDelegatedSerializer delegatedSerializer, final HColumn<?, ?> col, final Serializer<?> serializer) {
        delegatedSerializer.setDelegate(serializer);
        if (col == null || col.getValue() == null) {
            if (DoubleSerializer.get() == serializer) {
                return Double.NaN;
            }
            return 0;
        }

        final Object value = col.getValue();
        if (Number.class.isInstance(value)) {
            return Number.class.cast(value);
        }
        throw new IllegalArgumentException("not a number " + value);
    }
}
