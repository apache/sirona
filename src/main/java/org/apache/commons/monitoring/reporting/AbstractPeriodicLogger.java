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

package org.apache.commons.monitoring.reporting;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.monitoring.Repository;
import org.apache.commons.monitoring.listeners.SecondaryRepository;

/**
 * An helper class to build a periodic task that logs the monitored application
 * state for a period.
 * <p>
 * Typical use of this class is to implement the log method to format the
 * indicators and append the result for each period in a log file.
 *
 * @author <a href="mailto:nicolas@apache.org">Nicolas De Loof</a>
 */
public abstract class AbstractPeriodicLogger
    extends TimerTask
{
    /** The timer that handles the period */
    private Timer timer;

    /** The observed repository */
    private Repository repository;

    /** The observed repository */
    private SecondaryRepository secondary;


    /**
     * @param period the period (in ms) to log the monitoring state
     * @param repository the target monitoring repository
     */
    public AbstractPeriodicLogger( long period, Repository repository )
    {
        super();
        this.secondary = new SecondaryRepository( repository );
        timer = new Timer();
        timer.scheduleAtFixedRate( this, period, period );
    }

    /**
     *
     */
    public void stop()
    {
        timer.cancel();
    }

    /**
     * {@inheritDoc}
     *
     * @see java.util.TimerTask#run()
     */
    @Override
    public void run()
    {
        try
        {
            // Create a new observer for the next period
            SecondaryRepository period = secondary;
            period.detach();
            secondary = new SecondaryRepository( repository );

            log( period );
        }
        catch (Exception exception)
        {
            // catch any exception, as throwing it will stop the time
        }
    }

    /**
     * Log the data from the (secondary) repository generated during the period
     * @param period secondary repository that observed the monitored state during the last active period
     */
    protected abstract void log( Repository period ) throws IOException;
}
