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

import me.prettyprint.cassandra.model.BasicColumnDefinition;
import me.prettyprint.cassandra.model.ConfigurableConsistencyLevel;
import me.prettyprint.cassandra.serializers.DoubleSerializer;
import me.prettyprint.cassandra.serializers.IntegerSerializer;
import me.prettyprint.cassandra.serializers.LongSerializer;
import me.prettyprint.cassandra.serializers.SerializerTypeInferer;
import me.prettyprint.cassandra.serializers.StringSerializer;
import me.prettyprint.cassandra.service.CassandraHostConfigurator;
import me.prettyprint.cassandra.service.StringKeyIterator;
import me.prettyprint.cassandra.service.ThriftKsDef;
import me.prettyprint.hector.api.Cluster;
import me.prettyprint.hector.api.Keyspace;
import me.prettyprint.hector.api.Serializer;
import me.prettyprint.hector.api.beans.HColumn;
import me.prettyprint.hector.api.ddl.ColumnFamilyDefinition;
import me.prettyprint.hector.api.ddl.ColumnIndexType;
import me.prettyprint.hector.api.ddl.ComparatorType;
import me.prettyprint.hector.api.factory.HFactory;
import org.apache.sirona.cassandra.CassandraBuilder;
import org.apache.sirona.cassandra.DynamicDelegatedSerializer;
import org.apache.sirona.configuration.ioc.Destroying;
import org.apache.sirona.configuration.ioc.IoCs;

import java.util.Collection;
import java.util.HashSet;
import java.util.logging.Logger;

import static java.util.Arrays.asList;

public class CassandraSirona {
    private static final Logger LOGGER = Logger.getLogger(CassandraSirona.class.getName());

    private static final String EMPTY_VALUE = "";
    private static final String SEPARATOR = "->";

    private final CassandraBuilder builder = IoCs.findOrCreateInstance(CassandraBuilder.class);
    private final Cluster cluster;
    private final Keyspace keyspace;

    public CassandraSirona() {
        final CassandraHostConfigurator configurator = new CassandraHostConfigurator(builder.getHosts());
        configurator.setMaxActive(builder.getMaxActive());

        cluster = HFactory.getOrCreateCluster(builder.getCluster(), configurator);

        final String keyspaceName = builder.getKeyspace();

        final ConfigurableConsistencyLevel consistencyLevelPolicy = new ConfigurableConsistencyLevel();
        consistencyLevelPolicy.setDefaultReadConsistencyLevel(builder.getReadConsistencyLevel());
        consistencyLevelPolicy.setDefaultWriteConsistencyLevel(builder.getWriteConsistencyLevel());
        keyspace = HFactory.createKeyspace(keyspaceName, cluster, consistencyLevelPolicy);

        final ColumnFamilyDefinition gauges =
            HFactory.createColumnFamilyDefinition(keyspaceName, builder.getGaugeValuesColumnFamily(), ComparatorType.LONGTYPE);

        final ColumnFamilyDefinition markersGauges =
            HFactory.createColumnFamilyDefinition(keyspaceName, builder.getMarkerGaugesColumFamily(), ComparatorType.UTF8TYPE);

        final ColumnFamilyDefinition counters =
            HFactory.createColumnFamilyDefinition(keyspaceName, builder.getCounterColumnFamily(), ComparatorType.UTF8TYPE);

        final ColumnFamilyDefinition markersCounters =
            HFactory.createColumnFamilyDefinition(keyspaceName, builder.getMarkerCountersColumnFamily(), ComparatorType.UTF8TYPE);

        final ColumnFamilyDefinition statuses =
            HFactory.createColumnFamilyDefinition(keyspaceName, builder.getStatusColumnFamily(), ComparatorType.UTF8TYPE);

        final ColumnFamilyDefinition markersStatuses =
            HFactory.createColumnFamilyDefinition(keyspaceName, builder.getMarkerStatusesColumnFamily(), ComparatorType.UTF8TYPE);

        final ColumnFamilyDefinition pathTracking =
            HFactory.createColumnFamilyDefinition( keyspaceName, builder.getPathTrackingColumFamily(), ComparatorType.UTF8TYPE);
        pathTracking.setDefaultValidationClass( ComparatorType.UTF8TYPE.getClassName() );
        pathTracking.setComparatorType( ComparatorType.UTF8TYPE );

        // creating indexes for cql query

        BasicColumnDefinition levelColumn = new BasicColumnDefinition();
        levelColumn.setName( StringSerializer.get().toByteBuffer("level"));
        levelColumn.setIndexName("level");
        levelColumn.setIndexType( ColumnIndexType.KEYS);
        levelColumn.setValidationClass(ComparatorType.LONGTYPE.getClassName());
        pathTracking.addColumnDefinition( levelColumn  );

        BasicColumnDefinition startTimeColumn = new BasicColumnDefinition();
        startTimeColumn.setName( StringSerializer.get().toByteBuffer("startTime"));
        startTimeColumn.setIndexName("startTime");
        startTimeColumn.setIndexType( ColumnIndexType.KEYS);
        startTimeColumn.setValidationClass(ComparatorType.LONGTYPE.getClassName());
        pathTracking.addColumnDefinition( startTimeColumn  );

        // force the validation class for this column as can fail on non embeded cassandra
        BasicColumnDefinition executionTimeColumn = new BasicColumnDefinition();
        executionTimeColumn.setName( StringSerializer.get().toByteBuffer("executionTime"));
        //startTimeColumn.setIndexName("startTime");
        //startTimeColumn.setIndexType( ColumnIndexType.KEYS);
        executionTimeColumn.setValidationClass(ComparatorType.LONGTYPE.getClassName());
        pathTracking.addColumnDefinition( executionTimeColumn  );


        final ColumnFamilyDefinition markerPathTracking =
            HFactory.createColumnFamilyDefinition( keyspaceName, builder.getMarkerPathTrackingColumFamily(), ComparatorType.UTF8TYPE);

        { // ensure keyspace exists, here if the keyspace doesn't exist we suppose nothing exist
            if (cluster.describeKeyspace(keyspaceName) == null) {
                LOGGER.info("Creating Sirona Cassandra '" + keyspaceName + "' keyspace.");
                cluster.addKeyspace(
                    HFactory.createKeyspaceDefinition(keyspaceName, //
                                                      ThriftKsDef.DEF_STRATEGY_CLASS, //
                                                      builder.getReplicationFactor(), //
                                                      asList(counters, //
                                                             markersCounters, //
                                                             gauges, //
                                                             markersGauges, //
                                                             statuses, //
                                                             markersStatuses, //
                                                             pathTracking, //
                                                             markerPathTracking)));
            }
        }
    }

    public String generateKey(final String... bases) {
        final StringBuilder builder = new StringBuilder();
        if (bases == null || bases.length == 0) {
            return builder.toString();
        }

        for (final String s : bases) {
            if (s != null) {
                builder.append(s).append(SEPARATOR);
            }
        }
        if (builder.length() > 0) {
            builder.setLength(builder.length() - SEPARATOR.length());
        }
        return builder.toString();
    }

    @Destroying
    public void shutdown() {
        HFactory.shutdownCluster(cluster);
    }

    public Keyspace getKeyspace() {
        return keyspace;
    }

    public String getCounterColumnFamily() {
        return builder.getCounterColumnFamily();
    }

    public String getGaugeValuesColumnFamily() {
        return builder.getGaugeValuesColumnFamily();
    }

    public String getStatusColumnFamily() {
        return builder.getStatusColumnFamily();
    }

    public String getMarkerGaugesColumFamily() {
        return builder.getMarkerGaugesColumFamily();
    }

    public String getPathTrackingColumFamily() {
        return builder.getPathTrackingColumFamily();
    }

    public String getMarkerPathTrackingColumFamily() {
        return builder.getMarkerPathTrackingColumFamily();
    }

    public String getMarkerCountersColumnFamily() {
        return builder.getMarkerCountersColumnFamily();
    }

    public String getMarkerStatusesColumnFamily() {
        return builder.getMarkerStatusesColumnFamily();
    }

    public String keySeparator() {
        return SEPARATOR;
    }

    //---------------------------------------------------------
    // utils static methods
    //---------------------------------------------------------

    public static HColumn<String, ?> emptyColumn(final String name) {
        return column(name, EMPTY_VALUE);
    }

    public static <A, B> HColumn<A, B> column(final A name, final B value) {
        return HFactory.createColumn(name, //
                                     value, //
                                     (Serializer<A>) SerializerTypeInferer.getSerializer(name), //
                                     (Serializer<B>) SerializerTypeInferer.getSerializer(value));
    }

    public static Collection<String> keys(final Keyspace keyspace, final String family) {
        final Collection<String> set = new HashSet<String>();
        for (final String item : new StringKeyIterator.Builder(keyspace, family).build()) {
            set.add(item);
        }
        return set;
    }

    public static Number getOrDefault(final DynamicDelegatedSerializer delegatedSerializer, final HColumn<?, ?> col, final Serializer<?> serializer) {
        delegatedSerializer.setDelegate(serializer);

        if (col == null || col.getValue() == null) {
            if ( DoubleSerializer.get() == serializer) {
                return Double.NaN;
            }
            return 0;
        }

        if ( LongSerializer.get() == serializer)
        {
          return Number.class.cast( serializer.fromByteBuffer( col.getValueBytes() ) );
        }

        if ( IntegerSerializer.get() == serializer)
        {
            return Number.class.cast( serializer.fromByteBuffer( col.getValueBytes() ) );
        }

        Object value = col.getValue();
        if (Number.class.isInstance(value)) {
            return Number.class.cast(value);
        }
        throw new IllegalArgumentException("not a number " + value);
    }
}
