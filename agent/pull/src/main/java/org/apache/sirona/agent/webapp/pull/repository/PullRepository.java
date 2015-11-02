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
package org.apache.sirona.agent.webapp.pull.repository;

import org.apache.sirona.configuration.Configuration;
import org.apache.sirona.configuration.ioc.IoCs;
import org.apache.sirona.cube.Cube;
import org.apache.sirona.cube.CubeBuilder;
import org.apache.sirona.cube.MapBuilder;
import org.apache.sirona.gauges.Gauge;
import org.apache.sirona.gauges.GaugeDataStoreAdapter;
import org.apache.sirona.repositories.DefaultRepository;
import org.apache.sirona.repositories.Repository;
import org.apache.sirona.status.NodeStatus;
import org.apache.sirona.status.NodeStatusReporter;
import org.apache.sirona.store.memory.counter.InMemoryCounterDataStore;
import org.apache.sirona.store.memory.tracking.InMemoryPathTrackingDataStore;
import org.apache.sirona.store.status.EmptyStatuses;

import java.util.Collection;
import java.util.Collections;

/**
 * FIXME we do not send path tracking entries here!
 */
public class PullRepository extends DefaultRepository {
    private static final String REGISTRATION_TYPE = "registration";

    private final Cube cube;
    private final boolean clearAfterCollect;

    public PullRepository() {
        super(new InMemoryCounterDataStore(), new GaugeDataStoreAdapter(), new EmptyStatuses(), new InMemoryPathTrackingDataStore(), findAlerters());
        cube = IoCs.findOrCreateInstance(CubeBuilder.class).build();
        clearAfterCollect = Configuration.is(Configuration.CONFIG_PROPERTY_PREFIX + "pull.counter.clearOnCollect", false);
    }

    public Collection<Gauge> getGauges() {
        if (!GaugeDataStoreAdapter.class.isInstance(gaugeDataStore)) {
            return Collections.emptyList();
        }
        return GaugeDataStoreAdapter.class.cast(gaugeDataStore).getGauges();
    }

    public String snapshot() {
        final long time = System.currentTimeMillis();

        final StringBuilder answer = cube.newEventStream();

        // counters
        answer.append(cube.counterSnapshot(Repository.INSTANCE.counters()));

        // gauges
        for (final Gauge g : getGauges()) {
            try {
                cube.gaugeSnapshot(answer, time, g.role(), g.value());
            } catch (final Exception e) {
                // no-op: ignore
            }
        }

        // status
        final NodeStatus status = new NodeStatusReporter().computeStatus();
        answer.append(cube.statusSnapshot(time, status));

        if (clearAfterCollect) {
            clearCounters();
        }

        // remove last ','
        if (answer.length() == 0) {
            return null;
        }

        return cube.globalPayload(answer);
    }

    public void register(final String registrationUrl) {
        if (registrationUrl != null) {
            cube.post(cube.buildEvent(cube.newEventStream(), REGISTRATION_TYPE, 0, new MapBuilder().add("url", registrationUrl).map()));
        }
    }
}
