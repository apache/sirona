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

public class OptimizedStatistics {
    protected long n = 0;
    protected double sum = 0;
    protected double min = Double.NaN;
    protected double max = Double.NaN;

    // first moment (mean)
    protected double m1 = Double.NaN;

    // second moment
    protected double m2 = Double.NaN;

    public OptimizedStatistics() {
        // no-op
    }

    public OptimizedStatistics(final long n, final double sum, final double min,
                               final double max, final double m1, final double m2) {
        this.n = n;
        this.sum = sum;
        this.min = min;
        this.max = max;
        this.m1 = m1;
        this.m2 = m2;
    }

    public OptimizedStatistics addValue(double value) {
        if (n == 0) {
            m1 = 0.0;
            m2 = 0.0;
        }

        n++;
        sum += value;

        // min
        if (value < min || Double.isNaN(min)) {
            min = value;
        }

        // max
        if (value > max || Double.isNaN(max)) {
            max = value;
        }

        // first moment
        final double dev = value - m1;
        final double nDev = dev / n;
        m1 += nDev;

        // second moment
        m2 += dev * nDev * (n - 1);

        return this;
    }

    public void clear() {
        n = 0;
        sum = 0;
        min = Double.NaN;
        max = Double.NaN;
        m1 = Double.NaN;
        m2 = Double.NaN;
    }

    public double getMean() {
        return m1;
    }

    public double getVariance() {
        if (n == 0) {
            return Double.NaN;
        } else if (n == 1) {
            return 0;
        }
        return m2 / (n - 1);
    }

    public double getStandardDeviation() {
        if (n > 1) {
            return Math.sqrt(getVariance());
        } else if (n == 1) {
            return 0.;
        }
        return Double.NaN;
    }

    public double getMax() {
        return max;
    }

    public double getMin() {
        return min;
    }

    public long getN() {
        return n;
    }

    public double getSum() {
        return sum;
    }

    public double getSecondMoment() {
        return m2;
    }

    public OptimizedStatistics copy() {
        return new OptimizedStatistics(n, sum, min, max, m1, m2);
    }

    @Override
    public String toString() {
        return "OptimizedStatistics{" +
                "n=" + n +
                ", sum=" + sum +
                ", min=" + min +
                ", max=" + max +
                ", m1=" + m1 +
                ", m2=" + m2 +
                '}';
    }
}
