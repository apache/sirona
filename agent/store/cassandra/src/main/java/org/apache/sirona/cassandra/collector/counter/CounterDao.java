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
import me.prettyprint.cassandra.serializers.SerializerTypeInferer;
import me.prettyprint.cassandra.serializers.StringSerializer;
import me.prettyprint.hector.api.Keyspace;
import me.prettyprint.hector.api.Serializer;
import me.prettyprint.hector.api.beans.ColumnSlice;
import me.prettyprint.hector.api.beans.HColumn;
import me.prettyprint.hector.api.factory.HFactory;
import me.prettyprint.hector.api.query.QueryResult;
import me.prettyprint.hector.api.query.SliceQuery;
import org.apache.sirona.cassandra.DynamicDelegatedSerializer;
import org.apache.sirona.cassandra.collector.CassandraSirona;
import org.apache.sirona.configuration.Configuration;
import org.apache.sirona.counters.Counter;
import org.apache.sirona.math.M2AwareStatisticalSummary;

public class CounterDao {
    private static final String[] FIND_COLUMNS = new String[] { "maxConcurrency", "variance", "n", "max", "min", "sum", "m2", "mean" };

    private final Keyspace keyspace;
    private final String family;
    private final CassandraSirona cassandra;

    public CounterDao() {
        this.cassandra = Configuration.findOrCreateInstance(CassandraSirona.class);
        this.keyspace = cassandra.getKeyspace();
        this.family = cassandra.getCounterColumnFamily();
    }

    public CassandraLeafCounter findByKey(final Counter.Key ckey, final String marker) {
        final String key = cassandra.generateKey(ckey.getRole().getName(), ckey.getName(), marker);

        final DynamicDelegatedSerializer<Number> serializer = new DynamicDelegatedSerializer<Number>();
        final SliceQuery<String, String, Number> q = HFactory.createSliceQuery(keyspace,
            StringSerializer.get(), StringSerializer.get(), serializer);

        final QueryResult<ColumnSlice<String, Number>> result = q.setKey(key)
            .setColumnNames(FIND_COLUMNS)
            .setColumnFamily(family)
            .execute();

        final ColumnSlice<String, Number> map = result.get();
        if (map.getColumns().isEmpty()) {
            return null;
        }

        final CassandraLeafCounter counter = new CassandraLeafCounter(ckey);
        counter.sync(new M2AwareStatisticalSummary(
            getOrZero(serializer, map.getColumnByName("mean"), DoubleSerializer.get()).doubleValue(),
            getOrZero(serializer, map.getColumnByName("variance"), DoubleSerializer.get()).doubleValue(),
            getOrZero(serializer, map.getColumnByName("n"), LongSerializer.get()).longValue(),
            getOrZero(serializer, map.getColumnByName("max"), DoubleSerializer.get()).doubleValue(),
            getOrZero(serializer, map.getColumnByName("min"), DoubleSerializer.get()).doubleValue(),
            getOrZero(serializer, map.getColumnByName("sum"), DoubleSerializer.get()).doubleValue(),
            getOrZero(serializer, map.getColumnByName("m2"), DoubleSerializer.get()).doubleValue()),
            getOrZero(serializer, map.getColumnByName("maxConcurrency"), IntegerSerializer.get()).intValue());
        return counter;
    }

    public CassandraLeafCounter save(final CassandraLeafCounter counter, final String marker) {
        final Counter.Key key = counter.getKey();
        final String id = id(key, marker);

        HFactory.createMutator(keyspace, StringSerializer.get())
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
            .addInsertion(id, family, column("marker", marker))
            .execute();

        return counter;
    }

    private String id(final Counter.Key key, final String marker) {
        return cassandra.generateKey(key.getRole().getName(), key.getName(), marker);
    }

    private static Number getOrZero(final DynamicDelegatedSerializer<Number> delegatedSerializer, final HColumn<String, Number> col, final Serializer<? extends Number> serializer) {
        delegatedSerializer.setDelegate(serializer);
        if (col == null || col.getValue() == null) {
            return 0;
        }
        return col.getValue();
    }

    private static <B> HColumn<String, B> column(final String name, final B value) {
        return HFactory.createColumn(name, value, StringSerializer.get(), (Serializer<B>) SerializerTypeInferer.getSerializer(value));
    }
}
