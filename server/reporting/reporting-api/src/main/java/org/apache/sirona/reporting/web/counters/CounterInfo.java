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
package org.apache.sirona.reporting.web.counters;

import org.apache.sirona.counters.Counter;

/**
 * @since 0.3
 */
public class CounterInfo
{

    private final KeyInfo keyInfo;

    private final double hits;

    private final double max;

    private final double mean;

    private final double min;

    private final double standardDeviation;

    private final double sum;

    private final double variance;

    private final double concurrency;

    private final double maxConcurrency;

    public CounterInfo( KeyInfo keyInfo, double hits, double max, double mean, double min, double standardDeviation,
                        double sum, double variance, double concurrency, double maxConcurrency )
    {
        this.keyInfo = keyInfo;
        this.hits = hits;
        this.max = max;
        this.mean = mean;
        this.min = min;
        this.standardDeviation = standardDeviation;
        this.sum = sum;
        this.variance = variance;
        this.concurrency = concurrency;
        this.maxConcurrency = maxConcurrency;
    }

    public KeyInfo getKeyInfo()
    {
        return keyInfo;
    }

    public double getHits()
    {
        return hits;
    }

    public double getMax()
    {
        return max;
    }

    public double getMean()
    {
        return mean;
    }

    public double getMin()
    {
        return min;
    }

    public double getStandardDeviation()
    {
        return standardDeviation;
    }

    public double getSum()
    {
        return sum;
    }

    public double getVariance()
    {
        return variance;
    }

    public double getConcurrency()
    {
        return concurrency;
    }

    public double getMaxConcurrency()
    {
        return maxConcurrency;
    }

    @Override
    public String toString()
    {
        return "CounterInfo{" +
            "keyInfo=" + keyInfo +
            ", hits=" + hits +
            ", max=" + max +
            ", mean=" + mean +
            ", min=" + min +
            ", standardDeviation=" + standardDeviation +
            ", sum=" + sum +
            ", variance=" + variance +
            ", concurrency=" + concurrency +
            ", maxConcurrency=" + maxConcurrency +
            '}';
    }
}
