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

import org.apache.commons.monitoring.Monitor;
import org.apache.commons.monitoring.StopWatch;

/**
 * @author <a href="mailto:nicolas@apache.org">Nicolas De Loof</a>
 *
 */
public abstract class StopWatchDecorator
    implements StopWatch
{
    private StopWatch decorated;

    /**
     * @param decorated
     */
    public StopWatchDecorator( StopWatch decorated )
    {
        super();
        this.decorated = decorated;
    }

    public StopWatch cancel()
    {
        return decorated.cancel();
    }

    public long getElapsedTime()
    {
        return decorated.getElapsedTime();
    }

    public Monitor getMonitor()
    {
        return decorated.getMonitor();
    }

    public boolean isPaused()
    {
        return decorated.isPaused();
    }

    public boolean isStoped()
    {
        return decorated.isStoped();
    }

    public StopWatch pause()
    {
        return decorated.pause();
    }

    public StopWatch resume()
    {
        return decorated.resume();
    }

    public StopWatch stop()
    {
        return decorated.stop();
    }

    public StopWatch stop( boolean canceled )
    {
        return decorated.stop( canceled );
    }

    public StopWatch getDecorated()
    {
        return decorated;
    }
}
