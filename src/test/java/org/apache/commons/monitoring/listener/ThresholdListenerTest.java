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

import junit.framework.TestCase;

import org.apache.commons.monitoring.Counter;
import org.apache.commons.monitoring.Monitor;
import org.apache.commons.monitoring.StatValue;
import org.apache.commons.monitoring.Unit;
import org.apache.commons.monitoring.impl.monitors.CreateValuesOnDemandMonitor;
import org.apache.commons.monitoring.listeners.ThresholdListener;

/**
 * @author <a href="mailto:nicolas@apache.org">Nicolas De Loof</a>
 */
public class ThresholdListenerTest
    extends TestCase
{
    public void testThresholdListener()
        throws Exception
    {
        final Monitor monitor = new CreateValuesOnDemandMonitor(
            new Monitor.Key( "MonitoringTest.testMonitoring", "test", "utils" ) );
        Counter counter = monitor.getCounter( Monitor.PERFORMANCES );

        TestListener listener = new TestListener( monitor );
        counter.addListener( listener );

        counter.add( 1, Unit.NONE );
        assertEquals( "unexpected listener notification", 0, listener.count );
        counter.add( 10, Unit.NONE );
        assertEquals( "listener didn't get notified", 1, listener.count );

        counter.removeListener( listener );
        counter.add( 10, Unit.NONE );
        assertEquals( "removed listener was notified", 1, listener.count );
    }

    private final class TestListener
        extends ThresholdListener
    {
        private final Monitor monitor;

        long count = 0;

        private TestListener( Monitor monitor )
        {
            this.monitor = monitor;
        }

        public void exceed( StatValue value, long l )
        {
            count++;
            assertEquals( 10, l );
            assertEquals( Monitor.PERFORMANCES, value.getRole() );
            assertEquals( monitor, value.getMonitor() );
        }

        public long getThreshold()
        {
            return 5;
        }
    }
}
