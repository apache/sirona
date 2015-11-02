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
package org.apache.sirona.cassandra.collector.status;

import me.prettyprint.cassandra.serializers.DateSerializer;
import me.prettyprint.cassandra.serializers.StringSerializer;
import me.prettyprint.hector.api.Keyspace;
import me.prettyprint.hector.api.beans.ColumnSlice;
import me.prettyprint.hector.api.beans.HColumn;
import me.prettyprint.hector.api.beans.OrderedRows;
import me.prettyprint.hector.api.beans.Row;
import me.prettyprint.hector.api.factory.HFactory;
import me.prettyprint.hector.api.mutation.Mutator;
import me.prettyprint.hector.api.query.QueryResult;
import org.apache.sirona.cassandra.collector.CassandraSirona;
import org.apache.sirona.configuration.ioc.IoCs;
import org.apache.sirona.status.NodeStatus;
import org.apache.sirona.status.Status;
import org.apache.sirona.status.ValidationResult;
import org.apache.sirona.store.status.CollectorBaseNodeStatusDataStore;

import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;

import static org.apache.sirona.cassandra.collector.CassandraSirona.column;

public class CassandraCollectorNodeStatusDataStore extends CollectorBaseNodeStatusDataStore {
    private final Keyspace keyspace;
    private final String family;
    private final String markerFamily;
    private final CassandraSirona cassandra;

    public CassandraCollectorNodeStatusDataStore() {
        this.cassandra = IoCs.findOrCreateInstance(CassandraSirona.class);
        this.keyspace = cassandra.getKeyspace();
        this.family = cassandra.getStatusColumnFamily();
        this.markerFamily = cassandra.getMarkerStatusesColumnFamily();
    }

    @Override
    public Map<String, NodeStatus> statuses() {
        final QueryResult<OrderedRows<String, String,Date>> result =
            HFactory.createRangeSlicesQuery(keyspace, StringSerializer.get(), StringSerializer.get(), DateSerializer.get())
                .setColumnFamily(markerFamily)
                .setRange(null, null, false, Integer.MAX_VALUE)
                .execute();

        if (result == null || result.get() == null) {
            return null;
        }

        final Map<String, NodeStatus> statuses = new TreeMap<String, NodeStatus>();
        for (final Row<String, String, Date> status : result.get()) {
            final Collection<ValidationResult> validations = new LinkedList<ValidationResult>();

            Date maxDate = null;
            for (final HColumn<String, Date> col : status.getColumnSlice().getColumns()) {
                final QueryResult<ColumnSlice<String, String>> subResult =
                    HFactory.createSliceQuery(keyspace, StringSerializer.get(), StringSerializer.get(), StringSerializer.get())
                        .setColumnFamily(family)
                        .setRange(null, null, false, Integer.MAX_VALUE)
                        .setKey(col.getName())
                        .execute();

                if (subResult == null || subResult.get() == null) {
                    continue;
                }

                final Date value = col.getValue();
                if (maxDate == null || value == null) {
                    maxDate = value;
                } else if (maxDate.compareTo(value) < 0) {
                    maxDate = value;
                }

                final ColumnSlice<String, String> slice = subResult.get();
                validations.add(new ValidationResult(
                    slice.getColumnByName("name").getValue(),
                    Status.valueOf(slice.getColumnByName("status").getValue()),
                    slice.getColumnByName("description").getValue()));
            }
            statuses.put(status.getKey(), new NodeStatus(validations.toArray(new ValidationResult[validations.size()]), maxDate));
        }
        return statuses;
    }

    @Override
    public void reset() { // TODO: like clearCounters() see if it should do something or not
        super.reset();
    }

    @Override
    public void store(final String node, final NodeStatus status) {
        final Mutator<String> mutator = HFactory.createMutator(keyspace, StringSerializer.get());
        for (final ValidationResult validationResult : status.getResults()) {
            final String id = cassandra.generateKey(node, validationResult.getName());
            mutator.addInsertion(node, markerFamily, column(id, status.getDate()))
                .addInsertion(id, family, column("name", validationResult.getName()))
                .addInsertion(id, family, column("description", validationResult.getMessage()))
                .addInsertion(id, family, column("status", validationResult.getStatus().name()));
        }
        mutator.execute();
    }
}
