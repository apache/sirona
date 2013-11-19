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

import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class OptimizedStatisticsTest {
    @Test
    public void checkConsistency() {
        final SummaryStatistics normal = new SummaryStatistics();
        final OptimizedStatistics optimized = new OptimizedStatistics();

        for (int i = 0; i < 100; i++) {
            final double value = Math.random() * 100;
            normal.addValue(value);
            optimized.addValue(value);
        }

        // Counter only relies on a subset of all stats of SummarryStatistics
        assertEquals(normal.getN(), optimized.getN(), 0.);
        assertEquals(normal.getMax(), optimized.getMax(), 0.);
        assertEquals(normal.getMin(), optimized.getMin(), 0.);
        assertEquals(normal.getSum(), optimized.getSum(), 0.);
        assertEquals(normal.getStandardDeviation(), optimized.getStandardDeviation(), 0.);
        assertEquals(normal.getVariance(), optimized.getVariance(), 0.);
        assertEquals(normal.getMean(), optimized.getMean(), 0.);
        assertEquals(normal.getSecondMoment(), optimized.getSecondMoment(), 0.);
    }
}
