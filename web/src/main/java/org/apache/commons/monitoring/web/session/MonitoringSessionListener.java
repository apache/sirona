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
package org.apache.commons.monitoring.web.session;

import org.apache.commons.monitoring.Role;
import org.apache.commons.monitoring.counters.Counter;
import org.apache.commons.monitoring.counters.Unit;
import org.apache.commons.monitoring.repositories.Repository;
import org.apache.commons.monitoring.stopwatches.StopWatch;

import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MonitoringSessionListener implements HttpSessionListener {
    private final Map<String, StopWatch> watches = new ConcurrentHashMap<String, StopWatch>();

    private final Counter counter;

    public MonitoringSessionListener() {
        counter = Repository.INSTANCE.getCounter(new Counter.Key(new Role("session", Unit.UNARY), "session"));
    }

    @Override
    public void sessionCreated(final HttpSessionEvent httpSessionEvent) {
        final StopWatch watch = Repository.INSTANCE.start(counter);
        watches.put(httpSessionEvent.getSession().getId(), watch);
    }

    @Override
    public void sessionDestroyed(final HttpSessionEvent httpSessionEvent) {
        final StopWatch watch = watches.remove(httpSessionEvent.getSession().getId());
        if (watch != null) {
            watch.stop();
        }
    }
}
