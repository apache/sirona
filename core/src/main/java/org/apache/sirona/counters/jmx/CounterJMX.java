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
package org.apache.sirona.counters.jmx;

import org.apache.sirona.counters.Counter;

public class CounterJMX implements CounterJMXMBean {
    private final Counter delegate;

    public CounterJMX(final Counter counter) {
        this.delegate = counter;
    }

    @Override
    public double getMax() {
        return delegate.getMax();
    }

    @Override
    public double getMin() {
        return delegate.getMin();
    }

    @Override
    public long getHits() {
        return delegate.getHits();
    }

    @Override
    public double getSum() {
        return delegate.getSum();
    }

    @Override
    public double getStandardDeviation() {
        return delegate.getStandardDeviation();
    }

    @Override
    public double getMean() {
        return delegate.getMean();
    }

    @Override
    public String getRole() {
        return delegate.getKey().getRole().getName();
    }

    @Override
    public String getName() {
        return delegate.getKey().getName();
    }
}
