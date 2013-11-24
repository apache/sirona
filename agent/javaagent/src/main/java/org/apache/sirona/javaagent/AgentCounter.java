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
import org.apache.sirona.counters.Counter;
import org.apache.sirona.repositories.Repository;
import org.apache.sirona.stopwatches.StopWatch;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

// just a helper to ease ASM work
public class AgentCounter {
    private final StopWatch watch;
    private static final ConcurrentMap<String, Counter.Key> KEYS = new ConcurrentHashMap<String, Counter.Key>();

    public AgentCounter(final StopWatch watch) {
        this.watch = watch;
    }

    // called by agent
    public static AgentCounter start(final String name) {
        final Counter.Key key = findKey(name);
        final Counter monitor = Repository.INSTANCE.getCounter(key);
        return new AgentCounter(Repository.INSTANCE.start(monitor));
    }

    private static Counter.Key findKey(final String name) {
        final Counter.Key found = KEYS.get(name);
        if (found != null) {
            return found;
        }

        final Counter.Key key = new Counter.Key(Role.PERFORMANCES, name);
        KEYS.putIfAbsent(name, key);
        return key;
    }

    // called by agent
    public void stop() {
        watch.stop();
    }
}
