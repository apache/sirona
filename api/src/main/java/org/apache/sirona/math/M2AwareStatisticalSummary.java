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
package org.apache.sirona.math;

import java.io.Serializable;
import java.util.Map;

public class M2AwareStatisticalSummary implements Serializable {
    private final double mean;
    private final double variance;
    private final long n;
    private final double max;
    private final double min;
    private final double sum;
    private final double m2;

    public M2AwareStatisticalSummary(final double mean, final double variance, final long n,
                                     final double max, final double min, final double sum,
                                     final double m2) {
        this.mean = mean;
        this.variance = variance;
        this.n = n;
        this.max = max;
        this.min = min;
        this.sum = sum;
        this.m2 = m2;
    }

    public M2AwareStatisticalSummary(final Map<String, Object> data) {
        this(toDouble(data.get("mean")), toDouble(data.get("variance")), toLong(data.get("hits")),
            toDouble(data.get("max")), toDouble(data.get("min")), toDouble(data.get("sum")),
            toDouble(data.get("m2")));
    }

    private static double toDouble(final Object mean) {
        if (Number.class.isInstance(mean)) {
            return Number.class.cast(mean).doubleValue();
        }
        if (String.class.isInstance(mean)) {
            return Double.parseDouble(String.class.cast(mean));
        }
        if (mean == null) {
            return Double.NaN;
        }
        throw new IllegalArgumentException(mean + " not supported");
    }

    private static long toLong(final Object mean) {
        if (Number.class.isInstance(mean)) {
            return Number.class.cast(mean).longValue();
        }
        if (String.class.isInstance(mean)) {
            return Long.parseLong(String.class.cast(mean));
        }
        throw new IllegalArgumentException(mean + " not supported");
    }

    public double getSecondMoment() {
        return m2;
    }

    public double getMean() {
        return mean;
    }

    public double getVariance() {
        return variance;
    }

    public long getN() {
        return n;
    }

    public double getMax() {
        return max;
    }

    public double getMin() {
        return min;
    }

    public double getSum() {
        return sum;
    }
}
