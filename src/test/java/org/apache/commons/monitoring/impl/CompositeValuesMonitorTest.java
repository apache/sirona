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

import org.apache.commons.monitoring.Composite;
import org.apache.commons.monitoring.Counter;
import org.apache.commons.monitoring.Gauge;
import org.apache.commons.monitoring.Monitor;
import org.apache.commons.monitoring.Unit;
import org.apache.commons.monitoring.impl.monitors.CompositeValuesMonitor;

;

/**
 * Test for basic Monitor behaviour
 *
 * @author <a href="mailto:nicolas@apache.org">Nicolas De Loof</a>
 */
public class CompositeValuesMonitorTest
    extends TestCase
{
    public void testReset()
        throws Exception
    {
        Monitor monitor = new CompositeValuesMonitor( new Monitor.Key( "MonitorTest.testReset", "test", "utils" ) );
        Counter counter = monitor.getCounter( Monitor.PERFORMANCES );
        counter.add( 1, Unit.NANOS );
        assertEquals( 1, counter.get() );

        monitor.reset();
        assertEquals( 0, counter.get() );
    }

    @SuppressWarnings("unchecked")
    public void testCompositeCounter()
        throws Exception
    {
        Monitor monitor = new CompositeValuesMonitor( new Monitor.Key( "MonitorTest.testComposite", "test", "utils" ) );
        Counter counter = monitor.getCounter( "COUNTER" );
        Composite<Counter> composite = (Composite<Counter>) counter;

        counter.add( 1, Unit.NANOS );
        assertEquals( 1, counter.get() );

        Counter secondary = composite.createSecondary();
        assertEquals( 0, secondary.get() );

        counter.add( 1, Unit.NANOS );
        assertEquals( 2, counter.get() );
        assertEquals( 1, secondary.get() );

        counter.set( 3, Unit.NANOS );
        assertEquals( 3, counter.get() );
        assertEquals( 3, secondary.get() );

        composite.removeSecondary( secondary );
        counter.add( 1, Unit.NANOS );
        assertEquals( 4, counter.get() );
        assertEquals( 3, secondary.get() );
    }

    @SuppressWarnings("unchecked")
    public void testCompositeGauge()
        throws Exception
    {
        Monitor monitor = new CompositeValuesMonitor( new Monitor.Key( "MonitorTest.testComposite", "test", "utils" ) );
        Gauge gauge = monitor.getGauge( "GAUGE" );
        Composite<Gauge> composite = (Composite<Gauge>) gauge;

        gauge.increment(Unit.NONE);
        assertEquals( 1, gauge.get() );
        Gauge secondary = composite.createSecondary();
        assertEquals( 1, secondary.get() );

        gauge.increment(Unit.NONE);
        assertEquals( 2, gauge.get() );
        assertEquals( 2, secondary.get() );

        gauge.set( 3, Unit.NONE );
        assertEquals( 3, gauge.get() );
        assertEquals( 3, secondary.get() );

        gauge.add( 2, Unit.NONE );
        assertEquals( 5, gauge.get() );
        assertEquals( 5, secondary.get() );

        gauge.increment(Unit.NONE);
        assertEquals( 6, gauge.get() );
        assertEquals( 6, secondary.get() );

        gauge.decrement(Unit.NONE);
        assertEquals( 5, gauge.get() );
        assertEquals( 5, secondary.get() );

        composite.removeSecondary( secondary );
        gauge.decrement(Unit.NONE);
        assertEquals( 4, gauge.get() );
        assertEquals( 5, secondary.get() );
    }
}
