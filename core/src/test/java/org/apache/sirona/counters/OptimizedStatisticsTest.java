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
        final SummaryStatistics normal = new SummaryStatistics(); // considered as reference
        final OptimizedStatistics optimized = new OptimizedStatistics();

        for (int i = 0; i < 100; i++) {
            final double value = Math.random() * 100;
            normal.addValue(value);
            optimized.addValue(value);

            doAssert(normal, optimized); // important to assert for 0, 1 and n > 1
        }
    }

    private static void doAssert(final SummaryStatistics normal, OptimizedStatistics optimized) {
        double delta = Math.pow(10, -10);
        assertEquals(normal.getN(), optimized.getN(), delta);
        assertEquals(normal.getMax(), optimized.getMax(), delta);
        assertEquals(normal.getMin(), optimized.getMin(), delta);
        assertEquals(normal.getSum(), optimized.getSum(), delta);
        assertEquals(normal.getMean(), optimized.getMean(), delta);
        assertEquals(normal.getSecondMoment(), optimized.getSecondMoment(), delta);
        assertEquals(normal.getVariance(), optimized.getVariance(), delta);
        assertEquals(normal.getStandardDeviation(), optimized.getStandardDeviation(), delta);
    }
}
