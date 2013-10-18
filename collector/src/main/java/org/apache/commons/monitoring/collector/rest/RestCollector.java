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
package org.apache.commons.monitoring.collector.rest;

import org.apache.commons.monitoring.Role;
import org.apache.commons.monitoring.collector.rest.store.CollectorCounter;
import org.apache.commons.monitoring.collector.rest.store.CollectorCounterStore;
import org.apache.commons.monitoring.configuration.Configuration;
import org.apache.commons.monitoring.counters.Counter;
import org.apache.commons.monitoring.counters.Unit;
import org.apache.commons.monitoring.store.CounterDataStore;
import org.apache.commons.monitoring.store.GaugeDataStore;
import org.apache.commons.monitoring.store.InMemoryGaugeDataStore;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Map;

// close to cube collector API but backed by sirona DataStore
@Consumes(MediaType.APPLICATION_JSON)
@Path("/event")
public class RestCollector {
    private static final String OK = "{}";
    private static final String GAUGE = "gauge";
    private static final String COUNTER = "counter";

    private final CounterDataStore counterDataStore;
    private final GaugeDataStore gaugeDataStore;

    public RestCollector() {
        gaugeDataStore = Configuration.findOrCreateInstance(GaugeDataStore.class);
        if (!InMemoryGaugeDataStore.class.isInstance(gaugeDataStore)) {
            throw new IllegalStateException("Collector only works with " + InMemoryGaugeDataStore.class.getName());
        }

        counterDataStore = Configuration.findOrCreateInstance(CounterDataStore.class);
        if (!CollectorCounterStore.class.isInstance(counterDataStore)) {
            throw new IllegalStateException("Collector only works with " + CollectorCounterStore.class.getName());
        }
    }

    @POST
    @Path("put")
    public Response put(final Event[] events) {
        if (events != null && events.length > 0) {
            try {
                doPut(events);
            } catch (final Exception e) {
                return error(e);
            }
        }
        return ok();
    }

    private void doPut(final Event[] events) {
        for (final Event event : events) {
            if (COUNTER.equals(event.getType())) {
                updateCounter(event);
            } else if (GAUGE.equals(event.getType())) {
                updateGauge(event);
            }
        }
    }

    private void updateGauge(final Event event) {
        final Map<String,Object> data = event.getData();

        final long time = event.getTime().getTime();

        final String role = String.class.cast(data.get("role"));
        final String unit = String.class.cast(data.get("unit"));
        final double value= Number.class.cast(data.get("value")).doubleValue();

        final Role roleInstance = new Role(role, Unit.get(unit));
        gaugeDataStore.createOrNoopGauge(roleInstance);
        InMemoryGaugeDataStore.class.cast(gaugeDataStore).addToGauge(roleInstance, time, value);
    }

    private void updateCounter(final Event event) {
        final Map<String,Object> data = event.getData();

        final long time = event.getTime().getTime();

        final long hits = Number.class.cast(data.get("hits")).longValue();
        final long sum = Number.class.cast(data.get("sum")).longValue();
        final int concurrency = Number.class.cast(data.get("concurrency")).intValue();

        final String role = String.class.cast(data.get("role"));
        final String unit = String.class.cast(data.get("unit"));
        final String name = String.class.cast(data.get("name"));

        final Counter counter = counterDataStore.getOrCreateCounter(new Counter.Key(new Role(role, Unit.get(unit)), name));
        CollectorCounter.class.cast(counter).addEvent(time, hits, sum, concurrency); // we checked the store in the constructor so that's ok
    }

    private Response ok() {
        return Response.ok(OK).build();
    }

    private Response error(final Exception e) {
        return Response.status(400).entity("{\"error\":\"" + e.getMessage() + "\"}").build();
    }
}
