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

/**
 * Instrumentation tool to compute resource consumption of some code fragment execution.
 * <p>
 * StopWatch implementation is supposed not to be thread-safe and to be a one-shot tool. Don't
 * share it beetween threads, don't try to reuse it.
 *
 * @author <a href="mailto:nicolas@apache.org">Nicolas De Loof</a>
 */
public interface StopWatch
{
    /**
     * @return Elapsed time (in nanoseconds) for the monitored process, not
     * including paused time
     */
    public abstract long getElapsedTime();

    /**
     * Temporary stop the StopWatch. Elapsed time calculation will not include
     * time spent in paused mode.
     */
    public abstract StopWatch pause();

    /**
     * Resume the StopWatch after a pause.
     */
    public abstract StopWatch resume();

    /**
     * Stop monitoring the process. A StopWatch created with
     * {@link #start(Monitor)} cannot be re-used after stopped has been called.
     * @return TODO
     */
    public abstract StopWatch stop();

    /**
     * Convenience method to stop or cancel a Stopwatch depending on success of
     * monitored operation
     *
     * @param canceled
     * @return time elapsed since the probe has been started
     */
    public abstract StopWatch stop( boolean canceled );

    /**
     * Cancel monitoring. Elapsed time will not be computed and will not be
     * published to the monitor.
     * <p>
     * In some circumstances you want to monitor time elapsed from early stage
     * of computation, and discover latter if the computed data is relevant. For
     * example, monitoring a messaging system, but beeing interested only by
     * some types of messages. In such case, a StopWatch can be started early
     * and canceled when the application is able to determine it's relevancy.
     * <p>
     * In any way, the probe will still report thread concurrency even if
     * canceled.
     */
    public abstract StopWatch cancel();

    /**
     * @return <code>true</code> if the StopWatch has been stopped
     */
    public abstract boolean isStoped();

    /**
     * @return <code>true</code> if the StopWatch has been paused
     */
    public abstract boolean isPaused();

    public abstract Monitor getMonitor();

}