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
package org.apache.sirona.repositories;

import org.apache.sirona.Role;
import org.apache.sirona.SironaException;
import org.apache.sirona.alert.AlertListener;
import org.apache.sirona.configuration.Configuration;
import org.apache.sirona.configuration.ioc.IoCs;
import org.apache.sirona.counters.Counter;
import org.apache.sirona.gauges.Gauge;
import org.apache.sirona.gauges.GaugeAware;
import org.apache.sirona.gauges.jvm.ActiveThreadGauge;
import org.apache.sirona.gauges.jvm.CPUGauge;
import org.apache.sirona.gauges.jvm.UsedMemoryGauge;
import org.apache.sirona.gauges.jvm.UsedNonHeapMemoryGauge;
import org.apache.sirona.status.NodeStatus;
import org.apache.sirona.stopwatches.CounterStopWatch;
import org.apache.sirona.stopwatches.StopWatch;
import org.apache.sirona.store.DataStoreFactory;
import org.apache.sirona.store.counter.CollectorCounterStore;
import org.apache.sirona.store.counter.CounterDataStore;
import org.apache.sirona.store.gauge.CollectorGaugeDataStore;
import org.apache.sirona.store.gauge.CommonGaugeDataStore;
import org.apache.sirona.store.gauge.GaugeDataStore;
import org.apache.sirona.store.gauge.GaugeValuesRequest;
import org.apache.sirona.store.status.CollectorNodeStatusDataStore;
import org.apache.sirona.store.status.NodeStatusDataStore;
import org.apache.sirona.store.tracking.PathTrackingDataStore;
import org.apache.sirona.util.ClassLoaders;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.SortedMap;

public class DefaultRepository implements Repository
{
    protected final CounterDataStore counterDataStore;
    protected final NodeStatusDataStore nodeStatusDataStore;
    protected final CommonGaugeDataStore gaugeDataStore;
    protected final PathTrackingDataStore pathTrackingDataStore;

    public DefaultRepository() {
        this(findCounterDataStore(), findGaugeDataStore(), findStatusDataStore(), findPathTrackingDataStore(), findAlerters());
    }

    protected DefaultRepository(final CounterDataStore counter, final CommonGaugeDataStore gauge, //
                                final NodeStatusDataStore status, final PathTrackingDataStore pathTrackingDataStore,
                                final Collection<AlertListener> alertListeners) {
        this.counterDataStore = counter;
        this.gaugeDataStore = gauge;
        this.nodeStatusDataStore = status;
        this.pathTrackingDataStore = pathTrackingDataStore;

        if (CollectorCounterStore.class.isInstance(counter)) {
            IoCs.setSingletonInstance(CollectorCounterStore.class, counter);
        } else {
            IoCs.setSingletonInstance(CounterDataStore.class, counter);
        }
        if (CollectorGaugeDataStore.class.isInstance(gauge)) {
            IoCs.setSingletonInstance(CollectorGaugeDataStore.class, gauge);
        } else {
            IoCs.setSingletonInstance(GaugeDataStore.class, gauge);
        }
        if (CollectorNodeStatusDataStore.class.isInstance(status)) {
            IoCs.setSingletonInstance(CollectorNodeStatusDataStore.class, status);
        } else {
            IoCs.setSingletonInstance(NodeStatusDataStore.class, status);
        }

        if (Configuration.is(Configuration.CONFIG_PROPERTY_PREFIX + "core.gauge.activated", true)) {
            addGauge(new CPUGauge());
            addGauge(new UsedMemoryGauge());
            addGauge(new UsedNonHeapMemoryGauge());
            addGauge(new ActiveThreadGauge());
        }

        for (final AlertListener listener : alertListeners) {
            nodeStatusDataStore.addAlerter(listener);
        }
    }

    protected static Collection<AlertListener> findAlerters() {
        final Collection<AlertListener> listeners = new ArrayList<AlertListener>();
        final String alerters = Configuration.getProperty(Configuration.CONFIG_PROPERTY_PREFIX + "alerters", null);
        if (alerters != null && alerters.trim().length() > 0) {
            for (final String alert : alerters.split(" *, *")) {
                final String classKey = alert + ".class";
                final String type = Configuration.getProperty(classKey, null);
                if (type == null) {
                    throw new IllegalArgumentException("Missing configuration " + classKey);
                }

                try {
                    final Class<?> clazz = ClassLoaders.current().loadClass(type);
                    final AlertListener listener = IoCs.autoSet(alert, AlertListener.class.cast(clazz.newInstance()));
                    listeners.add(listener);
                } catch (final Exception e) {
                    throw new IllegalArgumentException(e);
                }
            }
        }
        return listeners;
    }

    protected static NodeStatusDataStore findStatusDataStore() {
        NodeStatusDataStore status = null;
        try {
            status = IoCs.findOrCreateInstance(NodeStatusDataStore.class);
        } catch (final SironaException e) {
            // no-op
        }
        if (status == null) {
            status = IoCs.findOrCreateInstance(DataStoreFactory.class).getNodeStatusDataStore();
        }
        return status;
    }

    protected static CommonGaugeDataStore findGaugeDataStore() {
        CommonGaugeDataStore gauge = null;
        try {
            gauge = IoCs.findOrCreateInstance(GaugeDataStore.class);
        } catch (final SironaException e) {
            // no-op
        }
        if (gauge == null) {
            try {
                gauge = IoCs.findOrCreateInstance(CollectorGaugeDataStore.class);
            } catch (final SironaException e) {
                // no-op
            }
        }
        if (gauge == null) {
            gauge = IoCs.findOrCreateInstance(DataStoreFactory.class).getGaugeDataStore();
        }
        return gauge;
    }

    protected static PathTrackingDataStore findPathTrackingDataStore() {
        PathTrackingDataStore pathTrackingDataStore = null;
        try {
            pathTrackingDataStore = IoCs.findOrCreateInstance(PathTrackingDataStore.class);
        } catch (final SironaException e) {
            // no-op
        }
        /**
        FIXME define/implement CollectorPathTrackingDataStore
        if (pathTrackingDataStore == null) {
            try {
                pathTrackingDataStore = IoCs.findOrCreateInstance(CollectorPathTrackingDataStore.class);
            } catch (final SironaException e) {
                // no-op
            }
        }
         */
        if (pathTrackingDataStore == null) {
            pathTrackingDataStore = IoCs.findOrCreateInstance(DataStoreFactory.class).getPathTrackingDataStore();
        }
        return pathTrackingDataStore;
    }

    protected static CounterDataStore findCounterDataStore() {
        CounterDataStore counter = null;
        try {
            counter = IoCs.findOrCreateInstance(CounterDataStore.class);
        } catch (final SironaException e) {
            // no-op
        }
        if (counter == null) {
            try {
                counter = IoCs.findOrCreateInstance(CollectorCounterStore.class);
            } catch (final SironaException e) {
                // no-op
            }
        }
        if (counter == null) {
            counter = IoCs.findOrCreateInstance(DataStoreFactory.class).getCounterDataStore();
        }
        return counter;
    }

    @Override
    public Counter getCounter(final Counter.Key key) {
        return counterDataStore.getOrCreateCounter(key);
    }

    @Override
    public Collection<Counter> counters() {
        return counterDataStore.getCounters();
    }

    @Override
    public void clearCounters() {
        counterDataStore.clearCounters();
    }

    @Override
    public void reset() {
        clearCounters();
        nodeStatusDataStore.reset();
        for (final Role g : gauges()) {
            gaugeDataStore.gaugeStopped(g);
        }
    }

    @Override
    public StopWatch start(final Counter monitor) {
        return new CounterStopWatch(monitor);
    }

    @Override
    public SortedMap<Long, Double> getGaugeValues(final long start, final long end, final Role role) {
        return gaugeDataStore.getGaugeValues(new GaugeValuesRequest(start, end, role));
    }

    @Override
    public Collection<Role> gauges() {
        return gaugeDataStore.gauges();
    }

    @Override
    public Role findGaugeRole(final String name) {
        return gaugeDataStore.findGaugeRole(name);
    }

    @Override
    public void addGauge(final Gauge gauge) {
        if (GaugeDataStore.class.isInstance(gaugeDataStore)) {
            GaugeDataStore.class.cast(gaugeDataStore).createOrNoopGauge(gauge.role());
        }
        if (GaugeAware.class.isInstance(gaugeDataStore)) {
            GaugeAware.class.cast(gaugeDataStore).addGauge(gauge);
        }
    }

    @Override
    public void stopGauge(final Gauge gauge) {
        if (GaugeDataStore.class.isInstance(gaugeDataStore)) {
            GaugeDataStore.class.cast(gaugeDataStore).gaugeStopped(gauge.role());
        }
    }

    @Override
    public Map<String, NodeStatus> statuses() {
        return nodeStatusDataStore.statuses();
    }

}
