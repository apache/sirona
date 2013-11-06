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
package org.apache.sirona.collector.server;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.sirona.Role;
import org.apache.sirona.configuration.Configuration;
import org.apache.sirona.counters.Counter;
import org.apache.sirona.counters.Unit;
import org.apache.sirona.math.M2AwareStatisticalSummary;
import org.apache.sirona.repositories.Repository;
import org.apache.sirona.status.NodeStatus;
import org.apache.sirona.status.Status;
import org.apache.sirona.status.ValidationResult;
import org.apache.sirona.store.counter.CollectorCounterStore;
import org.apache.sirona.store.gauge.CollectorGaugeDataStore;
import org.apache.sirona.store.status.CollectorNodeStatusDataStore;
import org.apache.sirona.store.status.NodeStatusDataStore;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

// should work with cube clients, see cube module for details
// Note: for this simple need we don't need JAXRS
public class Collector extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(Collector.class.getName());

    private static final String OK = "{}";
    private static final String GAUGE = "gauge";
    private static final String COUNTER = "counter";
    private static final String VALIDATION = "validation";

    private final Map<String, Role> roles = new ConcurrentHashMap<String, Role>();

    private CollectorCounterStore counterDataStore = null;
    private CollectorGaugeDataStore gaugeDataStore = null;
    private CollectorNodeStatusDataStore statusDataStore;
    private ObjectMapper mapper;

    @Override
    public void init() {
        // force init to ensure we have stores
        Configuration.findOrCreateInstance(Repository.class);

        {
            final CollectorGaugeDataStore gds = Configuration.findOrCreateInstance(CollectorGaugeDataStore.class);
            if (gds == null) {
                throw new IllegalStateException("Collector only works with " + CollectorGaugeDataStore.class.getName());
            }
            this.gaugeDataStore = CollectorGaugeDataStore.class.cast(gds);
        }

        {
            final CollectorCounterStore cds = Configuration.findOrCreateInstance(CollectorCounterStore.class);
            if (cds == null) {
                throw new IllegalStateException("Collector only works with " + CollectorCounterStore.class.getName());
            }
            this.counterDataStore = CollectorCounterStore.class.cast(cds);
        }

        {
            final NodeStatusDataStore nds = Configuration.findOrCreateInstance(CollectorNodeStatusDataStore.class);
            if (!CollectorNodeStatusDataStore.class.isInstance(nds)) {
                throw new IllegalStateException("Collector only works with " + CollectorNodeStatusDataStore.class.getName());
            }
            this.statusDataStore = CollectorNodeStatusDataStore.class.cast(nds);
        }

        this.mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false).configure(JsonParser.Feature.ALLOW_NON_NUMERIC_NUMBERS, true);
    }

    @Override
    protected void doPost(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        final Event[] events = mapper.readValue(req.getInputStream(), Event[].class);
        if (events != null && events.length > 0) {
            try {
                if (VALIDATION.equals(events[0].getType())) {
                    final Collection<ValidationResult> results = new ArrayList<ValidationResult>(events.length);
                    for (final Event event : events) {
                        final Map<String, Object> data = event.getData();
                        results.add(new ValidationResult(
                            (String) data.get("name"),
                            Status.valueOf((String) data.get("status")),
                            (String) data.get("message")));
                    }
                    final NodeStatus status = new NodeStatus(results.toArray(new ValidationResult[results.size()]));
                    statusDataStore.store((String) events[0].getData().get("marker"), status);
                } else {
                    for (final Event event : events) {
                        final String type = event.getType();

                        if (COUNTER.equals(type)) {
                            updateCounter(event);
                        } else if (GAUGE.equals(type)) {
                            updateGauge(event);
                        } else {
                            LOGGER.info("Unexpected type '" + type + "', skipping");
                        }
                    }
                }
            } catch (final Exception e) {
                resp.setStatus(HttpURLConnection.HTTP_BAD_REQUEST);
                resp.getWriter().write("{\"error\":\"" + e.getMessage().replace('\"', ' ') + "\"}");
                return;
            }
        }

        resp.setStatus(HttpURLConnection.HTTP_OK);
        resp.getWriter().write(OK);
    }

    private void updateGauge(final Event event) {
        final Map<String,Object> data = event.getData();

        final long time = event.getTime().getTime();
        final double value= Number.class.cast(data.get("value")).doubleValue();

        gaugeDataStore.addToGauge(role(data), time, value, String.class.cast(data.get("marker")));
    }

    private void updateCounter(final Event event) {
        final Map<String,Object> data = event.getData();

        counterDataStore.update(
            new Counter.Key(role(data), String.class.cast(data.get("name"))),
            String.class.cast(data.get("marker")),
            new M2AwareStatisticalSummary(data),
            Number.class.cast(data.get("concurrency")).intValue());
    }

    private Role role(final Map<String, Object> data) {
        final String name = String.class.cast(data.get("role"));
        final Role existing = roles.get(name);
        if (existing != null) {
            return existing;
        }

        final Role created = new Role(name, Unit.get(String.class.cast(data.get("unit"))));
        roles.put(name, created);
        return created;
    }
}
