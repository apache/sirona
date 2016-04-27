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
package org.apache.sirona.store.memory.counter;

import org.apache.sirona.Role;
import org.apache.sirona.counters.Counter;
import org.apache.sirona.counters.LockableCounter;
import org.apache.sirona.counters.OptimizedStatistics;
import org.apache.sirona.counters.Unit;
import org.apache.sirona.store.counter.CounterDataStore;
import org.junit.Test;

import java.util.Collection;

import static org.junit.Assert.assertEquals;

public class ExponentialDecayCounterTest {
    @Test
    public void run() {
        final ExponentialDecayCounter counter = new ExponentialDecayCounter(new Counter.Key(new Role("doctor", Unit.KILO), "weight"), new CounterDataStore() {
            public Counter getOrCreateCounter(final Counter.Key key) {
                throw new UnsupportedOperationException();
            }

            public void clearCounters() {
                throw new UnsupportedOperationException();
            }

            public Collection<Counter> getCounters() {
                throw new UnsupportedOperationException();
            }

            public void addToCounter(final Counter defaultCounter, final double delta) {
                LockableCounter.class.cast(defaultCounter).addInternal(delta);
            }
        }, ExponentialDecayCounter.ACCEPTABLE_DEFAULT_ALPHA, 3, 60);

        counter.add(80.0, Unit.KILO);
        counter.add(75.0, Unit.KILO);
        counter.add(90.0, Unit.KILO);

        final OptimizedStatistics accurate = new OptimizedStatistics();
        accurate.addValue(80);
        accurate.addValue(75);
        accurate.addValue(90);

        {
            final OptimizedStatistics statistics = counter.getStatistics();
            assertEquals(3, statistics.getN());

            // ensure counter and stats reflects the same state
            assertEquals(counter.getHits(), statistics.getN());
            assertEquals(counter.getSum(), statistics.getSum(), 0.);
            assertEquals(counter.getMean(), statistics.getMean(), 0.);
            assertEquals(counter.getVariance(), statistics.getVariance(), 0.);
            assertEquals(counter.getMin(), statistics.getMin(), 0.);
            assertEquals(counter.getMax(), statistics.getMax(), 0.);

            // check values are accurate
            assertEquals(accurate.getSum(), statistics.getSum(), 0.);
            assertEquals(accurate.getMin(), statistics.getMin(), 0.);
            assertEquals(accurate.getMax(), statistics.getMax(), 0.);
            assertEquals(accurate.getMean(), statistics.getMean(), 10);
            assertEquals(accurate.getStandardDeviation(), statistics.getStandardDeviation(), 3);
            // assertEquals(accurate.getVariance(), statistics.getVariance(), 15); // not perfect yet
        }
    }
}

