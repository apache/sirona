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

import me.prettyprint.cassandra.serializers.StringSerializer;
import me.prettyprint.hector.api.Keyspace;
import me.prettyprint.hector.api.beans.HColumn;
import me.prettyprint.hector.api.beans.HSuperColumn;
import me.prettyprint.hector.api.beans.OrderedSuperRows;
import me.prettyprint.hector.api.beans.SuperRow;
import me.prettyprint.hector.api.beans.SuperSlice;
import me.prettyprint.hector.api.factory.HFactory;
import me.prettyprint.hector.api.mutation.Mutator;
import me.prettyprint.hector.api.query.QueryResult;
import org.apache.sirona.cassandra.collector.CassandraSirona;
import org.apache.sirona.configuration.Configuration;
import org.apache.sirona.status.NodeStatus;
import org.apache.sirona.status.Status;
import org.apache.sirona.status.ValidationResult;
import org.apache.sirona.store.status.CollectorNodeStatusDataStore;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import static org.apache.sirona.cassandra.collector.CassandraSirona.column;

public class CassandraCollectorNodeStatusDataStore implements CollectorNodeStatusDataStore {
    private final CassandraSirona cassandra;
    private final Keyspace keyspace;
    private final String family;

    public CassandraCollectorNodeStatusDataStore() {
        this.cassandra = Configuration.findOrCreateInstance(CassandraSirona.class);
        this.keyspace = cassandra.getKeyspace();
        this.family = cassandra.getStatusColumnFamily();
    }

    @Override
    public Map<String, NodeStatus> statuses() {
        final QueryResult<OrderedSuperRows<String, String, String, String>> mainResult =
            HFactory.createRangeSuperSlicesQuery(keyspace, StringSerializer.get(), StringSerializer.get(), StringSerializer.get(), StringSerializer.get())
                .setColumnFamily(family)
                .setRange(null, null, false, Integer.MAX_VALUE)
                .setKeys("", "")
                .execute();

        if (mainResult == null) {
            return Collections.emptyMap();
        }

        final Map<String, NodeStatus> statuses = new HashMap<String, NodeStatus>();
        for (final SuperRow<String, String, String, String> status : mainResult.get()) {
            final SuperSlice<String,String,String> superSlice = status.getSuperSlice();

            final Collection<ValidationResult> validations = new LinkedList<ValidationResult>();
            for (final HSuperColumn<String, String, String> column : superSlice.getSuperColumns()) {
                validations.add(new ValidationResult(column.getName(), Status.valueOf(column.getSubColumnByName("status").getValue()), column.getSubColumnByName("description").getValue()));
            }
            statuses.put(status.getKey(), new NodeStatus(validations.toArray(new ValidationResult[validations.size()])));
        }
        return statuses;
    }

    @Override // TODO: like clearCounters() see if it should do something or not
    public void reset() {
        // no-op
    }

    @Override
    public void store(final String node, final NodeStatus status) {
        final Mutator<String> mutator = HFactory.createMutator(keyspace, StringSerializer.get());
        for (final ValidationResult validationResult : status.getResults()) {
            mutator.addInsertion(node, family,
                HFactory.createSuperColumn(validationResult.getName(),
                    Arrays.<HColumn<String, String>>asList(
                        column("description", validationResult.getMessage()),
                        column("status", validationResult.getStatus().name())
                    ),
                    StringSerializer.get(), StringSerializer.get(), StringSerializer.get()));
        }
        mutator.execute();
    }
}
