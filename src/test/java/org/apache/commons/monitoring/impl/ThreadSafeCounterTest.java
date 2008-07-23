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

import org.apache.commons.monitoring.Counter;
import org.apache.commons.monitoring.MonitoringTest;
import org.apache.commons.monitoring.Unit;
import org.apache.commons.monitoring.impl.metrics.ThreadSafeCounter;

public class ThreadSafeCounterTest
    extends TestCase
{

    public void testValue()
        throws Exception
    {
        Counter counter = new ThreadSafeCounter( MonitoringTest.COUNTER );

        counter.set( 1, Unit.UNARY );
        assertEquals( 1, counter.getMin() );
        assertEquals( 1, counter.getMax() );
        assertEquals( 1, counter.get() );

        counter.add( 10, Unit.UNARY );
        assertEquals( 1, counter.getMin() );
        assertEquals( 10, counter.getMax() );
        assertEquals( 11, counter.get() );

        counter.add( -2, Unit.UNARY );
        assertEquals( -2, counter.getMin() );
        assertEquals( 10, counter.getMax() );
        assertEquals( 9, counter.get() );

        assertEquals( 3, counter.getHits() );
        assertEquals( 3.0D, counter.getMean(), 0D );
    }

}
