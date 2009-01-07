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

import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.monitoring.Repository;
import org.apache.commons.monitoring.repositories.ObserverRepository;

/**
 * Base class to periodically log a fixed set of monitored data.
 * 
 * @author <a href="mailto:nicolas@apache.org">Nicolas De Loof</a>
 */
public abstract class AbstractPeriodicLogger
    extends TimerTask
{
    /** The timer that handles the period */
    private Timer timer;

    /** The observed repository */
    private Repository.Observable repository;

    /** The observed repository */
    private ObserverRepository secondary;

    /** The interval for periodic logging of monitored state */
    private int period;

    /**
     * @param period the period (in ms) to log the monitoring state
     * @param repository the observed repository
     */
    public AbstractPeriodicLogger( int period, Repository.Observable repository )
    {
        this.repository = repository;
        this.timer = new Timer();
        this.period = period;
    }

    public void init()
    {
        observeRepositoryForPeriod();
        timer.scheduleAtFixedRate( this, period, period );
    }

    private Repository observeRepositoryForPeriod()
    {
        ObserverRepository previous = this.secondary;
        this.secondary = new ObserverRepository( repository );
        if ( previous != null )
        {
            previous.detach();
        }
        return previous;
    }

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
            Repository observedPeriod = observeRepositoryForPeriod();
            log( observedPeriod );
        }
        catch ( Throwable t )
        {
            // catch any exception, as throwing it will stop the timer
            handleError( t );
        }
    }

    /**
     * Use the data from the (observer) repository generated during the last period
     *
     * @param observeRepositoryForPeriod
     */
    protected abstract void log( Repository repositoryForPeriod );

    /**
     * Warn when logging the repository failed.
     * <p>
     * This method is expected to be override by user to avoid System.err outputs and use the application logging
     * strategy.
     *
     * @param t error during logging
     */
    protected void handleError( Throwable t )
    {
        System.err.println( "Failure to log observed repository : " + t.getMessage() );
    }

    /**
     * @return the SecondaryRepository active for the current period.
     */
    protected Repository getRepositoryForActivePeriod()
    {
        return this.secondary;
    }
}
