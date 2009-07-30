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

package org.apache.commons.monitoring.stopwatches;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

import org.apache.commons.monitoring.StopWatch;
import org.apache.commons.monitoring.monitors.NullMonitor;
import org.junit.Test;

/**
 * @author <a href="mailto:nicolas@apache.org">Nicolas De Loof</a>
 */
public class DefaultStopWatchTest
{
    private long time;

    /**
     * assert the StopWatch computes the time elapsed during the monitored
     * process execution. User a MockTimeWatch to make timing predictable
     *
     * @throws Exception
     */
    @Test
    public void computeTime()
        throws Exception
    {
        time = 0;
        StopWatch stopWatch = new MockTimeWatch();
        time++;
        stopWatch.pause();
        assertTrue( stopWatch.isPaused() );
        System.out.println( stopWatch.toString() );
        time++;
        stopWatch.resume();
        assertTrue( !stopWatch.isPaused() );
        System.out.println( stopWatch.toString() );
        time++;
        stopWatch.stop();
        assertEquals( 2, stopWatch.getElapsedTime() );
        assertTrue( stopWatch.isStoped() );
        System.out.println( stopWatch.toString() );
    }

    /**
     * Check that the elapsed time computed by the WtopWatch is not affected by
     * unexpected method calls.
     *
     * @throws Exception
     */
    @Test
    public void supportUnexpectedCalls()
        throws Exception
    {
        time = 0;
        StopWatch stopWatch = new MockTimeWatch();

        // resume the non-paused watch
        assertTrue( !stopWatch.isPaused() );
        stopWatch.resume();
        assertTrue( !stopWatch.isPaused() );

        // pause the watch multiple times
        time++;
        stopWatch.pause();
        assertEquals( 1, stopWatch.getElapsedTime() );
        assertTrue( stopWatch.isPaused() );
        time++;
        stopWatch.pause();
        assertEquals( 1, stopWatch.getElapsedTime() );
        assertTrue( stopWatch.isPaused() );

        stopWatch.stop();
        assertEquals( 1, stopWatch.getElapsedTime() );
        assertTrue( stopWatch.isStoped() );

        // Unexpected use after stopped
        stopWatch.resume();
        assertEquals( 1, stopWatch.getElapsedTime() );
        assertTrue( stopWatch.isStoped() );
        stopWatch.pause();
        assertEquals( 1, stopWatch.getElapsedTime() );
        assertTrue( stopWatch.isStoped() );
        stopWatch.stop();
        assertEquals( 1, stopWatch.getElapsedTime() );
        assertTrue( stopWatch.isStoped() );
    }


    private class MockTimeWatch
        extends DefaultStopWatch
    {
        public MockTimeWatch()
        {
            super( new NullMonitor() );
        }

        @Override
        protected long nanotime()
        {
            return time;
        }
    }
}
