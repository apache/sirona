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

package org.apache.commons.monitoring.listener;

import static org.apache.commons.monitoring.MonitoringTest.COUNTER;
import static org.apache.commons.monitoring.MonitoringTest.GAUGE;
import junit.framework.TestCase;

import org.apache.commons.monitoring.Repository;
import org.apache.commons.monitoring.Unit;
import org.apache.commons.monitoring.impl.repositories.DefaultRepository;
import org.apache.commons.monitoring.listeners.EmpyMonitor;
import org.apache.commons.monitoring.listeners.SecondaryRepository;

/**
 * @author <a href="mailto:nicolas@apache.org">Nicolas De Loof</a>
 */
public class SecondaryReposioryTest
    extends TestCase
{
    public void testSecondaryRepository()
        throws Exception
    {
        Repository.Observable primary = new DefaultRepository();
        primary.getMonitor( "test" ).getCounter( COUNTER ).add( 10, Unit.UNARY );
        primary.getMonitor( "test" ).getGauge( GAUGE ).set( 5, Unit.UNARY );

        SecondaryRepository secondary = new SecondaryRepository( primary );
        assertNotNull( secondary.getMonitor( "test" ) );
        assertNotNull( secondary.getMonitor( "test" ).getCounter( "COUNTER" ) );
        assertNotNull( secondary.getMonitor( "test" ).getGauge( GAUGE ) );
        assertEquals( 0, secondary.getMonitor( "test" ).getCounter( "COUNTER" ).get() );
        assertEquals( 5, secondary.getMonitor( "test" ).getGauge( GAUGE ).get() );

        primary.getMonitor( "test" ).getCounter( COUNTER ).add( 10, Unit.UNARY );
        primary.getMonitor( "test" ).getGauge( GAUGE ).increment( Unit.UNARY );
        assertEquals( 10, secondary.getMonitor( "test" ).getCounter( COUNTER ).get() );
        assertEquals( 6, secondary.getMonitor( "test" ).getGauge( GAUGE ).get() );

        primary.getMonitor( "new" ).getCounter( COUNTER ).add( 10, Unit.UNARY );
        assertEquals( 10, secondary.getMonitor( "new" ).getCounter( COUNTER ).get() );

        secondary.detach();

        primary.getMonitor( "anotherone" ).getCounter( COUNTER ).add( 10, Unit.UNARY );
        primary.getMonitor( "test" ).getCounter( COUNTER ).add( 10, Unit.UNARY );
        primary.getMonitor( "test" ).getGauge( GAUGE ).increment( Unit.UNARY );
        assertTrue( secondary.getMonitor( "anotherone" ) instanceof EmpyMonitor );
        assertEquals( 30, primary.getMonitor( "test" ).getCounter( COUNTER ).get() );
        assertEquals( 10, secondary.getMonitor( "test" ).getCounter( COUNTER ).get() );
        assertEquals( 7, primary.getMonitor( "test" ).getGauge( GAUGE ).get() );
        assertEquals( 6, secondary.getMonitor( "test" ).getGauge( GAUGE ).get() );
    }
}
