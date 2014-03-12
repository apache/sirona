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
package org.apache.sirona.cassandra.agent.gauge;

import org.apache.sirona.Role;
import org.apache.sirona.cassandra.collector.gauge.CassandraCollectorGaugeDataStore;
import org.apache.sirona.configuration.ioc.AutoSet;
import org.apache.sirona.configuration.ioc.Created;
import org.apache.sirona.store.gauge.BatchGaugeDataStoreAdapter;
import org.apache.sirona.store.gauge.GaugeValuesRequest;
import org.apache.sirona.util.Localhosts;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.SortedMap;
import java.util.logging.Logger;

@AutoSet
public class CassandraGaugeDataStore extends BatchGaugeDataStoreAdapter {
    private static final Logger LOGGER = Logger.getLogger(CassandraGaugeDataStore.class.getName());

    private final CassandraCollectorGaugeDataStore delegate = new CassandraCollectorGaugeDataStore();
    protected String marker;
    protected boolean readFromStore = true;

    @Created
    protected void initMarkerIfNeeded() {
        if (marker == null) {
            marker = Localhosts.get();
        }
        LOGGER.warning("This storage used on app side can be a bit slow, maybe consider using a remote collector");
    }

    @Override
    protected void pushGauges(final Map<Role, Measure> gauges) {
        for (final Map.Entry<Role, Measure> entry : gauges.entrySet()) {
            final Role role = entry.getKey();
            final Measure measure = entry.getValue();

            delegate.createOrNoopGauge(role, marker);
            delegate.addToGauge(role, measure.getTime(), measure.getValue(), marker);
        }
    }

    @Override
    public SortedMap<Long, Double> getGaugeValues(final GaugeValuesRequest gaugeValuesRequest) {
        return delegate.getGaugeValues(gaugeValuesRequest, marker);
    }

    @Override
    public Collection<Role> gauges() {
        final Collection<Role> all = new HashSet<Role>();
        if (readFromStore) {
            all.addAll(delegate.gauges());
        }
        all.addAll(super.gauges()); // override by more recent ones
        return all;
    }

    @Override
    public Role findGaugeRole(final String name) {
        if (name== null || name.length()<1){
            return null;
        }
        for (Role role: delegate.gauges()){
            if (name.equals( role.getName() )){
                return role;
            }
        }
        return null;
    }
}
