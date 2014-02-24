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
package org.apache.sirona.counters;

import org.apache.sirona.Role;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A <code>Metric</code> is a numerical indicator of some monitored application state with support for simple
 * statistics.
 *
 *
 */
public interface Counter {
    Key getKey();

    void reset();

    void add(double delta);

    void add(double delta, Unit unit);

    AtomicInteger currentConcurrency();

    void updateConcurrency(int concurrency);

    int getMaxConcurrency();

    // --- Statistical indicators --------------------------------------------

    double getMax();

    double getMin();

    long getHits();

    double getSum();

    double getStandardDeviation();

    double getVariance();

    double getMean();

    double getSecondMoment(); // here for aggregation etc but not (yet?) a human metric so not in MetricData

    public static class Key implements Serializable {
        private final String name;
        private final Role role;
        private int hash = Integer.MIN_VALUE;

        public Key(final Role role, final String name) {
            this.role = role;
            this.name = name;
        }

        @Override
        public String toString() {
            return "name=" + name;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            final Key key = (Key) o;
            return name.equals(key.name) && role.equals(key.role);

        }

        @Override
        public int hashCode() {
            if (hash == Integer.MIN_VALUE) {
                hash = name.hashCode();
                hash = 31 * hash + role.hashCode();
            }
            return hash;
        }

        public String getName() {
            return name;
        }

        public Role getRole() {
            return role;
        }
    }
}
