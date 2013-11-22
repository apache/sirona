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

import net.sf.ehcache.CacheException;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Status;
import net.sf.ehcache.event.CacheManagerEventListener;
import net.sf.ehcache.statistics.FlatStatistics;
import org.apache.sirona.gauges.Gauge;
import org.apache.sirona.repositories.Repository;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class EhCacheCachesCountManagerGauge extends EhCacheManagerGaugeBase {
    public EhCacheCachesCountManagerGauge(final CacheManager cacheManager, final Gauge... relatedGauges) {
        super("caches-count", cacheManager);

        final Collection<Gauge> gauges = new ArrayList<Gauge>(1 + (relatedGauges == null ? 0 : relatedGauges.length));
        if (relatedGauges != null) {
            gauges.addAll(Arrays.<Gauge>asList(relatedGauges));
        }
        gauges.add(this);

        cacheManager.getCacheManagerEventListenerRegistry().registerListener(new DynamicCacheGauges(cacheManager, gauges.toArray(new Gauge[gauges.size()])));
    }

    @Override
    public double value() {
        return manager.getCacheNames().length;
    }

    private static class DynamicCacheGauges implements CacheManagerEventListener {
        private final CacheManager manager;
        private final Gauge[] relatedGauges;
        private final Map<String, Collection<Gauge>> children = new ConcurrentHashMap<String, Collection<Gauge>>();

        public DynamicCacheGauges(final CacheManager cacheManager, final Gauge... relatedGauges) {
            this.manager = cacheManager;
            this.relatedGauges = relatedGauges;
        }

        @Override
        public void init() throws CacheException {
            // no-op
        }

        @Override
        public Status getStatus() {
            return manager.getStatus();
        }

        @Override
        public void dispose() throws CacheException {
            for (final String gauge : children.keySet()) {
                notifyCacheRemoved(gauge);
            }
            for (final Gauge gauge : relatedGauges) {
                if (Repository.INSTANCE.gauges().contains(gauge.role())) {
                    Repository.INSTANCE.stopGauge(gauge);
                }
            }
        }

        @Override
        public void notifyCacheAdded(final String cacheName) {
            final Collection<Gauge> gauges = new ArrayList<Gauge>();
            for (final Method m : FlatStatistics.class.getMethods()) {
                final Class<?> returnType = m.getReturnType();
                final String name = m.getName();
                if (m.getParameterTypes().length == 0 &&
                    (returnType.equals(Long.TYPE) || returnType.equals(Double.TYPE))
                    && !name.startsWith("get") && !name.startsWith("local") && !name.startsWith("xa")) {
                    gauges.add(new EhCacheCacheGauge(m, manager.getCache( cacheName)));
                }
            }
            children.put(cacheName, gauges);
        }

        @Override
        public void notifyCacheRemoved(final String cacheName) {
            final Collection<Gauge> gauges = children.remove(cacheName);
            if (gauges != null) {
                for (final Gauge gauge : gauges) {
                    Repository.INSTANCE.stopGauge(gauge);
                }
            }
        }
    }
}
