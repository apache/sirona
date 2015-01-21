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

import org.apache.sirona.store.counter.LeafCollectorCounter;
import org.apache.sirona.math.M2AwareStatisticalSummary;

import java.util.Collection;
import java.util.Iterator;

public class Aggregators {
    public static M2AwareStatisticalSummary aggregate(final Collection<LeafCollectorCounter> statistics) {
        if (statistics == null) {
            return null;
        }

        final Iterator<LeafCollectorCounter> iterator = statistics.iterator();
        if (!iterator.hasNext()) {
            return null;
        }

        LeafCollectorCounter current = iterator.next();
        long n = current.getHits();
        double min = current.getMin();
        double sum = current.getSum();
        double max = current.getMax();
        double m2 = current.getSecondMoment();
        double mean = current.getMean();
        while (iterator.hasNext()) {
            current = iterator.next();
            if (current.getMin() < min || Double.isNaN(min)) {
                min = current.getMin();
            }
            if (current.getMax() > max || Double.isNaN(max)) {
                max = current.getMax();
            }
            sum += current.getSum();
            final double oldN = n;
            final double curN = current.getHits();
            n += curN;
            final double meanDiff = current.getMean() - mean;
            mean = sum / n;
            m2 = m2 + current.getSecondMoment() + meanDiff * meanDiff * oldN * curN / n;
        }

        final double variance;
        if (n == 0) {
            variance = Double.NaN;
        } else if (n == 1) {
            variance = 0d;
        } else {
            variance = m2 / (n - 1);
        }
        return new M2AwareStatisticalSummary(mean, variance, n, max, min, sum, m2);
    }

    private Aggregators() {
        // no-op
    }
}
