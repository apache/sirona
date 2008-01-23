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

public class ConcurrencyGaugeTest
    extends TestCase
{
    private long time;

    /**
     * Use a fake time to emulate to concurrent threads
     * <ul>
     * <li>First one runs during 4 time units </li>
     * <li>First one runs during 2 time units </li>
     * </ul>
     * average concurrency is expected to be 1.5
     * 
     * <pre>
     *  concurrency
     *  3          _______
     *  2      ___|       |___
     *  1     |               |
     *  0 +---1---2---3---4---5---&gt; time
     * 
     * 
     * 
     * </pre>
     * 
     * @throws Exception
     */
    public void testCompteAverageConcurency()
        throws Exception
    {
        Gauge concurrency = new MockTimeConcurrencyGauge();

        time = 1;
        concurrency.increment();
        time++;
        concurrency.increment();
        time++;
        time++;
        concurrency.decrement();
        time++;
        concurrency.decrement();

        assertEquals( 0, concurrency.get() );
        assertEquals( 1.5D, concurrency.average(), 0D );
    }

    private class MockTimeConcurrencyGauge
        extends ConcurrencyGauge
    {
        @Override
        protected long nanoTime()
        {
            return time;
        }
    }
}
