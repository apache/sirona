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
package org.apache.sirona.cassandra.collector.gauge;

import me.prettyprint.cassandra.serializers.DoubleSerializer;
import me.prettyprint.cassandra.serializers.LongSerializer;
import me.prettyprint.cassandra.serializers.StringSerializer;
import me.prettyprint.hector.api.Keyspace;
import me.prettyprint.hector.api.beans.ColumnSlice;
import me.prettyprint.hector.api.beans.HColumn;
import me.prettyprint.hector.api.factory.HFactory;
import me.prettyprint.hector.api.query.QueryResult;
import org.apache.sirona.Role;
import org.apache.sirona.cassandra.collector.CassandraSirona;
import org.apache.sirona.configuration.ioc.IoCs;
import org.apache.sirona.counters.Unit;
import org.apache.sirona.store.gauge.CollectorGaugeDataStore;
import org.apache.sirona.store.gauge.GaugeValuesRequest;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import static org.apache.sirona.cassandra.collector.CassandraSirona.column;
import static org.apache.sirona.cassandra.collector.CassandraSirona.emptyColumn;
import static org.apache.sirona.cassandra.collector.CassandraSirona.keys;

public class CassandraCollectorGaugeDataStore implements CollectorGaugeDataStore {
    private final CassandraSirona cassandra;
    private final Keyspace keyspace;
    private final String valueFamily;
    private final String markerFamily;

    public CassandraCollectorGaugeDataStore() {
        this.cassandra = IoCs.findOrCreateInstance(CassandraSirona.class);
        this.keyspace = cassandra.getKeyspace();
        this.valueFamily = cassandra.getGaugeValuesColumnFamily();
        this.markerFamily = cassandra.getMarkerGaugesColumFamily();
    }

    private String id(final Role role, final String marker) { // order is really important here, see keyToRole()
        return cassandra.generateKey(role.getName(), role.getUnit().getName(), marker);
    }

    private Role keyToRole(final String key) {
        final String[] segments = key.split(cassandra.keySeparator());
        return new Role(segments[0], Unit.get(segments[1]));  // no need of segments[2] (= marker)
    }

    @Override
    public void createOrNoopGauge(final Role role, final String marker) {
        internalCreateOrNoopGauge(role, marker);
    }

    private String internalCreateOrNoopGauge(final Role role, final String marker) {
        final String id = id(role, marker);

        HFactory.createMutator(keyspace, StringSerializer.get())
            .addInsertion(marker, markerFamily, emptyColumn(id))
            .execute();

        return id;
    }

    @Override
    public void addToGauge(final Role role, final long time, final double value, final String marker) {
        HFactory.createMutator(keyspace, StringSerializer.get())
            .addInsertion(internalCreateOrNoopGauge(role, marker), valueFamily, column(time, value))
            .execute();
    }

    @Override
    public Collection<String> markers() {
        return keys(keyspace, markerFamily);
    }

    @Override
    public SortedMap<Long, Double> getGaugeValues(final GaugeValuesRequest gaugeValuesRequest, final String marker) {
        final QueryResult<ColumnSlice<Long, Double>> qResult =
            HFactory.createSliceQuery(keyspace, StringSerializer.get(), LongSerializer.get(), DoubleSerializer.get()) //
            .setKey(id(gaugeValuesRequest.getRole(), marker)) //
            .setColumnFamily(valueFamily) //
            .setRange(gaugeValuesRequest.getStart(), gaugeValuesRequest.getEnd(), false, Integer.MAX_VALUE) //
            .execute();

        final SortedMap<Long, Double> result = new TreeMap<Long, Double>();
        for (final HColumn<Long, Double> slide : qResult.get().getColumns()) {
            result.put(slide.getName(), slide.getValue());
        }

        return result;
    }

    @Override
    public SortedMap<Long, Double> getGaugeValues(final GaugeValuesRequest gaugeValuesRequest) {
        final SortedMap<Long, Double> result = new TreeMap<Long, Double>();

        for (final String marker : markers()) {
            for (final Map.Entry<Long, Double> values : getGaugeValues(gaugeValuesRequest, marker).entrySet()) {
                final Long key = values.getKey();

                Double d = result.get(key);
                if (d == null) {
                    d = 0.;
                }

                result.put(key, d + values.getValue());
            }
        }

        return result;
    }

    @Override
    public Collection<Role> gauges() {
        final Collection<Role> roles = new HashSet<Role>();
        for (final String key : keys(keyspace, valueFamily)) {
            roles.add(keyToRole(key));
        }
        return roles;
    }

    @Override
    public Role findGaugeRole(final String name) {
        for (final String key : keys(keyspace, valueFamily)) {
            final String[] segments = key.split(cassandra.keySeparator());
            if (segments[0].equals(name)) {
                return keyToRole(key);
            }
        }
        throw new IllegalArgumentException("role '" + name + "' not found");
    }

    @Override
    public void gaugeStopped(final Role gauge) {
        // no-op
    }
}
