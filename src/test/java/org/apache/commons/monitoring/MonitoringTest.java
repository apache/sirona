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

package org.apache.commons.monitoring;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import junit.framework.TestCase;

import org.apache.commons.monitoring.impl.DefaultRepository;

/**
 * @author <a href="mailto:nicolas@apache.org">Nicolas De Loof</a>
 */
public class MonitoringTest
    extends TestCase
{

    public void testStopWatchConcurrencyMonitoring()
        throws Exception
    {
        Monitoring.setRepository( new DefaultRepository() );

        StopWatch stopWatch1 = Monitoring.start( "MonitoringTest.testMonitoring", "test", "utils" );
        StopWatch stopWatch2 = Monitoring.start( "MonitoringTest.testMonitoring", "test", "utils" );
        stopWatch2.stop();

        Monitor monitor = Monitoring.getMonitor( "MonitoringTest.testMonitoring", "test", "utils" );
        Gauge concurrency = monitor.getGauge( Monitor.CONCURRENCY );
        assertEquals( 1, concurrency.get() );
        assertEquals( 2, concurrency.getMax() );

        stopWatch1.stop();
        assertEquals( 0, concurrency.get() );
    }

    public void testThreadSafety()
        throws Exception
    {
        int threads = 50;
        final int loops = 10000;

        Monitoring.setRepository( new DefaultRepository() );

        ExecutorService pool = Executors.newFixedThreadPool( threads );
        for ( int i = 0; i < threads; i++ )
        {
            pool.execute( new Runnable()
            {
                public void run()
                {
                    for ( int j = 0; j < loops; j++ )
                    {
                        Monitor monitor = Monitoring.getMonitor( "MonitoringTest.testMultiThreading", "test", "utils" );
                        monitor.getCounter( "COUNTER" ).add( 1 );
                        monitor.getGauge( "GAUGE" ).increment();
                    }
                    try
                    {
                        Thread.sleep( (long) ( Math.random() * 100 ) );
                    }
                    catch ( InterruptedException e )
                    {
                        // ignore
                    }
                }
            } );
        }
        pool.shutdown();
        pool.awaitTermination( 30, TimeUnit.SECONDS );

        Monitor monitor = Monitoring.getMonitor( "MonitoringTest.testMultiThreading", "test", "utils" );
        assertEquals( threads * loops, monitor.getCounter( "COUNTER" ).getHits() );
        assertEquals( threads * loops, monitor.getCounter( "COUNTER" ).get() );
        assertEquals( threads * loops, monitor.getGauge( "GAUGE" ).get() );
    }

}
