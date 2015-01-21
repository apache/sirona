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

/**
 * An enum to acces data from a Counter based on the property name. Can be used to avoid reflection on Counter
 * implementation when requesting data and undesirable exposure of internals.
 * <p/>
 * example :
 * <p/>
 * <pre>
 * String property = httpServletRequest.getParameter( &quot;property&quot; );
 *
 * Double data = MetricData.valueOf( property ).value( metric );
 * </pre>
 *
 *
 */
public enum MetricData {
    Hits {
        @Override
        public double value(final Counter counter) {
            return counter.getHits();
        }

        @Override
        public boolean isTime() {
            return false;
        }
    },
    Max {
        @Override
        public double value(final Counter counter) {
            return counter.getMax();
        }

        @Override
        public boolean isTime() {
            return true;
        }
    },
    Mean {
        @Override
        public double value(final Counter counter) {
            return counter.getMean();
        }

        @Override
        public boolean isTime() {
            return true;
        }
    },
    Min {
        @Override
        public double value(final Counter counter) {
            return counter.getMin();
        }

        @Override
        public boolean isTime() {
            return true;
        }
    },
    StandardDeviation {
        @Override
        public double value(final Counter counter) {
            return counter.getStandardDeviation();
        }

        @Override
        public boolean isTime() {
            return false;
        }
    },
    Sum {
        @Override
        public double value(final Counter counter) {
            return counter.getSum();
        }

        @Override
        public boolean isTime() {
            return true;
        }
    },
    Variance {
        @Override
        public double value(final Counter counter) {
            return counter.getVariance();
        }

        @Override
        public boolean isTime() {
            return false;
        }
    },
    Value {
        @Override
        public double value(final Counter counter) {
            return counter.getSum();
        }

        @Override
        public boolean isTime() {
            return true;
        }
    },
    Concurrency {
        @Override
        public double value(final Counter counter) {
            return counter.currentConcurrency().get();
        }

        @Override
        public boolean isTime() {
            return false;
        }
    },
    MaxConcurrency {
        @Override
        public double value(final Counter counter) {
            return counter.getMaxConcurrency();
        }

        @Override
        public boolean isTime() {
            return false;
        }
    };

    public abstract double value(Counter counter);

    public abstract boolean isTime();
}