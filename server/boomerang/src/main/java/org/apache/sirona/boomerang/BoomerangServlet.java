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
package org.apache.sirona.boomerang;

import org.apache.sirona.Role;
import org.apache.sirona.SironaException;
import org.apache.sirona.boomerang.parser.BoomerangData;
import org.apache.sirona.configuration.ioc.IoCs;
import org.apache.sirona.counters.Counter;
import org.apache.sirona.counters.OptimizedStatistics;
import org.apache.sirona.counters.Unit;
import org.apache.sirona.math.M2AwareStatisticalSummary;
import org.apache.sirona.repositories.Repository;
import org.apache.sirona.store.counter.CollectorCounterStore;
import org.apache.sirona.store.counter.CounterDataStore;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.apache.sirona.boomerang.parser.QueryParser.parse;

// all data are in query, this is parsed then added to relative counter
// NOTE: concurrency is not supported
// TODO: see if more metrics would be interesting or if we want generic metric support
public class BoomerangServlet extends HttpServlet {
    private static final String UTF_8 = "UTF-8";

    private static final Unit BYTE_PER_SEC = new Unit("b/s");
    private static final Role BOOMERANG_PERCEIVED = new Role("boomerang_perceived", Unit.Time.MILLISECOND);
    private static final Role BOOMERANG_LATENCY = new Role("boomerang_latency", Unit.Time.MILLISECOND);
    private static final Role BOOMERANG_BANDWITH = new Role("boomerang_bandwidth", BYTE_PER_SEC);

    // marker doesn't make much sense for boomerang
    private static final String BOOMERANG_MARKER = "boomerang";

    private String encoding = UTF_8;
    private CollectorCounterStore collectorCounterStore = null;
    private CounterDataStore counterStore = null;

    @Override
    public void init(final ServletConfig config) throws ServletException {
        encoding = config.getInitParameter("encoding");
        if (encoding == null) {
            encoding = UTF_8;
        }

        // force init to ensure we have stores
        IoCs.findOrCreateInstance(Repository.class);
        try {
            collectorCounterStore = IoCs.findOrCreateInstance(CollectorCounterStore.class);
        } catch (final SironaException se) {
            counterStore = IoCs.findOrCreateInstance(CounterDataStore.class);
        }
    }

    @Override
    protected void service(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        final BoomerangData data = parse(toMap(req.getQueryString()));
        if (data.validate()) { // no url
            // convert to counters
            final String name = new URL(data.getUrl()).getPath(); // skip query
            if (data.getTDone() != null) {
                addToCounter(BOOMERANG_PERCEIVED, name, data.getTDone());
            }
            if (data.getLatency() != null) {
                addToCounter(BOOMERANG_LATENCY, name, data.getLatency());
            }
            if (data.getBandwidth() != null) {
                addToCounter(BOOMERANG_BANDWITH, name, data.getBandwidth());
            }
        }

        // answer
        resp.setStatus(200);
        resp.getWriter().write("");
    }

    private void addToCounter(final Role role, final String name, final long value) throws MalformedURLException {
        final Counter.Key key = new Counter.Key(role, name);
        if (collectorCounterStore != null) { // TODO: better locking? or better window aggregation with bg thread
            final Counter counter = collectorCounterStore.getOrCreateCounter(key, BOOMERANG_MARKER);
            synchronized (counter) { // counter is cached so we can sync it
                final OptimizedStatistics computationalStats = new OptimizedStatistics(
                    counter.getHits(), counter.getSum(), counter.getMin(), counter.getMax(),
                    counter.getMean(), counter.getSecondMoment()
                ).addValue(value);

                final M2AwareStatisticalSummary stats = new M2AwareStatisticalSummary(
                    computationalStats.getMean(), computationalStats.getVariance(), computationalStats.getN(),
                    computationalStats.getMax(), computationalStats.getMin(), computationalStats.getSum(),
                    computationalStats.getSecondMoment()
                );
                collectorCounterStore.update(key, BOOMERANG_MARKER, stats, -1);
            }
        } else {
            final Counter counter = counterStore.getOrCreateCounter(key);
            counterStore.addToCounter(counter, key.getRole().getUnit().convert(value, Unit.Time.MILLISECOND));
        }
    }

    private Map<String, String> toMap(final String query) {
        if (query == null || query.isEmpty()) {
            return Collections.emptyMap();
        }

        final Map<String, String> params = new HashMap<String, String>(15);
        for (final String kv : query.split("&")) {
            final String trimmed = kv.trim();
            if (trimmed.isEmpty()) {
                continue;
            }

            final String[] split = trimmed.split("=");
            if (split.length == 2) {
                try {
                    params.put(split[0], URLDecoder.decode(split[1], encoding));
                } catch (final UnsupportedEncodingException e) {
                    params.put(split[0], split[1]);
                }
            } // else no value
        }
        return params;
    }
}
