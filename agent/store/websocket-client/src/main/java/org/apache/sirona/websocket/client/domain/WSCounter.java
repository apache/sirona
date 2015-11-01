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
package org.apache.sirona.websocket.client.domain;

import org.apache.sirona.counters.Counter;

// just to define the payload API explicitly
// and not rely on our Counter which can evolve
public class WSCounter extends WSDomain {
    private String name;
    private String roleName;
    private String roleUnit;
    private double secondMoment;
    private long hits;
    private int concurrency;
    private double variance;
    private double sum;
    private double min;
    private double max;
    private double mean;

    public WSCounter() {
        // no-op
    }

    public WSCounter(final Counter counter, final String marker) {
        super("counter", marker);
        name = counter.getKey().getName();
        roleName = counter.getKey().getRole().getName();
        roleUnit = counter.getKey().getRole().getUnit().getName();
        secondMoment = counter.getSecondMoment();
        hits = counter.getHits();
        variance = counter.getVariance();
        sum = counter.getSum();
        min = counter.getMin();
        max = counter.getMax();
        mean = counter.getMean();
        concurrency = counter.currentConcurrency().get();
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(final String roleName) {
        this.roleName = roleName;
    }

    public String getRoleUnit() {
        return roleUnit;
    }

    public void setRoleUnit(final String roleUnit) {
        this.roleUnit = roleUnit;
    }

    public int getConcurrency() {
        return concurrency;
    }

    public void setConcurrency(final int concurrency) {
        this.concurrency = concurrency;
    }

    public double getMean() {
        return mean;
    }

    public void setMean(final double mean) {
        this.mean = mean;
    }

    public double getSecondMoment() {
        return secondMoment;
    }

    public void setSecondMoment(final double secondMoment) {
        this.secondMoment = secondMoment;
    }

    public long getHits() {
        return hits;
    }

    public void setHits(final long hits) {
        this.hits = hits;
    }

    public double getVariance() {
        return variance;
    }

    public void setVariance(final double variance) {
        this.variance = variance;
    }

    public double getSum() {
        return sum;
    }

    public void setSum(final double sum) {
        this.sum = sum;
    }

    public double getMin() {
        return min;
    }

    public void setMin(final double min) {
        this.min = min;
    }

    public double getMax() {
        return max;
    }

    public void setMax(final double max) {
        this.max = max;
    }
}
