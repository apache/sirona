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
package org.apache.sirona.cassandra.collector;

import me.prettyprint.cassandra.service.CassandraHostConfigurator;
import me.prettyprint.cassandra.service.ThriftKsDef;
import me.prettyprint.hector.api.Cluster;
import me.prettyprint.hector.api.Keyspace;
import me.prettyprint.hector.api.ddl.ColumnFamilyDefinition;
import me.prettyprint.hector.api.ddl.ComparatorType;
import me.prettyprint.hector.api.factory.HFactory;
import org.apache.sirona.cassandra.CassandraBuilder;
import org.apache.sirona.configuration.Configuration;

import java.util.logging.Logger;

import static java.util.Arrays.asList;

public class CassandraSirona {
    private static final Logger LOGGER = Logger.getLogger(CassandraSirona.class.getName());

    private static final String SEPARATOR = "->";

    private final CassandraBuilder builder = Configuration.findOrCreateInstance(CassandraBuilder.class);
    private final Cluster cluster;
    private final Keyspace keyspace;

    public CassandraSirona() {
        final CassandraHostConfigurator configurator = new CassandraHostConfigurator(builder.getHosts());
        configurator.setMaxActive(75);
        cluster = HFactory.getOrCreateCluster(builder.getCluster(), configurator);

        final String keyspaceName = builder.getKeyspace();

        keyspace = HFactory.createKeyspace(keyspaceName, cluster);

        final ColumnFamilyDefinition counters = HFactory.createColumnFamilyDefinition(keyspaceName, builder.getCounterColumnFamily(), ComparatorType.UTF8TYPE);
        final ColumnFamilyDefinition gauges = HFactory.createColumnFamilyDefinition(keyspaceName, builder.getGaugeColumnFamily(), ComparatorType.UTF8TYPE);
        final ColumnFamilyDefinition statuses = HFactory.createColumnFamilyDefinition(keyspaceName, builder.getStatusColumnFamily(), ComparatorType.UTF8TYPE);

        { // ensure keyspace exists, here if the keyspace doesn't exist we suppose nothing exist
            if (cluster.describeKeyspace(keyspaceName) == null) {
                LOGGER.info("Creating Cassandra '" + keyspaceName + "' keyspace.");
                cluster.addKeyspace(
                    HFactory.createKeyspaceDefinition(keyspaceName, ThriftKsDef.DEF_STRATEGY_CLASS, builder.getReplicationFactor(),
                        asList(counters, gauges, statuses)));
            }
        }
    }

    public String generateKey(final String... bases) {
        final StringBuilder builder = new StringBuilder();
        if (bases == null || bases.length == 0) {
            return builder.toString();
        }

        for (final String s : bases) {
            builder.append(s).append(SEPARATOR);
        }
        builder.setLength(builder.length() - SEPARATOR.length());
        return builder.toString();
    }

    @Configuration.Destroying
    public void shutdown() {
        HFactory.shutdownCluster(cluster);
    }

    public String getCounterColumnFamily() {
        return builder.getCounterColumnFamily();
    }

    public Keyspace getKeyspace() {
        return keyspace;
    }
}
