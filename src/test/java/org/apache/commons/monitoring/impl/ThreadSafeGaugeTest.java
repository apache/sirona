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

package org.apache.commons.monitoring.impl;

import junit.framework.TestCase;

import org.apache.commons.monitoring.Gauge;
import org.apache.commons.monitoring.MonitoringTest;
import org.apache.commons.monitoring.Role;
import org.apache.commons.monitoring.Unit;
import org.apache.commons.monitoring.impl.values.ThreadSafeGauge;

public class ThreadSafeGaugeTest
    extends TestCase
{
    private long time;

    public void testValue()
        throws Exception
    {
        Gauge gauge = new ThreadSafeGauge( MonitoringTest.GAUGE );

        gauge.set( 1, Unit.UNARY );
        assertEquals( 1, gauge.getMin() );
        assertEquals( 1, gauge.getMax() );
        assertEquals( 1, gauge.get() );

        gauge.increment(Unit.UNARY);
        assertEquals( 1, gauge.getMin() );
        assertEquals( 2, gauge.getMax() );
        assertEquals( 2, gauge.get() );

        gauge.decrement(Unit.UNARY);
        gauge.decrement(Unit.UNARY);
        assertEquals( 0, gauge.getMin() );
        assertEquals( 2, gauge.getMax() );
        assertEquals( 0, gauge.get() );
    }

    /**
     * Use a fake time to emulate to concurrent threads
     * <ul>
     * <li>First one runs during 4 time units </li>
     * <li>First one runs during 2 time units </li>
     * </ul>
     * <pre>
     *  concurrency
     *          _______
     *  2   ___|       |___
     *  1  |               |
     *    -0---1---2---3---4---&gt; time
     * </pre>
     * mean value is expected to be 1.5
     *
     * @throws Exception
     */
    public void testCompteMean()
        throws Exception
    {
        Gauge gauge = new MockTimeGauge( MonitoringTest.GAUGE );

        time = 0;
        gauge.increment(Unit.UNARY);
        time++;
        gauge.increment(Unit.UNARY);
        time++;
        time++;
        gauge.decrement(Unit.UNARY);
        time++;
        gauge.decrement(Unit.UNARY);

        assertEquals( 0, gauge.get() );
        assertEquals( 1.5D, gauge.getMean(), 0D );
    }

    /**
     * A custom ThreadSafeGauge that use a mock nanoTime implementation to make
     * timing predictable during unit test execution.
     *
     * @author <a href="mailto:nicolas@apache.org">Nicolas De Loof</a>
     */
    private class MockTimeGauge
        extends ThreadSafeGauge
    {

        public MockTimeGauge( Role<Gauge> role )
        {
            super( role );
        }

        @Override
        protected long nanotime()
        {
            return time;
        }
    }
}
