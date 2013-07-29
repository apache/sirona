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
package org.apache.commons.monitoring.counter;

import org.apache.commons.math.stat.descriptive.SummaryStatistics;
import org.apache.commons.monitoring.Role;
import org.apache.commons.monitoring.configuration.Configuration;
import org.apache.commons.monitoring.counter.queuemanager.MetricQueueManager;
import org.apache.commons.monitoring.monitors.Monitor;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class DefaultCounter implements Counter {
    private static final MetricQueueManager QUEUE_MANAGER = Configuration.newInstance(MetricQueueManager.class);

    protected Monitor monitor;
    protected SummaryStatistics statistics;
    protected Role role;
    protected Unit unit;
    protected Lock lock = new ReentrantLock();

    public DefaultCounter(final Role role) {
        this.role = role;
        this.unit = role.getUnit();
        this.statistics = new SummaryStatistics();
    }

    public void addInternal(final double delta) { // should be called from a thread safe environment
        statistics.addValue(delta);
    }

    @Override
    public void reset() {
        statistics.clear();
    }

    @Override
    public void add(final double delta) { // sensitive method which need to be thread safe, default implementation relies on disruptor
        QUEUE_MANAGER.add(this, delta);
    }

    @Override
    public void add(final double delta, final Unit deltaUnit) {
        add(unit.convert(delta, deltaUnit));
    }

    @Override
    public void setMonitor(final Monitor monitor) {
        this.monitor = monitor;
    }

    @Override
    public Monitor getMonitor() {
        return monitor;
    }

    @Override
    public Role getRole() {
        return role;
    }

    @Override
    public Unit getUnit() {
        return unit;
    }

    @Override
    public double getMax() {
        return statistics.getMax();
    }

    @Override
    public double getMin() {
        return statistics.getMin();
    }

    @Override
    public double getSum() {
        return statistics.getSum();
    }

    @Override
    public double getStandardDeviation() {
        return statistics.getStandardDeviation();
    }

    @Override
    public double getVariance() {
        return statistics.getVariance();
    }

    @Override
    public double getMean() {
        return statistics.getMean();
    }

    @Override
    public double getGeometricMean() {
        return statistics.getGeometricMean();
    }

    @Override
    public double getSumOfLogs() {
        return statistics.getSumOfLogs();
    }

    @Override
    public double getSumOfSquares() {
        return statistics.getSumOfLogs();
    }

    @Override
    public long getHits() {
        return statistics.getN();
    }

    public Lock getLock() {
        return lock;
    }
}
