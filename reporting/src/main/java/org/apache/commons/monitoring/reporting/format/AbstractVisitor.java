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

package org.apache.commons.monitoring.reporting.format;

import org.apache.commons.monitoring.Role;
import org.apache.commons.monitoring.Visitor;
import org.apache.commons.monitoring.counter.Counter;
import org.apache.commons.monitoring.monitors.Monitor;
import org.apache.commons.monitoring.repositories.Repository;

import java.util.Collection;

public abstract class AbstractVisitor implements Visitor {
    protected final RoleFilter filter;

    public AbstractVisitor(final RoleFilter filter) {
        this.filter = filter;
    }

    protected abstract void monitorEnd(String name);
    protected abstract void monitorStart(Monitor monitor);
    protected abstract void counterStart(String name);
    protected abstract void counterEnd(String name);
    protected abstract void repositoryEnd();
    protected abstract void repositoryStart();
    protected abstract void attribute(String name, double value);
    protected abstract void attribute(String name, String value);
    protected abstract void doVisit(final Repository repository);
    protected abstract void doVisit(final Monitor monitor);

    public final void visit(final Repository repository) {
        repositoryStart();
        doVisit(repository);
        repositoryEnd();
    }

    protected Collection<Monitor> getMonitors(final Repository repository) {
        return repository.getMonitors();
    }

    public final void visit(final Monitor monitor) {
        final Monitor.Key key = monitor.getKey();
        final String name = key.getName();
        monitorStart(monitor);
        doVisit(monitor);
        monitorEnd(name);
    }

    protected Collection<Counter> getMetrics(final Monitor monitor) {
        return monitor.getCounters();
    }

    private void attributes(final Counter counter) {
        for (final MetricData md : MetricData.values()) {
            attribute(md.name(), md.value(counter));
        }
    }

    public final void visit(final Counter counter) {
        final Role role = counter.getRole();
        if (filter.accept(role)) {
            final String name = role.getName();
            counterStart(name);
            attributes(counter);
            counterEnd(name);
        }
    }
}