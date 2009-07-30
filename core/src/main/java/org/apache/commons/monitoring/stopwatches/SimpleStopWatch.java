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

import static org.apache.commons.monitoring.Unit.Time.NANOSECOND;

import org.apache.commons.monitoring.Monitor;
import org.apache.commons.monitoring.Role;
import org.apache.commons.monitoring.StopWatch;

/**
 * Simple implementation of StopWatch that estimate monitored element execution time.
 *
 * @author <a href="mailto:nicolas@apache.org">Nicolas De Loof</a>
 */
public class SimpleStopWatch
    implements StopWatch
{
    /** Monitor that is notified of process execution state */
    public final Monitor monitor;

    /** Time the probe was started */
    protected final long startedAt;

    /** Time the probe was stopped */
    protected long stopedAt;

    /** Time the probe was paused */
    protected long pauseDelay;

    /** flag for stopped probe */
    protected boolean stoped;

    /** flag for paused probe */
    protected boolean paused;

    private Role role;

    public SimpleStopWatch( Monitor monitor )
    {
        this( monitor, Monitor.PERFORMANCES );
    }

    /**
     * Constructor.
     * <p>
     * The monitor can be set to null to use the StopWatch without the monitoring infrastructure.
     *
     * @param monitor the monitor associated with the process to be monitored
     */
    public SimpleStopWatch( Monitor monitor, Role role )
    {
        super();
        this.role = role;
        this.monitor = monitor;
        startedAt = nanotime();
    }

    /**
     * Returns the current value of the most precise available system timer, in nanoseconds. The real precision depends
     * on the JVM and the underlying system. On JRE before java5, <tt>backport-util-concurrent</tt> provides some
     * limited support for equivalent timer.
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
    public StopWatch pause()
    {
        if ( !paused && !stoped )
        {
            stopedAt = nanotime();
            paused = true;
        }
        return this;
    }

    /**
     * {@inheritDoc}
     *
     * @see org.apache.commons.monitoring.StopWatch#resume()
     */
    public StopWatch resume()
    {
        if ( paused && !stoped )
        {
            pauseDelay = nanotime() - stopedAt;
            paused = false;
            stopedAt = 0;
        }
        return this;
    }

    /**
     * {@inheritDoc}
     *
     * @return TODO
     * @see org.apache.commons.monitoring.StopWatch#stop()
     */
    public StopWatch stop()
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
            doStop();
        }
        return this;
    }

    protected void doStop()
    {
        monitor.getCounter( role ).add( getElapsedTime(), NANOSECOND );
    }

    /**
     * {@inheritDoc}
     *
     * @see org.apache.commons.monitoring.StopWatch#stop(boolean)
     */
    public StopWatch stop( boolean canceled )
    {
        if ( canceled )
        {
            cancel();
        }
        else
        {
            stop();
        }
        return this;
    }

    /**
     * {@inheritDoc}
     *
     * @see org.apache.commons.monitoring.StopWatch#cancel()
     */
    public StopWatch cancel()
    {
        if ( !stoped )
        {
            stoped = true;
            doCancel();
        }
        return this;
    }

    protected void doCancel()
    {

    }

    /**
     * {@inheritDoc}
     * <p>
     * Monitored application should use a <code>try/finally</code> block to ensure on of {@link #stop()} or
     * {@link #cancel()} method is invoked, even when an exception occurs. To avoid StopWatches to keep running if the
     * application didn't follow this recommendation, the finalizer is used to cancel the StopWatch and will log a
     * educational warning.
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