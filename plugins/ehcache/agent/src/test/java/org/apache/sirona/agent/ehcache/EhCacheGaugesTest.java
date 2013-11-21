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
package org.apache.sirona.agent.ehcache;

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sf.ehcache.config.Configuration;
import org.apache.sirona.gauges.Gauge;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class EhCacheGaugesTest {
    private CacheManager mgr;

    @Before
    public void createManager() {
        mgr = new CacheManager();
    }

    @After
    public void shutdownManager() {
        mgr.shutdown();
    }

    @Test
    public void register() {
        assertEquals(3, EhCacheGaugeFactory.register(mgr).size());
    }

    @Test
    public void factory() {
        assertEquals(3, new EhCacheGaugeFactory().gauges().length);
        new CacheManager(new Configuration().name("other"));
        assertEquals(6, new EhCacheGaugeFactory().gauges().length);
    }

    @Test
    public void ehCacheCachesCountManagerGauge() {
        final Gauge gauge = new EhCacheCachesCountManagerGauge(mgr);
        assertEquals(0., gauge.value(), 0.);
        mgr.addCacheIfAbsent("cache1");
        assertEquals(1., gauge.value(), 0.);
        mgr.addCacheIfAbsent("cache2");
        assertEquals(2., gauge.value(), 0.);
        mgr.removeCache("cache1");
        assertEquals(1., gauge.value(), 0.);
    }

    @Test
    public void cacheGauges() throws Exception {
        new EhCacheCachesCountManagerGauge(mgr);

        final Set<?> registeredListeners = mgr.getCacheManagerEventListenerRegistry().getRegisteredListeners();
        assertEquals(1, registeredListeners.size());
        final Object listener = registeredListeners.iterator().next();
        final Map<String, Collection<Gauge>> gauges = getField(listener, "children");
        assertTrue(gauges.isEmpty());

        mgr.addCache("cache1");
        assertEquals(1, gauges.size());
        for (final Gauge g : gauges.values().iterator().next()) {
            if (g.role().getName().equals("ehcache-__DEFAULT__-cacheHitCount")) {
                assertEquals(0., g.value(), 0.);
                mgr.getCache("cache1").put(new Element("k", "v", 0));
                mgr.getCache("cache1").get("k");
                mgr.getCache("cache1").get("k");
                assertEquals(2., g.value(), 0.);
                return;
            }
        }
        fail("gauge not found");
    }

    @Test
    public void cacheGaugesUnregister() throws Exception {
        new EhCacheCachesCountManagerGauge(mgr);

        final Map<String, Collection<Gauge>> gauges = getField(mgr.getCacheManagerEventListenerRegistry().getRegisteredListeners().iterator().next(), "children");

        mgr.addCache("cache1");
        assertEquals(1, gauges.size());
        mgr.removeCache("cache1");
        assertEquals(0, gauges.size());
    }

    @Test
    public void ehCacheTransactionCommittedCountManagerGauge() {
        final Gauge gauge = new EhCacheTransactionCommittedCountManagerGauge(mgr);
        assertEquals(0., gauge.value(), 0.);
        mgr.getTransactionController().begin();
        mgr.getTransactionController().commit();
        assertEquals(1., gauge.value(), 0.);
    }

    @Test
    public void ehCacheTransactionRollbackedCountManagerGauge() {
        final Gauge gauge = new EhCacheTransactionRollbackedCountManagerGauge(mgr);
        assertEquals(0., gauge.value(), 0.);
        mgr.getTransactionController().begin();
        mgr.getTransactionController().rollback();
        assertEquals(1., gauge.value(), 0.);
    }

    private static Map<String, Collection<Gauge>> getField(final Object listener, final String children) throws Exception {
        final Field field = listener.getClass().getDeclaredField(children);
        field.setAccessible(true);
        return (Map<String, Collection<Gauge>>) field.get(listener);
    }
}
