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
 * Estimates the time required for process execution (monitored method, service
 * invocation, database request...)
 * 
 * @author <a href="mailto:nicolas@apache.org">Nicolas De Loof</a>
 */
public class StopWatch
{
    /** Time the probe was started */
    private final long startedAt;

    /** Time the probe was stopped */
    private long stopedAt;

    /** Time the probe was paused */
    private long pauseDelay;

    /** flag for stopped probe */
    private boolean stoped;

    /** flag for paused probe */
    private boolean paused;

    /** Monitor that is notified of process execution state */
    private final Monitor monitor;

    /**
     * Constructor.
     * <p>
     * The monitor can be set to null to use the StopWatch without the
     * monitoring infrastructure.
     * 
     * @param monitor the monitor associated with the process to be monitored
     */
    public StopWatch( Monitor monitor )
    {
        super();
        this.monitor = monitor;
        startedAt = nanotime();
        if ( monitor != null )
        {
            monitor.getGauge( Monitor.CONCURRENCY ).increment();
        }
    }

    /**
     * @return Elapsed time (in nanoseconds) for the monitored process
     */
    public long getElapsedTime()
    {
        long delay;
        if ( stoped || paused )
        {
            delay = stopedAt - startedAt - pauseDelay;
        }
        else
        {
            // Still running !
            delay = nanotime() - startedAt - pauseDelay;
        }
        return delay;
    }

    /**
     * Temporary stop the StopWatch. Elapsed time calculation will not include
     * time spent in paused mode.
     */
    public void pause()
    {
        if ( !paused && !stoped )
        {
            stopedAt = nanotime();
            paused = true;
        }
    }

    /**
     * Resume the StopWatch after a pause.
     */
    public void resume()
    {
        if ( paused && !stoped )
        {
            pauseDelay = nanotime() - stopedAt;
            paused = false;
            stopedAt = 0;
        }
    }

    /**
     * Stop monitoring the process. A StopWatch created with
     * {@link #start(Monitor)} cannot be re-used after stopped has been called.
     */
    public void stop()
    {
        if ( !stoped )
        {
            long t = nanotime();
            if ( paused )
            {
                pauseDelay = t - stopedAt;
            }
            stopedAt = t;
            stoped = true;
            if ( monitor != null )
            {
                monitor.getGauge( Monitor.CONCURRENCY ).decrement();
                monitor.getCounter( Monitor.PERFORMANCES ).add( getElapsedTime() );
            }
        }
    }

    /**
     * Convenience method to stop or cancel a Stopwatch depending on success of
     * monitored operation
     * 
     * @param canceled
     * @return time elapsed since the probe has been started
     */
    public void stop( boolean canceled )
    {
        if ( canceled )
        {
            cancel();
        }
        else
        {
            stop();
        }
    }

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
    public void cancel()
    {
        if ( !stoped )
        {
            stoped = true;
            if ( monitor != null )
            {
                monitor.getGauge( Monitor.CONCURRENCY ).decrement();
            }
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * Monitored application should use a <code>try/finally</code> block to
     * ensure on of {@link #stop()} or {@link #cancel()} method is invoked, even
     * when an exception occurs. To avoid StopWatches to keep running if the
     * application didn't follow this recommandation, the finalizer is used to
     * cancel the StopWatch and will log a educational warning.
     * 
     * @see java.lang.Object#finalize()
     */
    protected void finalize()
    {
        // This probe is reclaimed by garbage-collector and still running,
        // the monitored code "forgot" to stop/cancel it properly.
        if ( !stoped && ( monitor != null ) )
        {
            System.err.println( "WARNING : Execution for " + monitor.getKey().toString() + " was not stoped properly. "
                + "This can result in wrong concurrency monitoring. " + "Use try/finally blocks to avoid this warning" );
        }
    }

    /**
     * Returns the current value of the most precise available system timer, in
     * nanoseconds. The real precision depends on the JVM and the underlying
     * system. On JRE before java5, <tt>backport-util-concurrent</tt> provides
     * some limited support for equivalent timer.
     * 
     * @see System#nanoTime()
     * @return time in nanosecond
     */
    protected long nanotime()
    {
        return System.nanoTime();
    }

    /**
     * @return <code>true</code> if the StopWatch has been stopped
     */
    public boolean isStoped()
    {
        return stoped;
    }

    /**
     * @return <code>true</code> if the StopWatch has been paused
     */
    public boolean isPaused()
    {
        return paused;
    }
}
