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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import org.apache.commons.monitoring.Repository;

/**
 * An abstract periodic logger implementation that uses a predefine set of
 * "indicators" to output monitored state in a log.
 * <p>
 * Typical use case is to produce a fixed format (CSV, Excel-like, tabular...)
 * in a log file, with a new line for each period.
 *
 * @author <a href="mailto:nicolas@apache.org">Nicolas De Loof</a>
 */
public abstract class AppendToFilePeriodicLogger
    extends AbstractPeriodicLogger
{

    private File output;

    /**
     * @param period the period (in ms) to log the monitoring state
     * @param repository the target monitoring repository
     * @param output the output file
     */
    public AppendToFilePeriodicLogger( long period, Repository repository, File output )
    {
        super( period, repository );
        this.output = output;
    }

    /**
     * Log the data from the (secondary) repository generated during the period
     *
     * @param period secondary repository that observed the monitored state
     * during the last active period
     */
    @Override
    protected final void log( Repository period )
        throws IOException
    {
        output.mkdirs();
        Writer writer = new FileWriter( output, true );
        // Log the detached state
        log( period, writer );
        writer.close();
    }

    /**
     * Log the data from the (secondary) repository generated during the period
     *
     * @param period secondary repository that observed the monitored state
     * during the last active period
     * @param writer the output to log repository
     * @throws IOException any I/O error during log
     */
    protected abstract void log( Repository period, Writer writer ) throws IOException;

}
