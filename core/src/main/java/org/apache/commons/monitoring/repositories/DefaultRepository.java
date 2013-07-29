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

package org.apache.commons.monitoring.repositories;

import org.apache.commons.monitoring.monitors.DefaultMonitor;
import org.apache.commons.monitoring.monitors.Monitor;
import org.apache.commons.monitoring.monitors.Monitor.Key;
import org.apache.commons.monitoring.stopwatches.CounterStopWatch;
import org.apache.commons.monitoring.stopwatches.StopWatch;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class DefaultRepository implements Repository {
    private final ConcurrentMap<Key, Monitor> monitors = new ConcurrentHashMap<Key, Monitor>(50);

    protected Monitor newMonitorInstance(final Key key) {
        return new DefaultMonitor(key);
    }

    protected Monitor register(final Monitor monitor) {
        return monitors.putIfAbsent(monitor.getKey(), monitor);
    }

    @Override
    public Monitor getMonitor(final Key key) {
        Monitor monitor = monitors.get(key);
        if (monitor == null) {
            monitor = newMonitorInstance(key);
            final Monitor previous = register(monitor);
            if (previous != null) {
                monitor = previous;
            }
        }
        return monitor;
    }

    @Override
    public Monitor getMonitor(final String name) {
        return getMonitor(name, Key.DEFAULT);
    }

    @Override
    public Monitor getMonitor(final String name, final String category) {
        return getMonitor(new Monitor.Key(name, category));
    }

    @Override
    public Collection<Monitor> getMonitors() {
        return Collections.unmodifiableCollection(monitors.values());
    }

    @Override
    public Collection<Monitor> getMonitorsFromCategory(final String category) {
        final Collection<Monitor> filtered = new LinkedList<Monitor>();
        for (final Monitor monitor : monitors.values()) {
            if (category.equals(monitor.getKey().getCategory())) {
                filtered.add(monitor);
            }
        }
        return filtered;
    }

    @Override
    public Set<String> getCategories() {
        final Set<String> categories = new HashSet<String>();
        for (final Key key : monitors.keySet()) {
            categories.add(key.getCategory());
        }
        return categories;
    }

    @Override
    public void clear() {
        monitors.clear();
    }

    @Override
    public void reset() {
        for (final Monitor monitor : monitors.values()) {
            monitor.reset();
        }
    }

    @Override
    public StopWatch start(final Monitor monitor) {
        return new CounterStopWatch(monitor);
    }
}
