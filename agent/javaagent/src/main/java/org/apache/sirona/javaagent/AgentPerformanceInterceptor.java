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
package org.apache.sirona.javaagent;

import org.apache.sirona.Role;
import org.apache.sirona.aop.AbstractPerformanceInterceptor;
import org.apache.sirona.counters.Counter;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

// just a helper to ease ASM work and reuse AbstractPerformanceInterceptor logic
public class AgentPerformanceInterceptor extends AbstractPerformanceInterceptor<String> {
    private static final ConcurrentMap<String, Counter.Key> KEYS = new ConcurrentHashMap<String, Counter.Key>();

    public static void initKey(final String name) {
        KEYS.putIfAbsent(name, new Counter.Key(Role.PERFORMANCES, name));
    }

    // called by agent
    public static Context start(final String name) {
        return new AgentPerformanceInterceptor().before(name, name);
    }

    @Override
    protected Counter.Key getKey(final String name) {
        return KEYS.get(name);
    }

    @Override
    protected Object proceed(final String invocation) throws Throwable {
        throw new UnsupportedOperationException("shouldn't be called directly");
    }

    @Override
    protected String getCounterName(final String invocation) {
        return invocation;
    }
}
