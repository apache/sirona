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

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.statistics.FlatStatistics;
import org.apache.sirona.configuration.Configuration;
import org.apache.sirona.gauges.Gauge;
import org.apache.sirona.gauges.GaugeFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class EhCacheGaugeFactory implements GaugeFactory {
    @Override
    public Gauge[] gauges() {
        if (!Configuration.is(Configuration.CONFIG_PROPERTY_PREFIX + "ehcache.activated", true)) {
            return null;
        }

        try {
            final Field list = CacheManager.class.getDeclaredField("ALL_CACHE_MANAGERS");
            list.setAccessible(true);
            final List<CacheManager> managers = List.class.cast(list.get(null));

            final Collection<Gauge> gauges = new ArrayList<Gauge>(managers.size() * 3);
            for (final CacheManager manager : managers) {
                gauges.addAll(register(manager));
                for (String name:manager.getCacheNames()){
                    Cache cache = manager.getCache( name );
                    if (cache!=null){
                        gauges.addAll(register( cache ));
                    }
                }
            }
            return gauges.toArray(new Gauge[gauges.size()]);
        } catch (final Exception e) {
            return null;
        }
    }

    /**
     * record gauge to run runtime statistics on Cache
     * @param cache
     * @return
     */
    public static Collection<Gauge> register(Cache cache){

        try {
            Collection<Gauge> gauges = new ArrayList<Gauge>( 6 );

            Method method = FlatStatistics.class.getMethod( "cacheHitCount", null );
            gauges.add( new EhCacheCacheGauge(method, cache) );

            method = FlatStatistics.class.getMethod( "cacheMissCount", null );
            gauges.add( new EhCacheCacheGauge(method, cache) );

            method = FlatStatistics.class.getMethod( "cacheHitRatio", null );
            gauges.add( new EhCacheCacheGauge(method, cache) );

            method = FlatStatistics.class.getMethod( "getSize", null );
            gauges.add( new EhCacheCacheGauge(method, cache) );

            method = FlatStatistics.class.getMethod( "getLocalHeapSizeInBytes", null );
            gauges.add( new EhCacheCacheGauge(method, cache) );

            method = FlatStatistics.class.getMethod( "getLocalDiskSizeInBytes", null );
            gauges.add( new EhCacheCacheGauge(method, cache) );

            return gauges;
        } catch ( NoSuchMethodException e ) {

            Logger.getLogger( EhCacheGaugeFactory.class.getName() )
                .log( Level.WARNING, "fail to record ehcache gauge: " + e.getMessage(), e );
            return Collections.emptyList();
        }

    }

    // utility method user can reuse for custom managers
    public static Collection<Gauge> register(final CacheManager manager) {
        final EhCacheTransactionCommittedCountManagerGauge commits = new EhCacheTransactionCommittedCountManagerGauge(manager);
        final EhCacheTransactionRollbackedCountManagerGauge rollbacks = new EhCacheTransactionRollbackedCountManagerGauge(manager);
        return Arrays.<Gauge>asList(
            new EhCacheCachesCountManagerGauge(manager, commits, rollbacks),
            commits, rollbacks
        );
    }
}
