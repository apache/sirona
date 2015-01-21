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
import me.prettyprint.hector.api.beans.ColumnSlice;
import me.prettyprint.hector.api.beans.HColumn;
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
import org.apache.sirona.store.counter.LeafCollectorCounter;
import org.apache.sirona.store.memory.counter.InMemoryCollectorCounterStore;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import static org.apache.sirona.cassandra.collector.CassandraSirona.keys;
import static org.apache.sirona.cassandra.collector.CassandraSirona.column;
import static org.apache.sirona.cassandra.collector.CassandraSirona.getOrDefault;
import static org.apache.sirona.cassandra.collector.CassandraSirona.emptyColumn;

public class CassandraCollectorCounterDataStore extends InMemoryCollectorCounterStore
{
    private final Keyspace keyspace;
    private final String family;
    private final String markerFamily;
    private final CassandraSirona cassandra;

    public CassandraCollectorCounterDataStore() {
        this.cassandra = IoCs.findOrCreateInstance(CassandraSirona.class);
        this.keyspace = cassandra.getKeyspace();
        this.markerFamily = cassandra.getMarkerCountersColumnFamily();
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
        final QueryResult<ColumnSlice<String, String>> qResult =
            HFactory
                .createSliceQuery(keyspace, StringSerializer.get(), StringSerializer.get(), StringSerializer.get()) //
                .setKey(marker) //
                .setColumnFamily(markerFamily) //
                .setRange(null, null, false, Integer.MAX_VALUE) //
                .execute();

        final Collection<CassandraLeafCounter> counters = new LinkedList<CassandraLeafCounter>();
        if (qResult == null || qResult.get() == null) {
            return counters;
        }

        for (final HColumn<String, String> colum : qResult.get().getColumns()) {
            final String[] segments = colum.getName().split(cassandra.keySeparator());
            final Counter.Key ckey = new Counter.Key(new Role(segments[0], Unit.get(segments[1])), segments[2]);
            counters.add(findByKey(ckey, marker));
        }

        return counters;
    }

    @Override
    public Collection<String> markers() {
        return keys(keyspace, markerFamily);
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
        final DynamicDelegatedSerializer<Object> serializer = new DynamicDelegatedSerializer<Object>();

        final QueryResult<ColumnSlice<String, Object>> result =
            HFactory.createSliceQuery(keyspace, StringSerializer.get(), StringSerializer.get(), serializer)
                .setColumnFamily(family)
                .setRange(null, null, false, Integer.MAX_VALUE)
                .setKey(id(ckey, marker))
                .execute();

        if (result == null || result.get() == null || result.get().getColumns().isEmpty()) {
            return null;
        }

        return counter(ckey, serializer, result.get(), marker);
    }

    protected CassandraLeafCounter counter(final Counter.Key ckey,
                                           final DynamicDelegatedSerializer<Object> serializer,
                                           final ColumnSlice<String, Object> map,
                                           final String marker) {
        return new CassandraLeafCounter(ckey, this, marker)
            .sync(new M2AwareStatisticalSummary(
                getOrDefault( serializer, map.getColumnByName( "mean" ), DoubleSerializer.get() ).doubleValue(),
                getOrDefault( serializer, map.getColumnByName( "variance" ), DoubleSerializer.get() ).doubleValue(),
                getOrDefault( serializer, map.getColumnByName( "n" ), LongSerializer.get() ).longValue(),
                getOrDefault( serializer, map.getColumnByName( "max" ), DoubleSerializer.get() ).doubleValue(),
                getOrDefault( serializer, map.getColumnByName( "min" ), DoubleSerializer.get() ).doubleValue(),
                getOrDefault( serializer, map.getColumnByName( "sum" ), DoubleSerializer.get() ).doubleValue(),
                getOrDefault( serializer, map.getColumnByName( "m2" ), DoubleSerializer.get() ).doubleValue()),
                getOrDefault( serializer, map.getColumnByName( "maxConcurrency" ),
                                              IntegerSerializer.get() ).intValue());
    }

    protected CassandraLeafCounter save(final CassandraLeafCounter counter, final String marker) {
        final Counter.Key key = counter.getKey();
        final String id = id(key, marker);

        HFactory.createMutator(keyspace, StringSerializer.get())
            // counter values
            .addInsertion(id, family, column("role", key.getRole().getName()))
            .addInsertion(id, family, column("key", key.getName()))
            .addInsertion(id, family, column("maxConcurrency", counter.getMaxConcurrency()))
            .addInsertion(id, family, column("variance", counter.getVariance()))
            .addInsertion(id, family, column("n", counter.getHits()))
            .addInsertion(id, family, column("max", counter.getMax()))
            .addInsertion(id, family, column("min", counter.getMin()))
            .addInsertion(id, family, column("sum", counter.getSum()))
            .addInsertion(id, family, column("m2", counter.getSecondMoment()))
            .addInsertion(id, family, column("mean", counter.getMean()))
            // counter in marker
            .addInsertion(marker, markerFamily, emptyColumn(id))
            //save it
            .execute();

        return counter;
    }

    protected String id(final Counter.Key key, final String marker) {
        return cassandra.generateKey(key.getRole().getName(), key.getRole().getUnit().getName(), key.getName(), marker);
    }

    public CassandraSirona getCassandra() {
        return cassandra;
    }


}
