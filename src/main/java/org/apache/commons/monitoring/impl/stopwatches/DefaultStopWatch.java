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

package org.apache.commons.monitoring.impl.stopwatches;

import org.apache.commons.monitoring.Monitor;
import org.apache.commons.monitoring.StopWatch;
import org.apache.commons.monitoring.Unit;

/**
 * Estimates the time required for process execution (monitored method, service
 * invocation, database request...).
 *
 * @author <a href="mailto:nicolas@apache.org">Nicolas De Loof</a>
 */
public class DefaultStopWatch
    implements StopWatch
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
    public DefaultStopWatch( Monitor monitor )
    {
        super();
        this.monitor = monitor;
        startedAt = nanotime();
        if ( monitor != null )
        {
            monitor.getGauge( Monitor.CONCURRENCY ).increment( Unit.UNARY );
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see org.apache.commons.monitoring.StopWatch#getElapsedTime()
     */
    public long getElapsedTime()
    {
        if ( stoped || paused )
        {
            return stopedAt - startedAt - pauseDelay;
        }
        else
        {
            // Still running !
            return nanotime() - startedAt - pauseDelay;
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see org.apache.commons.monitoring.StopWatch#pause()
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
     * {@inheritDoc}
     *
     * @see org.apache.commons.monitoring.StopWatch#resume()
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
     * {@inheritDoc}
     *
     * @see org.apache.commons.monitoring.StopWatch#stop()
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
                monitor.getGauge( Monitor.CONCURRENCY ).decrement( Unit.UNARY );
                monitor.getCounter( Monitor.PERFORMANCES ).add( getElapsedTime(), Unit.NANOS );
            }
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see org.apache.commons.monitoring.StopWatch#stop(boolean)
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
     * {@inheritDoc}
     *
     * @see org.apache.commons.monitoring.StopWatch#cancel()
     */
    public void cancel()
    {
        if ( !stoped )
        {
            stoped = true;
            if ( monitor != null )
            {
                monitor.getGauge( Monitor.CONCURRENCY ).decrement( Unit.UNARY );
            }
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * Monitored application should use a <code>try/finally</code> block to
     * ensure on of {@link #stop()} or {@link #cancel()} method is invoked, even
     * when an exception occurs. To avoid StopWatches to keep running if the
     * application didn't follow this recommendation, the finalizer is used to
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
     * {@inheritDoc}
     *
     * @see org.apache.commons.monitoring.StopWatch#isStoped()
     */
    public boolean isStoped()
    {
        return stoped;
    }

    /**
     * {@inheritDoc}
     *
     * @see org.apache.commons.monitoring.StopWatch#isPaused()
     */
    public boolean isPaused()
    {
        return paused;
    }

    @Override
    public String toString()
    {
        StringBuffer stb = new StringBuffer();
        if ( monitor != null )
        {
            stb.append( "Execution for " ).append( monitor.getKey().toString() ).append( " " );
        }
        if ( paused )
        {
            stb.append( "paused after " ).append( getElapsedTime() ).append( "ns" );
        }
        else if ( stoped )
        {
            stb.append( "stoped after " ).append( getElapsedTime() ).append( "ns" );
        }
        else
        {
            stb.append( "running for " ).append( getElapsedTime() ).append( "ns" );
        }
        return stb.toString();

    }

    /**
     * {@inheritDoc}
     *
     * @see org.apache.commons.monitoring.StopWatch#getMonitor()
     */
    public Monitor getMonitor()
    {
        return monitor;
    }
}
