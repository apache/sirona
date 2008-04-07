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
import java.util.Arrays;
import java.util.List;

import org.apache.commons.monitoring.Repository;
import org.apache.commons.monitoring.listeners.SecondaryRepository;

/**
 * A periodic logger implementation that uses a set of selector to extract
 * monitoring datas to log.
 * <p>
 * Typical use case is to produce a fixed format (CSV, Excel-like, tabular...)
 * in a log file, with a new line for each period.
 *
 * @author <a href="mailto:nicolas@apache.org">Nicolas De Loof</a>
 */
public abstract class SelectorPeriodicLogger
    extends AbstractPeriodicLogger
{
    private Selector[] selectors;

    /**
     * @param period the period (in ms) to log the monitoring state
     * @param repository the target monitoring repository
     * @param output the output file
     */
    public SelectorPeriodicLogger( long period, Repository.Observable repository, List<String> selectors )
    {
        super( period, repository );
        this.selectors = new Selector[selectors.size()];
        int i = 0;
        for ( String path : selectors )
        {
            this.selectors[i++] = new Selector( path );
        }
    }

    /**
     * @param period the period (in ms) to log the monitoring state
     * @param repository the target monitoring repository
     * @param output the output file
     */
    public SelectorPeriodicLogger( long period, Repository.Observable repository, String[] selectors )
    {
        this( period, repository, Arrays.asList( selectors ) );
    }

    /**
     * Log the data from the (secondary) repository generated during the period
     *
     * @param period secondary repository that observed the monitored state
     * during the last active period
     */
    @Override
    protected final void log( SecondaryRepository period )
        throws IOException
    {
        Object[] values = new Object[selectors.length];
        for ( int i = 0; i < selectors.length; i++ )
        {
            values[i] = selectors[i].select( period );
        }
        log( values );
    }

    /**
     * Log the data extracted by selectors
     *
     * @param values the data to log
     * @throws IOException any I/O error during log
     */
    protected abstract void log( Object[] values )
        throws IOException;

}
