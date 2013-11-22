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
import org.apache.sirona.SironaException;

import java.lang.reflect.Method;

public class EhCacheCacheGauge extends EhCacheManagerGaugeBase {
    private final Cache cache;
    private final Method method;

    public EhCacheCacheGauge(final Method method, final Cache cache) {
        super(method.getName(), cache.getCacheManager());

        this.cache = cache;
        this.method = method;
    }

    @Override
    public double value() {
        try {
            return Number.class.cast(method.invoke(cache.getStatistics())).doubleValue();
        } catch (final Exception e) {
            throw new SironaException(e);
        }
    }

    @Override
    public String toString() {
        return "EhCacheCacheGauge{" +
            "cache='" + cache + '\'' +
            ", method=" + method.getName() +
            '}';
    }
}
