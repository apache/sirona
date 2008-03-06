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
import org.apache.commons.monitoring.Unit;
import org.apache.commons.monitoring.impl.values.ThreadSafeCounter;

public class ThreadSafeCounterTest
    extends TestCase
{

    public void testValue()
        throws Exception
    {
        Counter counter = new ThreadSafeCounter( "test" );

        counter.set( 1, Unit.NONE );
        assertEquals( 1, counter.getMin() );
        assertEquals( 1, counter.getMax() );
        assertEquals( 1, counter.get() );

        counter.add( 10, Unit.NONE );
        assertEquals( 1, counter.getMin() );
        assertEquals( 10, counter.getMax() );
        assertEquals( 11, counter.get() );

        counter.add( -2, Unit.NONE );
        assertEquals( -2, counter.getMin() );
        assertEquals( 10, counter.getMax() );
        assertEquals( 9, counter.get() );

        assertEquals( 3, counter.getHits() );
        assertEquals( 3.0D, counter.getMean(), 0D );
    }


    public void testUnits()
        throws Exception
    {
        Counter counter = new ThreadSafeCounter( "test" );
        assertNull( counter.getUnit() );
        counter.set( 10, Unit.NANOS );
        assertEquals( Unit.NANOS, counter.getUnit() );
        assertEquals( 10, counter.get() );
        counter.set( 10, Unit.SECOND );
        assertEquals( Unit.NANOS, counter.getUnit() );
        assertEquals( 10000000000L, counter.get() );

        try
        {
            counter.add( 1, Unit.NONE );
            fail( "incompatible unit not detected" );
        }
        catch (IllegalArgumentException e)
        {
            // Expected
        }
    }

}
